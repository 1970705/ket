package com.wordland.data.seed

import androidx.compose.ui.graphics.Color

/**
 * Complete asset checklist for Make Lake
 * 60 audio files + 14 image files = 74 total assets
 */
object MakeLakeAssetChecklist {
    /**
     * Audio Files Checklist (60 files)
     * Format: MP3, 44.1kHz, mono, 128kbps
     * Duration: 0.5-2s per word
     */
    val audioFiles =
        listOf(
            // Level 1: Basic Making
            AudioAsset("make.mp3", "/meɪk/", "make", "make_001"),
            AudioAsset("build.mp3", "/bɪld/", "build", "make_002"),
            AudioAsset("draw.mp3", "/drɔː/", "draw", "make_003"),
            AudioAsset("paint.mp3", "/peɪnt/", "paint", "make_004"),
            AudioAsset("write.mp3", "/raɪt/", "write", "make_005"),
            AudioAsset("create.mp3", "/kriˈeɪt/", "create", "make_006"),
            // Level 2: Cooking and Food
            AudioAsset("cook.mp3", "/kʊk/", "cook", "make_007"),
            AudioAsset("bake.mp3", "/beɪk/", "bake", "make_008"),
            AudioAsset("mix.mp3", "/mɪks/", "mix", "make_009"),
            AudioAsset("cut.mp3", "/kʌt/", "cut", "make_010"),
            AudioAsset("fix.mp3", "/fɪks/", "fix", "make_011"),
            AudioAsset("clean.mp3", "/kliːn/", "clean", "make_012"),
            // Level 3: Materials
            AudioAsset("wood.mp3", "/wʊd/", "wood", "make_013"),
            AudioAsset("metal.mp3", "/ˈmetl/", "metal", "make_014"),
            AudioAsset("plastic.mp3", "/ˈplæstɪk/", "plastic", "make_015"),
            AudioAsset("paper.mp3", "/ˈpeɪpər/", "paper", "make_016"),
            AudioAsset("glass.mp3", "/ɡlɑːs/", "glass", "make_017"),
            AudioAsset("stone.mp3", "/stəʊn/", "stone", "make_018"),
            // Level 4: Tools
            AudioAsset("hammer.mp3", "/ˈhæmər/", "hammer", "make_019"),
            AudioAsset("saw.mp3", "/sɔː/", "saw", "make_020"),
            AudioAsset("glue.mp3", "/ɡluː/", "glue", "make_021"),
            AudioAsset("scissors.mp3", "/ˈsɪzərz/", "scissors", "make_022"),
            AudioAsset("brush.mp3", "/brʌʃ/", "brush", "make_023"),
            AudioAsset("tool.mp3", "/tuːl/", "tool", "make_024"),
            // Level 5: Construction
            AudioAsset("house.mp3", "/haʊs/", "house", "make_025"),
            AudioAsset("wall.mp3", "/wɔːl/", "wall", "make_026"),
            AudioAsset("floor.mp3", "/flɔːr/", "floor", "make_027"),
            AudioAsset("roof.mp3", "/ruːf/", "roof", "make_028"),
            AudioAsset("door.mp3", "/dɔːr/", "door", "make_029"),
            AudioAsset("window.mp3", "/ˈwɪndəʊ/", "window", "make_030"),
            // Level 6: Arts and Crafts
            AudioAsset("art.mp3", "/ɑːt/", "art", "make_031"),
            AudioAsset("craft.mp3", "/krɑːft/", "craft", "make_032"),
            AudioAsset("design.mp3", "/dɪˈzaɪn/", "design", "make_033"),
            AudioAsset("shape.mp3", "/ʃeɪp/", "shape", "make_034"),
            AudioAsset("color.mp3", "/ˈkʌlər/", "color", "make_035"),
            AudioAsset("model.mp3", "/ˈmɒdl/", "model", "make_036"),
            // Level 7: Technology
            AudioAsset("machine.mp3", "/məˈʃiːn/", "machine", "make_037"),
            AudioAsset("robot.mp3", "/ˈrəʊbɒt/", "robot", "make_038"),
            AudioAsset("computer.mp3", "/kəmˈpjuːtər/", "computer", "make_039"),
            AudioAsset("invent.mp3", "/ɪnˈvent/", "invent", "make_040"),
            AudioAsset("electric.mp3", "/ɪˈlektrɪk/", "electric", "make_041"),
            AudioAsset("power.mp3", "/ˈpaʊər/", "power", "make_042"),
            // Level 8: Agriculture
            AudioAsset("grow.mp3", "/ɡrəʊ/", "grow", "make_043"),
            AudioAsset("plant.mp3", "/plɑːnt/", "plant", "make_044"),
            AudioAsset("farm.mp3", "/fɑːrm/", "farm", "make_045"),
            AudioAsset("garden.mp3", "/ˈɡɑːrdən/", "garden", "make_046"),
            AudioAsset("harvest.mp3", "/ˈhɑːrvɪst/", "harvest", "make_047"),
            AudioAsset("seed.mp3", "/siːd/", "seed", "make_048"),
            // Level 9: Manufacturing
            AudioAsset("factory.mp3", "/ˈfæktəri/", "factory", "make_049"),
            AudioAsset("product.mp3", "/ˈprɒdʌkt/", "product", "make_050"),
            AudioAsset("produce.mp3", "/prəˈduːs/", "produce", "make_051"),
            AudioAsset("manufacture.mp3", "/ˌmænjʊˈfæktʃər/", "manufacture", "make_052"),
            AudioAsset("assemble.mp3", "/əˈsembl/", "assemble", "make_053"),
            AudioAsset("construction.mp3", "/kənˈstrʌkʃən/", "construction", "make_054"),
            // Level 10: Expert Creation
            AudioAsset("engineer.mp3", "/ˌendʒɪˈnɪər/", "engineer", "make_055"),
            AudioAsset("architect.mp3", "/ˈɑːrkɪtekt/", "architect", "make_056"),
            AudioAsset("invention.mp3", "/ɪnˈvenʃən/", "invention", "make_057"),
            AudioAsset("innovation.mp3", "/ˌɪnəˈveɪʃən/", "innovation", "make_058"),
            AudioAsset("craftsmanship.mp3", "/ˈkræftsmənʃɪp/", "craftsmanship", "make_059"),
            AudioAsset("workmanship.mp3", "/ˈwɜːrkmənʃɪp/", "workmanship", "make_060"),
        )

