package com.wordland.data.assets

/**
 * Asset specifications for Wordland content
 * Defines requirements for all media assets
 */

object AudioAssetSpec {
    // Format specifications
    const val FORMAT = "mp3"
    const val SAMPLE_RATE = 44100 // Hz
    const val BITRATE = 128 // kbps
    const val CHANNELS = "mono" // Mono for voice clarity

    // Duration targets (milliseconds)
    const val DURATION_MIN = 500 // 0.5 seconds
    const val DURATION_MAX = 2000 // 2 seconds
    const val DURATION_TARGET = 1200 // 1.2 seconds average

    // Volume normalization
    const val TARGET_DB = -16.0 // LUFS (loudness target)
    const val MAX_PEAK_DB = -1.0 // Headroom

    // File naming convention
    const val NAMING_PATTERN = "{word}.mp3" // lowercase, no spaces
    const val PATH_PATTERN = "assets/audio/{filename}"

    // Total required
    const val WORD_COUNT_LOOK_ISLAND = 60
}

object ImageAssetSpec {
    // Format specifications
    const val FORMAT = "png" // PNG for transparency support
    const val ALTERNATIVE_FORMAT = "jpg" // JPG for photos without alpha

    // Resolution (for 1080p target)
    const val SCREEN_WIDTH = 1080
    const val SCREEN_HEIGHT = 1920 // Portrait orientation

    // Island card images
    const val ISLAND_CARD_WIDTH = 800
    const val ISLAND_CARD_HEIGHT = 600
    const val ISLAND_THUMBNAIL_WIDTH = 400
    const val ISLAND_THUMBNAIL_HEIGHT = 300

    // Level preview images
    const val LEVEL_PREVIEW_WIDTH = 600
    const val LEVEL_PREVIEW_HEIGHT = 400

    // Background images
    const val BACKGROUND_WIDTH = 1080
    const val BACKGROUND_HEIGHT = 1920

    // Icon/UI assets
    const val ICON_SIZE = 96 // dp
    const val BUTTON_ICON_SIZE = 48 // dp

    // File naming conventions
    const val ISLAND_PATTERN = "island_{island_id}.{format}"
    const val LEVEL_PATTERN = "level_{level_id}.{format}"
    const val BACKGROUND_PATTERN = "bg_{scene_id}.{format}"
    const val ICON_PATTERN = "icon_{name}.{format}"

    // Compression
    const val PNG_COMPRESSION = 9 // 0-9 (9 = max compression, slowest)
    const val JPG_QUALITY = 85 // 0-100 (85 = good balance)

    // Color depth
    const val BIT_DEPTH = 32 // ARGB_8888
}

/**
 * Asset checklist for Look Island
 */
object LookIslandAssetChecklist {
    // Audio files (60 words)
    val audioFiles =
        listOf(
            // Level 1 (6 files)
            "look", "see", "watch", "eye", "glass", "find",
            // Level 2 (6 files)
            "color", "red", "blue", "dark", "light", "bright",
            // Level 3 (6 files)
            "stare", "notice", "observe", "appear", "view", "scene",
            // Level 4 (6 files)
            "big", "small", "tall", "short", "near", "far",
            // Level 5 (6 files)
            "round", "square", "straight", "beautiful", "ugly", "clean",
            // Level 6 (6 files)
            "front", "back", "side", "top", "bottom", "corner",
            // Level 7 (6 files)
            "picture", "photo", "camera", "film", "television", "screen",
            // Level 8 (6 files)
            "clear", "strange", "different", "same", "real", "false",
            // Level 9 (6 files)
            "street", "shop", "market", "window", "wall", "mirror",
            // Level 10 (6 files)
            "examine", "search", "discover", "focus", "blind", "vision",
        )

    // Image files required
    val imageAssets =
        listOf(
            // Island map illustration
            "island_look_island.png",
            // Level previews (10 levels)
            "level_look_island_level_01.png",
            "level_look_island_level_02.png",
            "level_look_island_level_03.png",
            "level_look_island_level_04.png",
            "level_look_island_level_05.png",
            "level_look_island_level_06.png",
            "level_look_island_level_07.png",
            "level_look_island_level_08.png",
            "level_look_island_level_09.png",
            "level_look_island_level_10.png",
            // Backgrounds (3 scenes)
            "bg_look_island.png", // Island overview
            "bg_look_classroom.png", // Learning environment
            "bg_look_nature.png", // Outdoor scene
        )

