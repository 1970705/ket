package com.wordland.data.assets

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for AssetSpec configurations
 *
 * Tests asset specification constants and calculations
 */
@RunWith(JUnit4::class)
class AssetSpecTest {
    // Audio Asset Spec Tests
    @Test
    fun audioAssetSpec_formatConstants() {
        assertEquals("mp3", AudioAssetSpec.FORMAT)
        assertEquals(44100, AudioAssetSpec.SAMPLE_RATE)
        assertEquals(128, AudioAssetSpec.BITRATE)
        assertEquals("mono", AudioAssetSpec.CHANNELS)
    }

    @Test
    fun audioAssetSpec_durationTargets() {
        assertEquals(500, AudioAssetSpec.DURATION_MIN)
        assertEquals(2000, AudioAssetSpec.DURATION_MAX)
        assertEquals(1200, AudioAssetSpec.DURATION_TARGET)
    }

    @Test
    fun audioAssetSpec_volumeNormalization() {
        assertEquals(-16.0, AudioAssetSpec.TARGET_DB, 0.01)
        assertEquals(-1.0, AudioAssetSpec.MAX_PEAK_DB, 0.01)
    }

    @Test
    fun audioAssetSpec_wordCountForLookIsland() {
        assertEquals(60, AudioAssetSpec.WORD_COUNT_LOOK_ISLAND)
    }

    // Image Asset Spec Tests
    @Test
    fun imageAssetSpec_formatConstants() {
        assertEquals("png", ImageAssetSpec.FORMAT)
        assertEquals("jpg", ImageAssetSpec.ALTERNATIVE_FORMAT)
    }

    @Test
    fun imageAssetSpec_resolution() {
        assertEquals(1080, ImageAssetSpec.SCREEN_WIDTH)
        assertEquals(1920, ImageAssetSpec.SCREEN_HEIGHT)
    }

    @Test
    fun imageAssetSpec_islandCardSizes() {
        assertEquals(800, ImageAssetSpec.ISLAND_CARD_WIDTH)
        assertEquals(600, ImageAssetSpec.ISLAND_CARD_HEIGHT)
        assertEquals(400, ImageAssetSpec.ISLAND_THUMBNAIL_WIDTH)
        assertEquals(300, ImageAssetSpec.ISLAND_THUMBNAIL_HEIGHT)
    }

    @Test
    fun imageAssetSpec_levelPreviewSizes() {
        assertEquals(600, ImageAssetSpec.LEVEL_PREVIEW_WIDTH)
        assertEquals(400, ImageAssetSpec.LEVEL_PREVIEW_HEIGHT)
    }

    @Test
    fun imageAssetSpec_backgroundSizes() {
        assertEquals(1080, ImageAssetSpec.BACKGROUND_WIDTH)
        assertEquals(1920, ImageAssetSpec.BACKGROUND_HEIGHT)
    }

    @Test
    fun imageAssetSpec_iconSizes() {
        assertEquals(96, ImageAssetSpec.ICON_SIZE)
        assertEquals(48, ImageAssetSpec.BUTTON_ICON_SIZE)
    }

    @Test
    fun imageAssetSpec_compression() {
        assertEquals(9, ImageAssetSpec.PNG_COMPRESSION)
        assertEquals(85, ImageAssetSpec.JPG_QUALITY)
    }

    @Test
    fun imageAssetSpec_bitDepth() {
        assertEquals(32, ImageAssetSpec.BIT_DEPTH)
    }

    // Look Island Asset Checklist Tests
    @Test
    fun lookIslandAssetChecklist_audioFileCount() {
        assertEquals(60, LookIslandAssetChecklist.audioFiles.size)
    }

    @Test
    fun lookIslandAssetChecklist_containsLevel1Words() {
        assertTrue(LookIslandAssetChecklist.audioFiles.contains("look"))
        assertTrue(LookIslandAssetChecklist.audioFiles.contains("see"))
        assertTrue(LookIslandAssetChecklist.audioFiles.contains("watch"))
    }

    @Test
    fun lookIslandAssetChecklist_containsLevel2Words() {
        assertTrue(LookIslandAssetChecklist.audioFiles.contains("color"))
        assertTrue(LookIslandAssetChecklist.audioFiles.contains("red"))
        assertTrue(LookIslandAssetChecklist.audioFiles.contains("blue"))
    }

    @Test
    fun lookIslandAssetChecklist_imageAssetCount() {
        assertEquals(14, LookIslandAssetChecklist.imageAssets.size)
    }

    @Test
    fun lookIslandAssetChecklist_containsIslandImage() {
        assertTrue(LookIslandAssetChecklist.imageAssets.contains("island_look_island.png"))
    }

    @Test
    fun lookIslandAssetChecklist_containsLevelPreviews() {
        assertTrue(LookIslandAssetChecklist.imageAssets.contains("level_look_island_level_01.png"))
        assertTrue(LookIslandAssetChecklist.imageAssets.contains("level_look_island_level_05.png"))
        assertTrue(LookIslandAssetChecklist.imageAssets.contains("level_look_island_level_10.png"))
    }

    @Test
    fun lookIslandAssetChecklist_containsBackgrounds() {
        assertTrue(LookIslandAssetChecklist.imageAssets.contains("bg_look_island.png"))
        assertTrue(LookIslandAssetChecklist.imageAssets.contains("bg_look_classroom.png"))
        assertTrue(LookIslandAssetChecklist.imageAssets.contains("bg_look_nature.png"))
    }

    @Test
    fun lookIslandAssetChecklist_totalAssetCount() {
        val totalAssets = LookIslandAssetChecklist.audioFiles.size + LookIslandAssetChecklist.imageAssets.size
        assertEquals(74, totalAssets)
    }

    @Test
    fun lookIslandAssetChecklist_generateChecklistIsNotEmpty() {
        val checklist = LookIslandAssetChecklist.generateChecklist()
        assertNotNull(checklist)
        assertTrue(checklist.length > 0)
    }

    @Test
    fun lookIslandAssetChecklist_checklistContainsRequiredSections() {
        val checklist = LookIslandAssetChecklist.generateChecklist()
        assertTrue(checklist.contains("AUDIO FILES"))
        assertTrue(checklist.contains("IMAGE FILES"))
        assertTrue(checklist.contains("DELIVERY INSTRUCTIONS"))
    }
}