    /**
     * Image Files Checklist (14 files)
     * Format: PNG with transparency
     * Island Card: 800×600px
     * Level Previews: 600×400px
     * Backgrounds: 1080×1920px
     */
    val imageFiles =
        listOf(
            // Island Card (1 file)
            ImageAsset(
                filename = "island_make_lake.png",
                size = "800×600px",
                description = "Make Lake island card",
                visualElements =
                    listOf(
                        "Cyan lake with creation",
                        "Workshops and tools floating",
                        "Half-built structures visible",
                        "Characters building and making",
                        "Water reflection of projects",
                    ),
                colorPalette =
                    listOf(
                        "#26C6DA" to "Cyan (primary)",
                        "#0097A7" to "Dark cyan (shadows)",
                        "#B2EBF2" to "Light cyan (highlights)",
                    ),
            ),
            // Level Previews (10 files)
            ImageAsset(
                filename = "level_make_lake_01.png",
                size = "600×400px",
                description = "Level 1: Basic Making",
                visualElements =
                    listOf(
                        "Drawing and painting scenes",
                        "Simple construction with blocks",
                        "Kids making crafts",
                    ),
            ),
            ImageAsset(
                filename = "level_make_lake_02.png",
                size = "600×400px",
                description = "Level 2: Cooking and Food",
                visualElements =
                    listOf(
                        "Kitchen scenes",
                        "Cooking and baking activities",
                        "Food preparation",
                    ),
            ),
            ImageAsset(
                filename = "level_make_lake_03.png",
                size = "600×400px",
                description = "Level 3: Materials",
                visualElements =
                    listOf(
                        "Wood, stone, metal displays",
                        "Material samples",
                        "Resource gathering",
                    ),
            ),
            ImageAsset(
                filename = "level_make_lake_04.png",
                size = "600×400px",
                description = "Level 4: Tools",
                visualElements =
                    listOf(
                        "Tool workshops",
                        "Hammer, saw, glue visible",
                        "Construction activities",
                    ),
            ),
            ImageAsset(
                filename = "level_make_lake_05.png",
                size = "600×400px",
                description = "Level 5: Construction",
                visualElements =
                    listOf(
                        "House building scenes",
                        "Wall, door, window placement",
                        "Construction progress",
                    ),
            ),
            ImageAsset(
                filename = "level_make_lake_06.png",
                size = "600×400px",
                description = "Level 6: Arts and Crafts",
                visualElements =
                    listOf(
                        "Art studios",
                        "Craft tables with supplies",
                        "Creative expression",
                    ),
            ),
            ImageAsset(
                filename = "level_make_lake_07.png",
                size = "600×400px",
                description = "Level 7: Technology",
                visualElements =
                    listOf(
                        "Robot and machine building",
                        "Computer workshops",
                        "Invention labs",
                    ),
            ),
            ImageAsset(
                filename = "level_make_lake_08.png",
                size = "600×400px",
                description = "Level 8: Agriculture",
                visualElements =
                    listOf(
                        "Farm and garden scenes",
                        "Growing and harvesting",
                        "Nature's creations",
                    ),
            ),
            ImageAsset(
                filename = "level_make_lake_09.png",
                size = "600×400px",
                description = "Level 9: Manufacturing",
                visualElements =
                    listOf(
                        "Factory production lines",
                        "Assembly workshops",
                        "Mass production",
                    ),
            ),
            ImageAsset(
                filename = "level_make_lake_10.png",
                size = "600×400px",
                description = "Level 10: Master Creation",
                visualElements =
                    listOf(
                        "Lake of infinite creativity",
                        "All building mastered",
                        "Engineering achievement",
                        "Invention celebration",
                    ),
            ),
            // Backgrounds (3 files)
            ImageAsset(
                filename = "bg_make_lake.png",
                size = "1080×1920px",
                description = "Make Lake island overview",
                visualElements =
                    listOf(
                        "Serene cyan lake",
                        "Floating workshops",
                        "Building materials on shore",
                        "Projects in various stages",
                        "Infinite creative potential",
                    ),
            ),
            ImageAsset(
                filename = "bg_workshop.png",
                size = "1080×1920px",
                description = "Creative workshop",
                visualElements =
                    listOf(
                        "Well-lit workspace",
                        "Tools and materials organized",
                        "Workbenches with projects",
                        "Inspiring atmosphere",
                    ),
            ),
            ImageAsset(
                filename = "bg_factory.png",
                size = "1080×1920px",
                description = "Production facility",
                visualElements =
                    listOf(
                        "Modern factory setting",
                        "Assembly lines visible",
                        "Quality control stations",
                        "Professional environment",
                    ),
            ),
        )

