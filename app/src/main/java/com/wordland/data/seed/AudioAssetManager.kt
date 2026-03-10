package com.wordland.data.assets

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for audio playback in Wordland
 */
@Singleton
class AudioAssetManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private var mediaPlayer: MediaPlayer? = null
        private var currentAudioPath: String? = null

        /**
         * Play audio file for a word
         *
         * @param wordId Word ID (e.g., "look_001")
         * @param onComplete Callback when audio finishes
         */
        suspend fun playWordAudio(
            wordId: String,
            onComplete: () -> Unit = {},
        ): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    // Extract word name from ID
                    val wordName = wordId.substring(5) // "look_001" → "001"
                    val audioFileName = "$wordName.mp3"
                    val audioPath = "audio/$audioFileName"

                    // Check if asset exists
                    val assetDescriptor = context.assets.openFd(audioPath)

                    // Release previous player
                    releasePlayer()

                    // Create new player
                    mediaPlayer =
                        MediaPlayer().apply {
                            setDataSource(assetDescriptor.fileDescriptor)
                            assetDescriptor.close()
                            prepareAsync()

                            setOnPreparedListener {
                                start()
                            }

                            setOnCompletionListener {
                                onComplete()
                                releasePlayer()
                            }

                            setOnErrorListener { _, _, _ ->
                                onComplete()
                                releasePlayer()
                                true
                            }
                        }

                    currentAudioPath = audioPath

                    Result.success(Unit)
                } catch (e: IOException) {
                    Result.failure(e)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Play audio by filename directly
         *
         * @param filename Audio filename (e.g., "look.mp3")
         */
        suspend fun playAudioFile(
            filename: String,
            onComplete: () -> Unit = {},
        ): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val audioPath = "audio/$filename"

                    // Check if asset exists
                    val assetDescriptor = context.assets.openFd(audioPath)

                    // Release previous player
                    releasePlayer()

                    // Create new player
                    mediaPlayer =
                        MediaPlayer().apply {
                            setDataSource(assetDescriptor.fileDescriptor)
                            assetDescriptor.close()
                            prepareAsync()

                            setOnPreparedListener {
                                start()
                            }

                            setOnCompletionListener {
                                onComplete()
                                releasePlayer()
                            }

                            setOnErrorListener { _, _, _ ->
                                onComplete()
                                releasePlayer()
                                true
                            }
                        }

                    currentAudioPath = audioPath

                    Result.success(Unit)
                } catch (e: IOException) {
                    Result.failure(e)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Stop current audio playback
         */
        fun stopAudio() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                releasePlayer()
            }
        }

        /**
         * Check if audio is currently playing
         */
        fun isPlaying(): Boolean {
            return mediaPlayer?.isPlaying == true
        }

        /**
         * Release MediaPlayer resources
         */
        private fun releasePlayer() {
            mediaPlayer?.release()
            mediaPlayer = null
            currentAudioPath = null
        }

        /**
         * Check if audio file exists for a word
         *
         * @param wordId Word ID to check
         * @return true if audio file exists in assets
         */
        fun audioExists(wordId: String): Boolean {
            return try {
                val wordName = wordId.substring(5)
                val audioFileName = "$wordName.mp3"
                val audioPath = "audio/$audioFileName"
                context.assets.open(audioPath).close()
                true
            } catch (e: IOException) {
                false
            }
        }

        /**
         * Get list of all available audio files
         *
         * @return List of audio filenames (without path)
         */
        fun getAvailableAudioFiles(): List<String> {
            return try {
                context.assets.list("audio")?.toList() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        /**
         * Call when activity/fragment is destroyed
         */
        fun onDestroy() {
            releasePlayer()
        }
    }
