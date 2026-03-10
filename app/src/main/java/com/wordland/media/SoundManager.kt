package com.wordland.media

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Unified sound effects and background music manager
 *
 * Features:
 * - Sound effects playback with concurrent support
 * - Background music with loop control
 * - Volume control for master, SFX, and music
 * - Enable/disable toggles for SFX and music
 * - Persistent settings via SharedPreferences
 * - Lifecycle management (pause/resume/release)
 *
 * @property context Application context
 */
@Singleton
class SoundManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            private const val TAG = "SoundManager"
            private const val PREFS_NAME = "sound_settings"

            // Preference keys
            private const val KEY_MASTER_VOLUME = "master_volume"
            private const val KEY_SFX_VOLUME = "sfx_volume"
            private const val KEY_MUSIC_VOLUME = "music_volume"
            private const val KEY_SOUND_ENABLED = "sound_enabled"
            private const val KEY_MUSIC_ENABLED = "music_enabled"
            private const val KEY_CURRENT_MUSIC = "current_music"

            // Volume defaults (0.0 - 1.0)
            private const val DEFAULT_MASTER_VOLUME = 0.7f
            private const val DEFAULT_SFX_VOLUME = 1.0f
            private const val DEFAULT_MUSIC_VOLUME = 0.5f

            // Volume ranges
            private const val MIN_VOLUME = 0.0f
            private const val MAX_VOLUME = 1.0f

            // SFX directory in assets
            private const val SFX_PATH = "audio/sfx/"
            private const val MUSIC_PATH = "audio/music/"
        }

        // SharedPreferences for persistent settings
        private val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Coroutine scope for async operations
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        // Volume settings
        private var masterVolume: Float = prefs.getFloat(KEY_MASTER_VOLUME, DEFAULT_MASTER_VOLUME)
        private var sfxVolume: Float = prefs.getFloat(KEY_SFX_VOLUME, DEFAULT_SFX_VOLUME)
        private var musicVolume: Float = prefs.getFloat(KEY_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME)

        // Enable/disable settings
        private var soundEnabled: Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        private var musicEnabled: Boolean = prefs.getBoolean(KEY_MUSIC_ENABLED, true)

        // Background music player (singleton for BGM)
        private var bgmPlayer: MediaPlayer? = null
        private var currentBgmTrack: String? = null
        private var bgmIsLooping = true
        private var bgmPausedPosition = 0

        // Sound effect players (pool for concurrent playback)
        private val sfxPlayers = mutableListOf<MediaPlayer>()
        private val MAX_CONCURRENT_SFX = 5

        // Audio asset manager for word pronunciation
        private val audioAssetManager: com.wordland.data.assets.AudioAssetManager by lazy {
            com.wordland.data.assets.AudioAssetManager(context)
        }

        // System sound effects generator as fallback
        private val systemSoundEffects = SystemSoundEffectsGenerator()

        // =====================================================
        // Sound Effects Playback
        // =====================================================

        /**
         * Play a sound effect
         *
         * @param effect The sound effect to play
         */
        suspend fun playSoundEffect(effect: SoundEffect) {
            if (!soundEnabled || sfxVolume <= 0f || masterVolume <= 0f) {
                Log.d(TAG, "Sound disabled, skipping: ${effect.fileName}")
                return
            }

            withContext(Dispatchers.IO) {
                try {
                    playSfxFromFile("${SFX_PATH}${effect.fileName}")
                    Log.d(TAG, "Playing sound effect: ${effect.fileName}")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to play sound effect from file: ${effect.fileName}, using system sound", e)
                    // Fallback to system sound effect
                    playSystemSoundEffect(effect)
                }
            }
        }

        /**
         * Play system sound effect as fallback
         */
        private fun playSystemSoundEffect(effect: SoundEffect) {
            val volume = (sfxVolume * 100).toInt()
            when (effect) {
                SoundEffect.CORRECT -> systemSoundEffects.playCorrectAnswer(volume)
                SoundEffect.WRONG -> systemSoundEffects.playWrongAnswer(volume)
                SoundEffect.COMBO, SoundEffect.COMBO_3, SoundEffect.COMBO_5, SoundEffect.COMBO_10 -> {
                    val comboLevel =
                        when (effect) {
                            SoundEffect.COMBO_10 -> 10
                            SoundEffect.COMBO_5 -> 5
                            SoundEffect.COMBO_3 -> 3
                            else -> 1
                        }
                    systemSoundEffects.playCombo(comboLevel, volume)
                }
                SoundEffect.LEVEL_COMPLETE -> systemSoundEffects.playLevelComplete(volume)
                SoundEffect.BUTTON_CLICK -> systemSoundEffects.playButtonClick(volume)
                SoundEffect.HINT -> systemSoundEffects.playHint(volume)
                else -> {
                    Log.d(TAG, "No system sound for effect: ${effect.fileName}")
                }
            }
        }

        /**
         * Play correct answer sound
         */
        suspend fun playCorrectAnswer() = playSoundEffect(SoundEffect.CORRECT)

        /**
         * Play wrong answer sound
         */
        suspend fun playWrongAnswer() = playSoundEffect(SoundEffect.WRONG)

        /**
         * Play combo sound (varies by combo level)
         *
         * @param level Combo level (affects pitch or intensity)
         */
        suspend fun playCombo(level: Int) {
            // Play combo sound with potential variation based on level
            val effect =
                when {
                    level >= 10 -> SoundEffect.COMBO_10
                    level >= 5 -> SoundEffect.COMBO_5
                    level >= 3 -> SoundEffect.COMBO_3
                    else -> SoundEffect.COMBO
                }
            playSoundEffect(effect)
            Log.d(TAG, "Playing combo sound for level: $level")
        }

        /**
         * Play level complete sound
         */
        suspend fun playLevelComplete() = playSoundEffect(SoundEffect.LEVEL_COMPLETE)

        /**
         * Play star earned sound
         */
        suspend fun playStarEarned() = playSoundEffect(SoundEffect.STAR_EARNED)

        /**
         * Play achievement unlock sound
         */
        suspend fun playAchievementUnlock() = playSoundEffect(SoundEffect.ACHIEVEMENT_UNLOCK)

        /**
         * Play button click sound
         */
        suspend fun playButtonClick() = playSoundEffect(SoundEffect.BUTTON_CLICK)

        /**
         * Play hint sound
         */
        suspend fun playHintSound() = playSoundEffect(SoundEffect.HINT)

        /**
         * Play chest open sound
         */
        suspend fun playChestOpen() = playSoundEffect(SoundEffect.CHEST_OPEN)

        /**
         * Play island unlock sound
         */
        suspend fun playIslandUnlock() = playSoundEffect(SoundEffect.ISLAND_UNLOCK)

        // =====================================================
        // Background Music Control
        // =====================================================

        /**
         * Play background music
         *
         * @param musicId Music filename (e.g., "main_theme.mp3")
         * @param loop Whether to loop the music (default: true)
         */
        fun playBackgroundMusic(
            musicId: String,
            loop: Boolean = true,
        ) {
            if (!musicEnabled || musicVolume <= 0f || masterVolume <= 0f) {
                Log.d(TAG, "Music disabled, skipping BGM: $musicId")
                return
            }

            scope.launch {
                try {
                    // Stop current BGM if playing
                    stopBackgroundMusic()

                    val musicPath = "${MUSIC_PATH}$musicId"
                    currentBgmTrack = musicId
                    bgmIsLooping = loop

                    withContext(Dispatchers.IO) {
                        // Create new MediaPlayer for BGM
                        val assetDescriptor = context.assets.openFd(musicPath)
                        bgmPlayer =
                            MediaPlayer().apply {
                                setDataSource(
                                    assetDescriptor.fileDescriptor,
                                    assetDescriptor.startOffset,
                                    assetDescriptor.length,
                                )
                                assetDescriptor.close()
                                isLooping = loop

                                // Set music volume
                                val effectiveVolume = masterVolume * musicVolume
                                setVolume(effectiveVolume, effectiveVolume)

                                setOnPreparedListener { player ->
                                    player.start()
                                    Log.d(TAG, "Background music started: $musicId (loop=$loop)")
                                }

                                setOnErrorListener { player, what, extra ->
                                    Log.e(TAG, "BGM error: what=$what, extra=$extra")
                                    stopBackgroundMusic()
                                    true
                                }

                                setOnCompletionListener {
                                    if (!loop) {
                                        // Non-looping music finished
                                        bgmPlayer = null
                                        currentBgmTrack = null
                                    }
                                }

                                prepareAsync()
                            }
                    }

                    // Save current music track
                    prefs.edit().putString(KEY_CURRENT_MUSIC, musicId).apply()
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to play background music: $musicId", e)
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error playing BGM: $musicId", e)
                }
            }
        }

        /**
         * Stop background music
         */
        fun stopBackgroundMusic() {
            bgmPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                    Log.d(TAG, "Background music stopped")
                } catch (e: Exception) {
                    Log.w(TAG, "Error stopping BGM", e)
                } finally {
                    bgmPlayer = null
                    currentBgmTrack = null
                    bgmPausedPosition = 0
                }
            }
        }

        /**
         * Pause background music (can be resumed)
         */
        fun pauseBackgroundMusic() {
            bgmPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        bgmPausedPosition = player.currentPosition
                        player.pause()
                        Log.d(TAG, "Background music paused at $bgmPausedPosition")
                    } else {
                        Log.d(TAG, "Background music not playing, nothing to pause")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error pausing BGM", e)
                }
            }
        }

        /**
         * Resume background music
         */
        fun resumeBackgroundMusic() {
            if (!musicEnabled || bgmPlayer == null) {
                return
            }

            bgmPlayer?.let { player ->
                try {
                    if (!player.isPlaying) {
                        player.start()
                        Log.d(TAG, "Background music resumed")
                    } else {
                        Log.d(TAG, "Background music already playing")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error resuming BGM", e)
                    // Try to restart if resume fails
                    currentBgmTrack?.let { track ->
                        playBackgroundMusic(track, bgmIsLooping)
                    }
                }
            }
        }

        // =====================================================
        // Volume Control
        // =====================================================

        /**
         * Set master volume (affects both SFX and music)
         *
         * @param volume Volume level (0.0 - 1.0)
         */
        fun setMasterVolume(volume: Float) {
            val newVolume = volume.coerceIn(MIN_VOLUME, MAX_VOLUME)
            masterVolume = newVolume
            prefs.edit().putFloat(KEY_MASTER_VOLUME, newVolume).apply()

            // Update BGM volume immediately
            updateBgmVolume()

            Log.d(TAG, "Master volume set to: $newVolume")
        }

        /**
         * Set sound effect volume
         *
         * @param volume Volume level (0.0 - 1.0)
         */
        fun setSoundEffectVolume(volume: Float) {
            val newVolume = volume.coerceIn(MIN_VOLUME, MAX_VOLUME)
            sfxVolume = newVolume
            prefs.edit().putFloat(KEY_SFX_VOLUME, newVolume).apply()
            Log.d(TAG, "SFX volume set to: $newVolume")
        }

        /**
         * Set music volume
         *
         * @param volume Volume level (0.0 - 1.0)
         */
        fun setMusicVolume(volume: Float) {
            val newVolume = volume.coerceIn(MIN_VOLUME, MAX_VOLUME)
            musicVolume = newVolume
            prefs.edit().putFloat(KEY_MUSIC_VOLUME, newVolume).apply()
            updateBgmVolume()
            Log.d(TAG, "Music volume set to: $newVolume")
        }

        /**
         * Get current master volume
         */
        fun getMasterVolume(): Float = masterVolume

        /**
         * Get current sound effect volume
         */
        fun getSoundEffectVolume(): Float = sfxVolume

        /**
         * Get current music volume
         */
        fun getMusicVolume(): Float = musicVolume

        // =====================================================
        // Enable/Disable Control
        // =====================================================

        /**
         * Set sound effects enabled state
         *
         * @param enabled true to enable, false to disable
         */
        fun setSoundEnabled(enabled: Boolean) {
            soundEnabled = enabled
            prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
            Log.d(TAG, "Sound ${if (enabled) "enabled" else "disabled"}")
        }

        /**
         * Set music enabled state
         *
         * @param enabled true to enable, false to disable
         */
        fun setMusicEnabled(enabled: Boolean) {
            musicEnabled = enabled
            prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()

            if (!enabled) {
                stopBackgroundMusic()
            } else if (currentBgmTrack != null) {
                // Resume music if re-enabled
                playBackgroundMusic(currentBgmTrack!!, bgmIsLooping)
            }

            Log.d(TAG, "Music ${if (enabled) "enabled" else "disabled"}")
        }

        /**
         * Check if sound effects are enabled
         */
        fun isSoundEnabled(): Boolean = soundEnabled

        /**
         * Check if music is enabled
         */
        fun isMusicEnabled(): Boolean = musicEnabled

        // =====================================================
        // Lifecycle Management
        // =====================================================

        /**
         * Pause all audio (BGM + SFX)
         * Call when app goes to background
         */
        fun pauseAll() {
            pauseBackgroundMusic()

            // Release all SFX players
            sfxPlayers.forEach { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing SFX player", e)
                }
            }
            sfxPlayers.clear()

            // Stop word pronunciation audio
            audioAssetManager.stopAudio()

            Log.d(TAG, "All audio paused")
        }

        /**
         * Resume all audio
         * Call when app returns from background
         */
        fun resumeAll() {
            resumeBackgroundMusic()
            Log.d(TAG, "All audio resumed")
        }

        /**
         * Release all resources
         * Call when SoundManager is no longer needed
         */
        fun release() {
            stopBackgroundMusic()

            // Release all SFX players
            sfxPlayers.forEach { player ->
                try {
                    player.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing SFX player", e)
                }
            }
            sfxPlayers.clear()

            // Release audio asset manager
            audioAssetManager.onDestroy()

            Log.d(TAG, "SoundManager released")
        }

        // =====================================================
        // Private Helper Methods
        // =====================================================

        /**
         * Play a sound effect from file
         *
         * @param path Asset path to the audio file
         */
        private suspend fun playSfxFromFile(path: String) =
            suspendCoroutine { continuation ->
                try {
                    // Clean up finished players
                    cleanupFinishedPlayers()

                    // Check player pool limit
                    if (sfxPlayers.size >= MAX_CONCURRENT_SFX) {
                        Log.d(TAG, "SFX pool full, skipping sound: $path")
                        continuation.resume(Unit)
                        return@suspendCoroutine
                    }

                    // Create new MediaPlayer for this SFX
                    val assetDescriptor = context.assets.openFd(path)
                    val player =
                        MediaPlayer().apply {
                            setDataSource(
                                assetDescriptor.fileDescriptor,
                                assetDescriptor.startOffset,
                                assetDescriptor.length,
                            )
                            assetDescriptor.close()

                            // Set SFX volume
                            val effectiveVolume = masterVolume * sfxVolume
                            setVolume(effectiveVolume, effectiveVolume)

                            setOnPreparedListener { mp ->
                                mp.start()
                            }

                            setOnCompletionListener {
                                // Mark for cleanup (will be removed on next play)
                            }

                            setOnErrorListener { _, _, _ ->
                                // Mark for cleanup
                                true
                            }

                            prepareAsync()
                        }

                    sfxPlayers.add(player)
                    Log.d(TAG, "SFX playing: $path (active: ${sfxPlayers.size})")
                    continuation.resume(Unit)
                } catch (e: IOException) {
                    Log.w(TAG, "Failed to load SFX: $path, will use system sound", e)
                    // Resume with exception to trigger fallback in playSoundEffect
                    continuation.resumeWith(Result.failure(e))
                } catch (e: Exception) {
                    Log.w(TAG, "Unexpected error playing SFX: $path", e)
                    continuation.resumeWith(Result.failure(e))
                }
            }

        /**
         * Clean up finished SFX players
         */
        private fun cleanupFinishedPlayers() {
            val iterator = sfxPlayers.iterator()
            var removed = 0
            while (iterator.hasNext()) {
                val player = iterator.next()
                try {
                    if (!player.isPlaying) {
                        player.release()
                        iterator.remove()
                        removed++
                    }
                } catch (e: Exception) {
                    // Player is in invalid state, remove it
                    try {
                        player.release()
                    } catch (_: Exception) {
                    }
                    iterator.remove()
                    removed++
                }
            }
            if (removed > 0) {
                Log.d(TAG, "Cleaned up $removed finished SFX players")
            }
        }

        /**
         * Update BGM volume based on master and music volumes
         */
        private fun updateBgmVolume() {
            bgmPlayer?.let { player ->
                val effectiveVolume = masterVolume * musicVolume
                player.setVolume(effectiveVolume, effectiveVolume)
                Log.d(TAG, "BGM volume updated to: $effectiveVolume")
            }
        }

        /**
         * Check if background music is currently playing
         */
        fun isBgmPlaying(): Boolean {
            return try {
                bgmPlayer?.isPlaying == true
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Get current background music track
         */
        fun getCurrentBgmTrack(): String? = currentBgmTrack
    }
