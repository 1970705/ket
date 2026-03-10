package com.wordland.navigation

/**
 * Navigation routes for Wordland app
 */
object NavRoute {
    // Onboarding
    const val ONBOARDING_WELCOME = "onboarding_welcome"
    const val ONBOARDING_PET_SELECTION = "onboarding_pet_selection"
    const val ONBOARDING_TUTORIAL = "onboarding_tutorial"
    const val ONBOARDING_CHEST = "onboarding_chest"
    const val PET_SELECTION = "pet_selection"

    // Main screens
    const val HOME = "home"
    const val ISLAND_MAP = "island_map"
    const val LEVEL_SELECT = "level_select"

    // Gameplay
    const val LEARNING = "learning/{levelId}/{islandId}"
    const val MULTIPLE_CHOICE = "multiple_choice/{levelId}/{islandId}"
    const val FILL_BLANK = "fill_blank/{levelId}/{islandId}"
    const val QUICK_JUDGE = "quick_judge/{levelId}/{islandId}"
    const val MATCH_GAME = "match_game/{levelId}/{islandId}"
    const val REVIEW = "review"
    const val PRACTICE = "practice"

    // Progress
    const val PROGRESS = "progress"
    const val PROFILE = "profile"

    // Settings
    const val AUDIO_SETTINGS = "audio_settings"

    // Star Breakdown
    const val STAR_BREAKDOWN = "star_breakdown/{stars}/{accuracy}/{hintsUsed}/{timeTaken}/{errorCount}/{islandId}"

    // Helper function to create learning route
    fun learning(
        levelId: String,
        islandId: String,
    ): String {
        return "learning/$levelId/$islandId"
    }

    // Helper function to create multiple choice route
    fun multipleChoice(
        levelId: String,
        islandId: String,
    ): String {
        return "multiple_choice/$levelId/$islandId"
    }

    // Helper function to create fill blank route
    fun fillBlank(
        levelId: String,
        islandId: String,
    ): String {
        return "fill_blank/$levelId/$islandId"
    }

    // Helper function to create quick judge route
    fun quickJudge(
        levelId: String,
        islandId: String,
    ): String {
        return "quick_judge/$levelId/$islandId"
    }

    // Helper function to create match game route
    fun matchGame(
        levelId: String,
        islandId: String,
    ): String {
        return "match_game/$levelId/$islandId"
    }

    // Helper to extract parameters from learning route
    fun learningArgs(route: String): Pair<String, String>? {
        val regex = """learning/([^/]+)/([^/]+)""".toRegex()
        val match = regex.find(route) ?: return null
        return Pair(match.groupValues[1], match.groupValues[2])
    }

    // Helper to extract parameters from multiple choice route
    fun multipleChoiceArgs(route: String): Pair<String, String>? {
        val regex = """multiple_choice/([^/]+)/([^/]+)""".toRegex()
        val match = regex.find(route) ?: return null
        return Pair(match.groupValues[1], match.groupValues[2])
    }

    // Helper to extract parameters from fill blank route
    fun fillBlankArgs(route: String): Pair<String, String>? {
        val regex = """fill_blank/([^/]+)/([^/]+)""".toRegex()
        val match = regex.find(route) ?: return null
        return Pair(match.groupValues[1], match.groupValues[2])
    }

    // Helper to extract parameters from quick judge route
    fun quickJudgeArgs(route: String): Pair<String, String>? {
        val regex = """quick_judge/([^/]+)/([^/]+)""".toRegex()
        val match = regex.find(route) ?: return null
        return Pair(match.groupValues[1], match.groupValues[2])
    }

    // Helper function to create star breakdown route
    fun starBreakdown(
        stars: Int,
        accuracy: Int,
        hintsUsed: Int,
        timeTaken: Int,
        errorCount: Int,
        islandId: String,
    ): String {
        return "star_breakdown/$stars/$accuracy/$hintsUsed/$timeTaken/$errorCount/$islandId"
    }
}