    /**
     * Complete checklist for validation
     */
    val totalRequired = audioFiles.size + imageFiles.size // 74 assets

    fun getAudioChecklist(): List<AudioAsset> = audioFiles

    fun getImageChecklist(): List<ImageAsset> = imageFiles

    /**
     * Data classes for asset tracking
     */
    data class AudioAsset(
        val filename: String,
        val pronunciation: String,
        val word: String,
        val wordId: String,
    )

    data class ImageAsset(
        val filename: String,
        val size: String,
        val description: String,
        val visualElements: List<String> = emptyList(),
        val colorPalette: List<Pair<String, String>> = emptyList(),
    ) {
        fun getColors(): String {
            return if (colorPalette.isNotEmpty()) {
                colorPalette.joinToString(", ") { "${it.first}: ${it.second}" }
            } else {
                "Not specified"
            }
        }

        fun getVisuals(): String {
            return visualElements.joinToString(", ")
        }
    }

    /**
     * Color Palette for Make Lake
     */
    object MakeLakeColors {
        val Primary = Color(0xFF26C6DA) // Cyan
        val PrimaryDark = Color(0xFF0097A7) // Dark cyan
        val SecondaryLight = Color(0xFFB2EBF2) // Light cyan
        val SecondaryDark = Color(0xFF00838F) // Darker cyan
        val Accent = Color(0xFFFF9800) // Orange (contrast)
        val Background = Color(0xFFE0F7FA) // Light cyan bg
        val Surface = Color(0xFFFFFFFF) // White cards
        val Error = Color(0xFFF44336) // Red (errors)

        val Build = Color(0xFF4CAF50) // Green (building)
        val Create = Color(0xFFFFEB3B) // Yellow (creativity)
        val Material = Color(0xFF795548) // Brown (resources)
        val Tool = Color(0xFF607D8B) // Gray (tools)
    }

    /**
     * Art Style Guidelines for Make Lake
     */
    object ArtStyleGuidelines {
        val VISUAL_STYLE =
            """
            Creation-focused style:
            - Cyan/teal water palette
            - Workshops and building sites
            - Tools and materials prominent
            - Half-finished projects visible
            - Productive, energetic aesthetic
            - Clear construction visualizations
            """.trimIndent()

        val CHARACTER_DESIGN =
            """
            Busy creators:
            - Active making poses
            - Holding tools and materials
            - Focused, determined expressions
            - Props: Hammers, brushes, blueprints
            - Diverse crafting roles
            """.trimIndent()

        val BACKGROUND_CONSISTENCY =
            """
            Level 1-3: Bright shoreline (cyan tones)
            Level 4-6: Mixed indoor/outdoor
            Level 7-8: Factory/workshop (artificial light)
            Level 9-10: Lake sunset (golden reflection)
            """.trimIndent()

        val DELIVERY_INSTRUCTIONS =
            """
            1. Folder structure: app/src/main/assets/
            2. Audio: audio/{word}.mp3 (60 files)
            3. Images: images/{filename}.png (14 files)
            4. Total: 74 assets for Make Lake
            5. Naming convention strictly followed
            6. Color profile: sRGB IEC61966-2.1
            """.trimIndent()
    }

    /**
     * Import for Color class
     */
}
