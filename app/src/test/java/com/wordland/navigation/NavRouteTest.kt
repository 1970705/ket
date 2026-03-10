package com.wordland.navigation

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for NavRoute navigation routes
 *
 * Tests route construction and parameter extraction
 */
@RunWith(JUnit4::class)
class NavRouteTest {
    @Test
    fun navRoute_constantsAreDefined() {
        assertEquals("pet_selection", NavRoute.PET_SELECTION)
        assertEquals("home", NavRoute.HOME)
        assertEquals("island_map", NavRoute.ISLAND_MAP)
        assertEquals("level_select", NavRoute.LEVEL_SELECT)
        assertEquals("learning/{levelId}/{islandId}", NavRoute.LEARNING)
        assertEquals("review", NavRoute.REVIEW)
        assertEquals("progress", NavRoute.PROGRESS)
        assertEquals("star_breakdown/{stars}/{accuracy}/{hintsUsed}/{timeTaken}/{errorCount}/{islandId}", NavRoute.STAR_BREAKDOWN)
    }

    @Test
    fun navRoute_learning_createsCorrectRoute() {
        val route = NavRoute.learning("level_01", "look_island")
        assertEquals("learning/level_01/look_island", route)
    }

    @Test
    fun navRoute_learning_withSpecialCharacters() {
        val route = NavRoute.learning("level_1-advanced", "make_lake")
        assertEquals("learning/level_1-advanced/make_lake", route)
    }

    @Test
    fun navRoute_multipleChoice_createsCorrectRoute() {
        val route = NavRoute.multipleChoice("level_01", "look_island")
        assertEquals("multiple_choice/level_01/look_island", route)
    }

    @Test
    fun navRoute_fillBlank_createsCorrectRoute() {
        val route = NavRoute.fillBlank("level_01", "look_island")
        assertEquals("fill_blank/level_01/look_island", route)
    }

    @Test
    fun navRoute_learningArgs_parsesValidRoute() {
        val route = "learning/level_01/look_island"
        val args = NavRoute.learningArgs(route)

        assertNotNull(args)
        assertEquals("level_01", args?.first)
        assertEquals("look_island", args?.second)
    }

    @Test
    fun navRoute_learningArgs_returnsNullForInvalidRoute() {
        val invalidRoute = "learning/level_01"
        val args = NavRoute.learningArgs(invalidRoute)

        assertNull(args)
    }

    @Test
    fun navRoute_learningArgs_returnsNullForMismatchedRoute() {
        val invalidRoute = "review/level_01/look_island"
        val args = NavRoute.learningArgs(invalidRoute)

        assertNull(args)
    }

    @Test
    fun navRoute_multipleChoiceArgs_parsesValidRoute() {
        val route = "multiple_choice/level_02/make_lake"
        val args = NavRoute.multipleChoiceArgs(route)

        assertNotNull(args)
        assertEquals("level_02", args?.first)
        assertEquals("make_lake", args?.second)
    }

    @Test
    fun navRoute_fillBlankArgs_parsesValidRoute() {
        val route = "fill_blank/level_03/listen_valley"
        val args = NavRoute.fillBlankArgs(route)

        assertNotNull(args)
        assertEquals("level_03", args?.first)
        assertEquals("listen_valley", args?.second)
    }

    @Test
    fun navRoute_extractLevelIdFromLearningRoute() {
        val route = NavRoute.learning("level_05", "look_island")
        val args = NavRoute.learningArgs(route)

        assertEquals("level_05", args?.first)
    }

    @Test
    fun navRoute_extractIslandIdFromLearningRoute() {
        val route = NavRoute.learning("level_05", "look_island")
        val args = NavRoute.learningArgs(route)

        assertEquals("look_island", args?.second)
    }

    @Test
    fun navRoute_learningWithUnderscoresInId() {
        val route = NavRoute.learning("look_island_level_01", "look_island")
        assertEquals("learning/look_island_level_01/look_island", route)

        val args = NavRoute.learningArgs(route)
        assertEquals("look_island_level_01", args?.first)
        assertEquals("look_island", args?.second)
    }

    @Test
    fun navRoute_allRoutesFollowPattern() {
        val learningRoute = NavRoute.learning("l1", "i1")
        val multipleChoiceRoute = NavRoute.multipleChoice("l2", "i2")
        val fillBlankRoute = NavRoute.fillBlank("l3", "i3")

        assertTrue(learningRoute.startsWith("learning/"))
        assertTrue(multipleChoiceRoute.startsWith("multiple_choice/"))
        assertTrue(fillBlankRoute.startsWith("fill_blank/"))
    }

    @Test
    fun navRoute_routesAreDistinct() {
        val home = NavRoute.HOME
        val islandMap = NavRoute.ISLAND_MAP
        val review = NavRoute.REVIEW
        val progress = NavRoute.PROGRESS

        assertNotEquals(home, islandMap)
        assertNotEquals(home, review)
        assertNotEquals(home, progress)
        assertNotEquals(islandMap, review)
        assertNotEquals(islandMap, progress)
        assertNotEquals(review, progress)
    }

    @Test
    fun navRoute_starBreakdown_createsCorrectRoute() {
        val route =
            NavRoute.starBreakdown(
                stars = 3,
                accuracy = 85,
                hintsUsed = 2,
                timeTaken = 45,
                errorCount = 1,
                islandId = "look_island",
            )
        assertEquals("star_breakdown/3/85/2/45/1/look_island", route)
    }

    @Test
    fun navRoute_starBreakdown_withZeroValues() {
        val route =
            NavRoute.starBreakdown(
                stars = 1,
                accuracy = 50,
                hintsUsed = 0,
                timeTaken = 0,
                errorCount = 3,
                islandId = "make_lake",
            )
        assertEquals("star_breakdown/1/50/0/0/3/make_lake", route)
    }

    @Test
    fun navRoute_starBreakdown_withPerfectScore() {
        val route =
            NavRoute.starBreakdown(
                stars = 3,
                accuracy = 100,
                hintsUsed = 0,
                timeTaken = 30,
                errorCount = 0,
                islandId = "look_island",
            )
        assertEquals("star_breakdown/3/100/0/30/0/look_island", route)
    }
}
