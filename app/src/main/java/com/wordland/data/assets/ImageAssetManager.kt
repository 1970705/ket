package com.wordland.data.assets
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for image asset loading in Wordland
 */
@Singleton
class ImageAssetManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * Load image asset as Bitmap
         *
         * @param filename Image filename (e.g., "island_look_island.png")
         * @param targetWidth Optional target width for scaling
         * @param targetHeight Optional target height for scaling
         * @return Bitmap result or failure
         */
        suspend fun loadImage(
            filename: String,
            targetWidth: Int? = null,
            targetHeight: Int? = null,
        ): Result<Bitmap> =
            withContext(Dispatchers.IO) {
                try {
                    val imagePath = "images/$filename"

                    // Load bitmap from assets
                    val assetStream = context.assets.open(imagePath)
                    val options =
                        BitmapFactory.Options().apply {
                            inScaled = true
                            inDensity = context.resources.displayMetrics.densityDpi
                        }

                    val bitmap = BitmapFactory.decodeStream(assetStream, null, options)
                    assetStream.close()

                    if (bitmap == null) {
                        return@withContext Result.failure(
                            IOException("Failed to decode bitmap: $filename"),
                        )
                    }

                    // Scale if target dimensions provided
                    val scaledBitmap =
                        if (targetWidth != null && targetHeight != null) {
                            Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                        } else {
                            bitmap
                        }

                    Result.success(scaledBitmap)
                } catch (e: IOException) {
                    Result.failure(e)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Load island card image
         *
         * @param islandId Island ID (e.g., "look_island")
         * @return Bitmap result
         */
        suspend fun loadIslandImage(islandId: String): Result<Bitmap> {
            val filename = "island_$islandId.png"
            return loadImage(
                filename = filename,
                targetWidth = ImageAssetSpec.ISLAND_CARD_WIDTH,
                targetHeight = ImageAssetSpec.ISLAND_CARD_HEIGHT,
            )
        }

        /**
         * Load level preview image
         *
         * @param levelId Level ID (e.g., "look_island_level_01")
         * @return Bitmap result
         */
        suspend fun loadLevelImage(levelId: String): Result<Bitmap> {
            val filename = "level_$levelId.png"
            return loadImage(
                filename = filename,
                targetWidth = ImageAssetSpec.LEVEL_PREVIEW_WIDTH,
                targetHeight = ImageAssetSpec.LEVEL_PREVIEW_HEIGHT,
            )
        }

        /**
         * Load background image
         *
         * @param sceneId Scene ID (e.g., "look_island")
         * @return Bitmap result
         */
        suspend fun loadBackgroundImage(sceneId: String): Result<Bitmap> {
            val filename = "bg_$sceneId.png"
            return loadImage(
                filename = filename,
                targetWidth = ImageAssetSpec.BACKGROUND_WIDTH,
                targetHeight = ImageAssetSpec.BACKGROUND_HEIGHT,
            )
        }

        /**
         * Check if image file exists
         *
         * @param filename Image filename to check
         * @return true if image file exists in assets
         */
        fun imageExists(filename: String): Boolean {
            return try {
                val imagePath = "images/$filename"
                context.assets.open(imagePath).close()
                true
            } catch (e: IOException) {
                false
            }
        }

        /**
         * Get list of all available image files
         *
         * @return List of image filenames (without path)
         */
        fun getAvailableImageFiles(): List<String> {
            return try {
                context.assets.list("images")?.toList() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

        /**
         * Get asset file size in bytes
         *
         * @param filename Asset filename with path (e.g., "images/photo.png")
         * @return File size in bytes or 0 if error
         */
        fun getAssetSize(filename: String): Long {
            return try {
                val assetStream = context.assets.open(filename)
                val size = assetStream.available().toLong()
                assetStream.close()
                size
            } catch (e: Exception) {
                0L
            }
        }
    }