    /**
     * Generate checklist file for content team
     */
    fun generateChecklist(): String {
        return """
            === WORDLAND - LOOK ISLAND ASSET CHECKLIST ===

            AUDIO FILES (60 total)
            Format: MP3, 44.1kHz, Mono, 128kbps
            Target Duration: 0.5s - 2s per word
            Volume: -16 LUFS normalized

            Level 1 - Basic Verbs:
              [ ] look.mp3
              [ ] see.mp3
              [ ] watch.mp3
              [ ] eye.mp3
              [ ] glass.mp3
              [ ] find.mp3

            Level 2 - Colors:
              [ ] color.mp3
              [ ] red.mp3
              [ ] blue.mp3
              [ ] dark.mp3
              [ ] light.mp3
              [ ] bright.mp3

            Level 3 - Gaze:
              [ ] stare.mp3
              [ ] notice.mp3
              [ ] observe.mp3
              [ ] appear.mp3
              [ ] view.mp3
              [ ] scene.mp3

            Level 4 - Size/Distance:
              [ ] big.mp3
              [ ] small.mp3
              [ ] tall.mp3
              [ ] short.mp3
              [ ] near.mp3
              [ ] far.mp3

            Level 5 - Shapes:
              [ ] round.mp3
              [ ] square.mp3
              [ ] straight.mp3
              [ ] beautiful.mp3
              [ ] ugly.mp3
              [ ] clean.mp3

            Level 6 - Positions:
              [ ] front.mp3
              [ ] back.mp3
              [ ] side.mp3
              [ ] top.mp3
              [ ] bottom.mp3
              [ ] corner.mp3

            Level 7 - Media:
              [ ] picture.mp3
              [ ] photo.mp3
              [ ] camera.mp3
              [ ] film.mp3
              [ ] television.mp3
              [ ] screen.mp3

            Level 8 - Description:
              [ ] clear.mp3
              [ ] strange.mp3
              [ ] different.mp3
              [ ] same.mp3
              [ ] real.mp3
              [ ] false.mp3

            Level 9 - Places:
              [ ] street.mp3
              [ ] shop.mp3
              [ ] market.mp3
              [ ] window.mp3
              [ ] wall.mp3
              [ ] mirror.mp3

            Level 10 - Advanced:
              [ ] examine.mp3
              [ ] search.mp3
              [ ] discover.mp3
              [ ] focus.mp3
              [ ] blind.mp3
              [ ] vision.mp3


            IMAGE FILES (14 total)
            Format: PNG (with transparency) or JPG (photos)
            Resolution: 1080x1920 (portrait), scaled as needed

            Island Assets:
              [ ] island_look_island.png (800x600 for card)

            Level Preview Images:
              [ ] level_look_island_level_01.png (600x400)
              [ ] level_look_island_level_02.png (600x400)
              [ ] level_look_island_level_03.png (600x400)
              [ ] level_look_island_level_04.png (600x400)
              [ ] level_look_island_level_05.png (600x400)
              [ ] level_look_island_level_06.png (600x400)
              [ ] level_look_island_level_07.png (600x400)
              [ ] level_look_island_level_08.png (600x400)
              [ ] level_look_island_level_09.png (600x400)
              [ ] level_look_island_level_10.png (600x400)

            Background Scenes:
              [ ] bg_look_island.png (1080x1920)
              [ ] bg_look_classroom.png (1080x1920)
              [ ] bg_look_nature.png (1080x1920)


            DELIVERY INSTRUCTIONS:
            1. Place audio files in: app/src/main/assets/audio/
            2. Place image files in: app/src/main/assets/images/
            3. Use lowercase filenames only
            4. Follow naming conventions exactly as listed
            5. Test all audio files in app before submission
            6. Verify image resolution and quality

            TOTAL ASSETS: 74 files (60 audio + 14 images)
            """.trimIndent()
    }
}
