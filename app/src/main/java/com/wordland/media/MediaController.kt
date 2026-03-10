package com.wordland.media

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * MediaController for audio playback using Android MediaPlayer.
 *
 * Supports playing MP3 files from res/raw/ with resource IDs.
 * Provides playback controls: play, pause, reset, seekTo.
 *
 * Thread-safe: All callbacks run on the main thread.
 *
 * @property context Application context for resource access
 */
class MediaController(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentResourceId: Int = 0
    private var isPrepared = false

    // Callbacks
    var onCompletionListener: (() -> Unit)? = null
    var onErrorListener: ((what: Int, extra: Int) -> Unit)? = null
    var onPreparedListener: (() -> Unit)? = null

    /**
     * Play an audio resource from res/raw/
     *
     * @param resourceId Raw resource ID (e.g., R.raw.word_apple)
     * @throws IOException if resource cannot be loaded
     */
    @Throws(IOException::class)
    fun play(resourceId: Int) {
        release()
        currentResourceId = resourceId
        isPrepared = false

        try {
            mediaPlayer =
                MediaPlayer().apply {
                    setOnPreparedListener { mp ->
                        isPrepared = true
                        mainHandler.post { onPreparedListener?.invoke() }
                        mp.start()
                    }

                    setOnCompletionListener {
                        mainHandler.post { onCompletionListener?.invoke() }
                    }

                    setOnErrorListener { _, what, extra ->
                        mainHandler.post { onErrorListener?.invoke(what, extra) }
                        true // Consume the error
                    }

                    val afd = context.resources.openRawResourceFd(resourceId)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()

                    prepareAsync()
                }
        } catch (e: IOException) {
            Log.e("MediaController", "Failed to load audio resource: $resourceId", e)
            release()
            throw e
        } catch (e: Exception) {
            Log.e("MediaController", "Unexpected error playing audio: $resourceId", e)
            release()
            throw IOException("Failed to play audio", e)
        }
    }

    /**
     * Pause the current playback.
     * Can be resumed with [play] or [start].
     */
    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        } catch (e: IllegalStateException) {
            Log.w("MediaController", "MediaPlayer in invalid state for pause", e)
        }
    }

    /**
     * Resume playback if paused.
     */
    fun start() {
        try {
            mediaPlayer?.let {
                if (isPrepared && !it.isPlaying) {
                    it.start()
                }
            }
        } catch (e: IllegalStateException) {
            Log.w("MediaController", "MediaPlayer in invalid state for start", e)
        }
    }

    /**
     * Stop and reset the playback to the beginning.
     * Resources are released and must be reloaded.
     */
    fun reset() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
            }
            isPrepared = false
        } catch (e: IllegalStateException) {
            Log.w("MediaController", "MediaPlayer in invalid state for reset", e)
        }
    }

    /**
     * Stop playback and release all resources.
     * Call this when done with the MediaController.
     */
    fun release() {
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.w("MediaController", "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
            isPrepared = false
        }
    }

    /**
     * Seek to a specific position in the current audio.
     *
     * @param positionMs Position in milliseconds
     */
    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.let {
                if (isPrepared) {
                    it.seekTo(positionMs)
                }
            }
        } catch (e: IllegalStateException) {
            Log.w("MediaController", "MediaPlayer in invalid state for seekTo", e)
        }
    }

    /**
     * Check if audio is currently playing.
     */
    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: IllegalStateException) {
            false
        }
    }

    /**
     * Get current playback position in milliseconds.
     */
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: IllegalStateException) {
            0
        }
    }

    /**
     * Get total duration of the current audio in milliseconds.
     */
    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: IllegalStateException) {
            0
        }
    }

    /**
     * Set the volume for this media player.
     *
     * @param leftVolume Left volume (0.0 to 1.0)
     * @param rightVolume Right volume (0.0 to 1.0)
     */
    fun setVolume(
        leftVolume: Float,
        rightVolume: Float,
    ) {
        try {
            mediaPlayer?.setVolume(leftVolume.coerceIn(0f, 1f), rightVolume.coerceIn(0f, 1f))
        } catch (e: IllegalStateException) {
            Log.w("MediaController", "MediaPlayer in invalid state for setVolume", e)
        }
    }

    /**
     * Check if the MediaPlayer is prepared and ready.
     */
    fun isPrepared(): Boolean = isPrepared

    /**
     * Get the current resource ID being played.
     */
    fun getCurrentResourceId(): Int = currentResourceId

    /**
     * Suspend function to play audio and wait for completion.
     * Use with coroutines for sequential audio playback.
     *
     * @param resourceId Raw resource ID
     * @return true if completed successfully, false if error occurred
     */
    suspend fun playAndWait(resourceId: Int): Boolean =
        suspendCancellableCoroutine { continuation ->
            val wasPlaying = isPlaying()
            val previousPosition = if (wasPlaying) getCurrentPosition() else 0
            val previousResourceId = currentResourceId

            try {
                onErrorListener = { what, extra ->
                    Log.e("MediaController", "Playback error: what=$what, extra=$extra")
                    continuation.resume(false)
                }

                onCompletionListener = {
                    continuation.resume(true)
                }

                play(resourceId)
            } catch (e: Exception) {
                Log.e("MediaController", "Failed to play audio", e)
                continuation.resumeWithException(e)
            }

            continuation.invokeOnCancellation {
                release()
                // Restore previous state if cancelled
                if (wasPlaying && previousResourceId != 0) {
                    try {
                        play(previousResourceId)
                        seekTo(previousPosition)
                    } catch (e: Exception) {
                        Log.w("MediaController", "Failed to restore previous audio", e)
                    }
                }
            }
        }

    /**
     * Loop playback with a specified delay between repetitions.
     *
     * @param resourceId Raw resource ID
     * @param repeatCount Number of times to repeat (0 = infinite)
     * @param delayMs Delay between repetitions in milliseconds
     */
    fun playWithLoop(
        resourceId: Int,
        repeatCount: Int = 0,
        delayMs: Long = 500,
    ) {
        var remainingRepeats = repeatCount

        onCompletionListener = {
            if (remainingRepeats != 0) {
                if (remainingRepeats > 0) remainingRepeats--
                mainHandler.postDelayed({
                    try {
                        play(resourceId)
                    } catch (e: Exception) {
                        Log.e("MediaController", "Failed to loop audio", e)
                    }
                }, delayMs)
            }
        }

        play(resourceId)
    }
}

/**
 * Factory function for creating MediaController instances.
 */
fun createMediaController(context: Context): MediaController {
    return MediaController(context.applicationContext)
}
