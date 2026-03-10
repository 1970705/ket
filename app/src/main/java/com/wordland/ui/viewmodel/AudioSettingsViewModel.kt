package com.wordland.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordland.ui.uistate.AudioSettingsUiState
import com.wordland.ui.uistate.TtsLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Audio Settings Screen
 *
 * Manages audio settings and persistence
 */
class AudioSettingsViewModel(
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AudioSettingsUiState>(AudioSettingsUiState.Loading)
    val uiState: StateFlow<AudioSettingsUiState> = _uiState.asStateFlow()

    companion object {
        const val PREFS_NAME = "audio_settings"
        private const val KEY_BG_MUSIC_ENABLED = "background_music_enabled"
        private const val KEY_BG_MUSIC_VOLUME = "background_music_volume"
        private const val KEY_SFX_ENABLED = "sound_effects_enabled"
        private const val KEY_SFX_VOLUME = "sound_effects_volume"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_TTS_LANGUAGE = "tts_language"

        private const val DEFAULT_BG_MUSIC_ENABLED = true
        private const val DEFAULT_BG_MUSIC_VOLUME = 0.7f
        private const val DEFAULT_SFX_ENABLED = true
        private const val DEFAULT_SFX_VOLUME = 0.8f
        private const val DEFAULT_TTS_ENABLED = true
        private const val DEFAULT_TTS_LANGUAGE = "en-US"
    }

    init {
        loadSettings()
    }

    /**
     * Load settings from SharedPreferences
     */
    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value =
                AudioSettingsUiState.Ready(
                    backgroundMusicEnabled =
                        sharedPreferences.getBoolean(
                            KEY_BG_MUSIC_ENABLED,
                            DEFAULT_BG_MUSIC_ENABLED,
                        ),
                    backgroundMusicVolume =
                        sharedPreferences.getFloat(
                            KEY_BG_MUSIC_VOLUME,
                            DEFAULT_BG_MUSIC_VOLUME,
                        ),
                    soundEffectsEnabled =
                        sharedPreferences.getBoolean(
                            KEY_SFX_ENABLED,
                            DEFAULT_SFX_ENABLED,
                        ),
                    soundEffectsVolume =
                        sharedPreferences.getFloat(
                            KEY_SFX_VOLUME,
                            DEFAULT_SFX_VOLUME,
                        ),
                    ttsEnabled =
                        sharedPreferences.getBoolean(
                            KEY_TTS_ENABLED,
                            DEFAULT_TTS_ENABLED,
                        ),
                    ttsLanguage =
                        TtsLanguage.fromLocaleCode(
                            sharedPreferences.getString(KEY_TTS_LANGUAGE, DEFAULT_TTS_LANGUAGE)
                                ?: DEFAULT_TTS_LANGUAGE,
                        ),
                )
        }
    }

    /**
     * Toggle background music
     */
    fun toggleBackgroundMusic(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is AudioSettingsUiState.Ready) {
            _uiState.value = currentState.copy(backgroundMusicEnabled = enabled)
            saveSetting(KEY_BG_MUSIC_ENABLED, enabled)
        }
    }

    /**
     * Update background music volume
     */
    fun updateBackgroundMusicVolume(volume: Float) {
        val currentState = _uiState.value
        if (currentState is AudioSettingsUiState.Ready) {
            _uiState.value = currentState.copy(backgroundMusicVolume = volume)
            saveSetting(KEY_BG_MUSIC_VOLUME, volume)
        }
    }

    /**
     * Toggle sound effects
     */
    fun toggleSoundEffects(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is AudioSettingsUiState.Ready) {
            _uiState.value = currentState.copy(soundEffectsEnabled = enabled)
            saveSetting(KEY_SFX_ENABLED, enabled)
        }
    }

    /**
     * Update sound effects volume
     */
    fun updateSoundEffectsVolume(volume: Float) {
        val currentState = _uiState.value
        if (currentState is AudioSettingsUiState.Ready) {
            _uiState.value = currentState.copy(soundEffectsVolume = volume)
            saveSetting(KEY_SFX_VOLUME, volume)
        }
    }

    /**
     * Toggle TTS
     */
    fun toggleTts(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is AudioSettingsUiState.Ready) {
            _uiState.value = currentState.copy(ttsEnabled = enabled)
            saveSetting(KEY_TTS_ENABLED, enabled)
        }
    }

    /**
     * Update TTS language
     */
    fun updateTtsLanguage(language: TtsLanguage) {
        val currentState = _uiState.value
        if (currentState is AudioSettingsUiState.Ready) {
            _uiState.value = currentState.copy(ttsLanguage = language)
            saveSetting(KEY_TTS_LANGUAGE, language.localeCode)
        }
    }

    /**
     * Reset all settings to default
     */
    fun resetToDefault() {
        viewModelScope.launch {
            _uiState.value =
                AudioSettingsUiState.Ready(
                    backgroundMusicEnabled = DEFAULT_BG_MUSIC_ENABLED,
                    backgroundMusicVolume = DEFAULT_BG_MUSIC_VOLUME,
                    soundEffectsEnabled = DEFAULT_SFX_ENABLED,
                    soundEffectsVolume = DEFAULT_SFX_VOLUME,
                    ttsEnabled = DEFAULT_TTS_ENABLED,
                    ttsLanguage = TtsLanguage.fromLocaleCode(DEFAULT_TTS_LANGUAGE),
                )

            // Save all defaults
            saveSetting(KEY_BG_MUSIC_ENABLED, DEFAULT_BG_MUSIC_ENABLED)
            saveSetting(KEY_BG_MUSIC_VOLUME, DEFAULT_BG_MUSIC_VOLUME)
            saveSetting(KEY_SFX_ENABLED, DEFAULT_SFX_ENABLED)
            saveSetting(KEY_SFX_VOLUME, DEFAULT_SFX_VOLUME)
            saveSetting(KEY_TTS_ENABLED, DEFAULT_TTS_ENABLED)
            saveSetting(KEY_TTS_LANGUAGE, DEFAULT_TTS_LANGUAGE)
        }
    }

    /**
     * Save a setting to SharedPreferences
     */
    private fun saveSetting(
        key: String,
        value: Any,
    ) {
        with(sharedPreferences.edit()) {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is String -> putString(key, value)
                else -> throw IllegalArgumentException("Unsupported type: ${value::class}")
            }
            apply()
        }
    }

    /**
     * Get current settings as a map (for testing/external use)
     */
    fun getCurrentSettings(): Map<String, Any?> {
        val state = _uiState.value
        return if (state is AudioSettingsUiState.Ready) {
            mapOf(
                "backgroundMusicEnabled" to state.backgroundMusicEnabled,
                "backgroundMusicVolume" to state.backgroundMusicVolume,
                "soundEffectsEnabled" to state.soundEffectsEnabled,
                "soundEffectsVolume" to state.soundEffectsVolume,
                "ttsEnabled" to state.ttsEnabled,
                "ttsLanguage" to state.ttsLanguage,
            )
        } else {
            emptyMap()
        }
    }
}

/**
 * Factory for creating AudioSettingsViewModel with SharedPreferences
 */
class AudioSettingsViewModelFactory(
    private val context: Context,
) {
    fun create(): AudioSettingsViewModel {
        val prefs = context.getSharedPreferences(AudioSettingsViewModel.PREFS_NAME, Context.MODE_PRIVATE)
        return AudioSettingsViewModel(prefs)
    }
}
