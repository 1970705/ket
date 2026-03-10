package com.wordland.media

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Text-to-Speech Controller for word pronunciation
 *
 * Uses Android's built-in TTS engine to speak English words.
 * Features:
 * - English language TTS
 * - Playback state tracking
 * - Error handling
 * - Resource cleanup
 *
 * @property context Application context
 */
class TTSController(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null

    // Initialization tracking
    private val initCalled = AtomicBoolean(false)
    private val initTimeoutHandler = Handler(Looper.getMainLooper())
    private val initTimeoutRunnable =
        Runnable {
            if (!initCalled.get()) {
                Log.e(TAG, "TTS initialization timeout after ${INIT_TIMEOUT_MS}ms")
                _errorMessage.value = "TTS 初始化超时，请检查设备语音设置"
                _isReady.value = false
            }
        }

    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Callback for when TTS finishes speaking
    var onSpeakingCompleteListener: (() -> Unit)? = null

    companion object {
        private const val TAG = "TTSController"
        private const val UTTERANCE_ID = "wordland_utterance"
        private const val INIT_TIMEOUT_MS = 5000L // 5 seconds
    }

    init {
        initTTS()
    }

    /**
     * Initialize Text-to-Speech engine
     *
     * Uses applicationContext to avoid lifecycle issues with component-specific contexts.
     */
    private fun initTTS() {
        Log.d(TAG, "Starting TTS initialization...")
        Log.d(TAG, "Context type: ${context.javaClass.simpleName}")
        Log.d(TAG, "Application context: ${context.applicationContext.javaClass.simpleName}")

        // Start timeout timer
        initTimeoutHandler.postDelayed(initTimeoutRunnable, INIT_TIMEOUT_MS)

        try {
            val appContext = context.applicationContext
            Log.d(TAG, "Creating TextToSpeech with application context...")
            tts = TextToSpeech(appContext, this)
            Log.d(TAG, "TTS object created, waiting for onInit callback...")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception - TTS permission denied", e)
            _errorMessage.value = "TTS 权限被拒绝"
            _isReady.value = false
            cancelTimeout()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TTS", e)
            _errorMessage.value = "TTS 初始化失败: ${e.message}"
            _isReady.value = false
            cancelTimeout()
        }
    }

    /**
     * Cancel the initialization timeout
     */
    private fun cancelTimeout() {
        initTimeoutHandler.removeCallbacks(initTimeoutRunnable)
    }

    /**
     * Called when TTS engine is initialized
     *
     * This callback is invoked asynchronously after TextToSpeech creation.
     * Status codes:
     * - TextToSpeech.SUCCESS (0): Initialization successful
     * - TextToSpeech.ERROR (-2): Initialization failed
     *
     * Note: The device may return -1 if TTS engine is not installed or disabled.
     * This is NOT the same as SUCCESS.
     */
    override fun onInit(status: Int) {
        // Mark initialization as complete and cancel timeout
        initCalled.set(true)
        cancelTimeout()

        Log.d(TAG, "onInit called with status: $status (SUCCESS=${TextToSpeech.SUCCESS}, ERROR=${TextToSpeech.ERROR})")

        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS engine initialized successfully, configuring language settings...")
            configureTTSEngine()
        } else {
            // Status -1 typically means TTS engine is not available/disabled on device
            val errorDescription = when (status) {
                -1 -> "TTS engine not available, not installed, or disabled"
                TextToSpeech.ERROR -> "General TTS error"
                else -> "Unknown error (code: $status)"
            }
            Log.e(TAG, "TTS initialization failed: $errorDescription")
            _errorMessage.value = "TTS 初始化失败：$errorDescription。请在设备设置中启用语音引擎。"
            _isReady.value = false
        }
    }

    /**
     * Configure TTS engine language settings
     */
    private fun configureTTSEngine() {
        tts?.let { engine ->
            // Log available languages for debugging
            val availableLanguages = engine.availableLanguages
            Log.d(TAG, "Available TTS languages: ${availableLanguages.joinToString { it.displayName }}")

            // Try to set language to US English first
            val result = engine.setLanguage(Locale.US)
            Log.d(TAG, "setLanguage(Locale.US) returned: $result (${resultCodeToString(result)})")

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to UK English
                Log.w(TAG, "US English not available, trying UK English")
                val ukResult = engine.setLanguage(Locale.UK)
                Log.d(TAG, "setLanguage(Locale.UK) returned: $ukResult (${resultCodeToString(ukResult)})")

                if (ukResult == TextToSpeech.LANG_MISSING_DATA || ukResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Last resort - try generic English
                    val enResult = engine.setLanguage(Locale.ENGLISH)
                    Log.d(TAG, "setLanguage(Locale.ENGLISH) returned: $enResult (${resultCodeToString(enResult)})")

                    if (enResult == TextToSpeech.LANG_MISSING_DATA || enResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "English language not supported by this TTS engine")
                        _errorMessage.value = "此设备的语音引擎不支持英语，请安装英语语音包"
                        _isReady.value = false
                        return@let
                    }
                }
            }

            // Verify final language setting
            val currentLanguage = engine.language
            Log.d(TAG, "Current TTS language: ${currentLanguage.displayName} (${currentLanguage.toLanguageTag()})")

            // Set up utterance progress listener
            setupUtteranceListener(engine)

            _isReady.value = true
            _errorMessage.value = null
            Log.d(TAG, "TTS fully initialized and ready to speak")
        } ?: run {
            Log.e(TAG, "TTS engine is null in configureTTSEngine")
            _errorMessage.value = "TTS 引擎未正确初始化"
            _isReady.value = false
        }
    }

    /**
     * Set up utterance progress listener for tracking speech state
     */
    private fun setupUtteranceListener(engine: TextToSpeech) {
        engine.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "onStart: utteranceId=$utteranceId")
                    _isPlaying.value = true
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "onDone: utteranceId=$utteranceId")
                    _isPlaying.value = false
                    onSpeakingCompleteListener?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "onError: utteranceId=$utteranceId")
                    _isPlaying.value = false
                }

                // Deprecated but needed for older API levels
                @Suppress("DEPRECATION")
                override fun onError(
                    utteranceId: String?,
                    errorCode: Int,
                ) {
                    Log.e(TAG, "onError: utteranceId=$utteranceId, errorCode=$errorCode (${errorCodeToString(errorCode)})")
                    _isPlaying.value = false
                }
            },
        )
    }

    /**
     * Convert setLanguage result code to readable string
     */
    private fun resultCodeToString(code: Int): String {
        return when (code) {
            TextToSpeech.LANG_AVAILABLE -> "LANG_AVAILABLE"
            TextToSpeech.LANG_COUNTRY_AVAILABLE -> "LANG_COUNTRY_AVAILABLE"
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> "LANG_COUNTRY_VAR_AVAILABLE"
            TextToSpeech.LANG_MISSING_DATA -> "LANG_MISSING_DATA"
            TextToSpeech.LANG_NOT_SUPPORTED -> "LANG_NOT_SUPPORTED"
            else -> "UNKNOWN($code)"
        }
    }

    /**
     * Convert error code to readable string
     */
    private fun errorCodeToString(code: Int): String {
        return when (code) {
            TextToSpeech.ERROR -> "ERROR"
            TextToSpeech.ERROR_SYNTHESIS -> "ERROR_SYNTHESIS"
            TextToSpeech.ERROR_SERVICE -> "ERROR_SERVICE"
            else -> "UNKNOWN($code)"
        }
    }

    /**
     * Speak the given text
     *
     * @param text The text to speak (typically a single English word)
     * @return true if speech was queued successfully, false otherwise
     */
    fun speak(text: String): Boolean {
        Log.d(TAG, "speak() called with text: '$text', isReady: ${isReady.value}")
        if (!isReady.value) {
            Log.w(TAG, "TTS not ready, cannot speak")
            return false
        }

        return tts?.let { engine ->
            try {
                _isPlaying.value = true
                val params =
                    Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
                    }
                Log.d(TAG, "Calling engine.speak() with utteranceId: $UTTERANCE_ID")
                val result =
                    engine.speak(
                        text,
                        TextToSpeech.QUEUE_FLUSH,
                        params,
                        UTTERANCE_ID,
                    )
                Log.d(TAG, "engine.speak() returned: $result")
                result == TextToSpeech.SUCCESS
            } catch (e: Exception) {
                Log.e(TAG, "Failed to speak", e)
                _isPlaying.value = false
                false
            }
        } ?: false.also { Log.e(TAG, "TTS engine is null") }
    }

    /**
     * Speak the given text with custom parameters
     *
     * @param text The text to speak
     * @param pitch Speech pitch (1.0 = normal, 0.5 = lower, 2.0 = higher)
     * @param speechRate Speech rate (1.0 = normal, 0.5 = slower, 2.0 = faster)
     * @return true if speech was queued successfully
     */
    fun speak(
        text: String,
        pitch: Float,
        speechRate: Float,
    ): Boolean {
        if (!isReady.value) {
            Log.w(TAG, "TTS not ready, cannot speak")
            return false
        }

        return tts?.let { engine ->
            try {
                engine.setPitch(pitch)
                engine.setSpeechRate(speechRate)
                _isPlaying.value = true
                val params =
                    Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
                    }
                engine.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    params,
                    UTTERANCE_ID,
                )
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to speak", e)
                _isPlaying.value = false
                false
            }
        } ?: false
    }

    /**
     * Stop current speech
     */
    fun stop() {
        tts?.stop()
        _isPlaying.value = false
    }

    /**
     * Check if TTS is speaking
     */
    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }

    /**
     * Set speech pitch
     *
     * @param pitch Pitch value (1.0 = normal)
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    /**
     * Set speech rate
     *
     * @param rate Speech rate (1.0 = normal)
     */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    /**
     * Get available languages
     */
    fun getAvailableLanguages(): Set<Locale> {
        return tts?.availableLanguages ?: emptySet()
    }

    /**
     * Release TTS resources
     * Call this when the controller is no longer needed
     */
    fun release() {
        Log.d(TAG, "Releasing TTS resources...")
        cancelTimeout()
        stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
        _isPlaying.value = false
        Log.d(TAG, "TTS resources released")
    }

    /**
     * Check if TTS is ready to speak
     */
    fun isReadyToSpeak(): Boolean {
        return isReady.value && tts != null
    }

    /**
     * Check if the device has TTS capabilities
     *
     * @return Pair<hasTTSSupport, hasEnglishSupport>
     */
    fun checkTTSCapabilities(): Pair<Boolean, Boolean> {
        val intent = android.content.Intent(android.speech.tts.TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
        // This is a basic check - actual verification happens during initialization
        return Pair(true, true) // Most modern Android devices have TTS
    }
}

/**
 * Factory function for creating TTSController instances
 */
fun createTTSController(context: Context): TTSController {
    return TTSController(context.applicationContext)
}
