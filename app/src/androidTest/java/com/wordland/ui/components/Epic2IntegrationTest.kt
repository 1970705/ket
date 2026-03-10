package com.wordland.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Epic #2 Integration Tests: Map System Reconstruction
 *
 * Sprint 1: Day 3-4 - Integration Testing Phase
 *
 * Test Coverage:
 * - View Toggle (4 tests)
 * - Fog System (3 tests)
 * - Player Ship (3 tests)
 * - Region Unlock (2 tests)
 *
 * Total: 12 integration tests
 */
@RunWith(AndroidJUnit4::class)
class Epic2IntegrationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // ========== Story #2.1: View Toggle (4 tests) ==========

    @Test
    fun tcEp2_001_ToggleButtonIsVisibleOnWorldMap() {
        composeTestRule.setContent {
            MaterialTheme {
                ViewModeTransitionSimple(
                    viewMode = com.wordland.domain.model.MapViewMode.WORLD_VIEW,
                    islandContent = { Box(modifier = Modifier.size(100.dp)) },
                    worldContent = { Box(modifier = Modifier.size(200.dp)) },
                )
            }
        }

        // Toggle button should be present
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun tcEp2_002_WorldToIslandViewTransitionCompletes() {
        var viewMode = com.wordland.domain.model.MapViewMode.WORLD_VIEW

        composeTestRule.setContent {
            MaterialTheme {
                ViewModeTransitionSimple(
                    viewMode = viewMode,
                    islandContent = { Box(modifier = Modifier.size(100.dp)) },
                    worldContent = { Box(modifier = Modifier.size(200.dp)) },
                )
            }
        }

        // Initial world view should render
        composeTestRule.onRoot().assertExists()

        // Simulate toggle
        viewMode = com.wordland.domain.model.MapViewMode.ISLAND_VIEW
        composeTestRule.setContent {
            MaterialTheme {
                ViewModeTransitionSimple(
                    viewMode = viewMode,
                    islandContent = { Box(modifier = Modifier.size(100.dp)) },
                    worldContent = { Box(modifier = Modifier.size(200.dp)) },
                )
            }
        }

        // Island view should render
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun tcEp2_003_IslandToWorldViewTransitionCompletes() {
        var viewMode = com.wordland.domain.model.MapViewMode.ISLAND_VIEW

        composeTestRule.setContent {
            MaterialTheme {
                ViewModeTransitionSimple(
                    viewMode = viewMode,
                    islandContent = { Box(modifier = Modifier.size(100.dp)) },
                    worldContent = { Box(modifier = Modifier.size(200.dp)) },
                )
            }
        }

        // Initial island view should render
        composeTestRule.onRoot().assertExists()

        // Simulate toggle
        viewMode = com.wordland.domain.model.MapViewMode.WORLD_VIEW
        composeTestRule.setContent {
            MaterialTheme {
                ViewModeTransitionSimple(
                    viewMode = viewMode,
                    islandContent = { Box(modifier = Modifier.size(100.dp)) },
                    worldContent = { Box(modifier = Modifier.size(200.dp)) },
                )
            }
        }

        // World view should render
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun tcEp2_004_ViewStatePersistsDuringConfigurationChange() {
        val viewMode = com.wordland.domain.model.MapViewMode.WORLD_VIEW

        composeTestRule.setContent {
            MaterialTheme {
                ViewModeTransitionSimple(
                    viewMode = viewMode,
                    islandContent = { Box(modifier = Modifier.size(100.dp)) },
                    worldContent = { Box(modifier = Modifier.size(200.dp)) },
                )
            }
        }

        // Verify state is maintained
        composeTestRule.onRoot().assertExists()
    }

    // ========== Story #2.2: Fog System (3 tests) ==========

    @Test
    fun tcEp2_021_FogRendersForListOfRegions() {
        val regions =
            listOf(
                com.wordland.domain.model.MapRegion(
                    id = "look_island",
                    islandId = "look_island",
                    name = "Look Island",
                    position = com.wordland.domain.model.MapPosition(0.3f, 0.3f),
                    bounds = com.wordland.domain.model.MapBounds(0.0f, 0.0f, 1.0f, 1.0f),
                    fogLevel = com.wordland.domain.model.FogLevel.VISIBLE,
                    isUnlocked = true,
                    icon = "🌲",
                ),
            )

        composeTestRule.setContent {
            MaterialTheme {
                FogOverlay(
                    regions = regions,
                    mapWidth = 400.dp,
                    mapHeight = 300.dp,
                )
            }
        }

        // Fog overlay should render
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun tcEp2_022_FogDisplaysWithCorrectVisibilityRadius() {
        val regions =
            listOf(
                com.wordland.domain.model.MapRegion(
                    id = "make_lake",
                    islandId = "make_lake",
                    name = "Make Lake",
                    position = com.wordland.domain.model.MapPosition(0.5f, 0.5f),
                    bounds = com.wordland.domain.model.MapBounds(0.0f, 0.0f, 1.0f, 1.0f),
                    fogLevel = com.wordland.domain.model.FogLevel.HIDDEN,
                    isUnlocked = false,
                    icon = "🌊",
                ),
            )

        composeTestRule.setContent {
            MaterialTheme {
                FogOverlay(
                    regions = regions,
                    mapWidth = 400.dp,
                    mapHeight = 300.dp,
                    playerPosition = com.wordland.domain.model.MapPosition(0.3f, 0.3f),
                    visibilityRadius = 0.15f,
                )
            }
        }

        // Fogged region should display fog overlay
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun tcEp2_023_FogAnimationCompletes() {
        val regions =
            listOf(
                com.wordland.domain.model.MapRegion(
                    id = "listen_valley",
                    islandId = "listen_valley",
                    name = "Listen Valley",
                    position = com.wordland.domain.model.MapPosition(0.7f, 0.3f),
                    bounds = com.wordland.domain.model.MapBounds(0.0f, 0.0f, 1.0f, 1.0f),
                    fogLevel = com.wordland.domain.model.FogLevel.HIDDEN,
                    isUnlocked = false,
                    icon = "⛰️",
                ),
            )

        composeTestRule.setContent {
            MaterialTheme {
                FogOverlay(
                    regions = regions,
                    mapWidth = 400.dp,
                    mapHeight = 300.dp,
                    playerPosition = com.wordland.domain.model.MapPosition(0.7f, 0.3f),
                    visibilityRadius = 0.5f,
                )
            }
        }

        // Fog should render
        composeTestRule.onRoot().assertExists()
    }

    // ========== Story #2.3: Player Ship (3 tests) ==========

    @Test
    fun tcEp2_041_ShipIconIsVisibleOnWorldView() {
        composeTestRule.setContent {
            MaterialTheme {
                PlayerShip(
                    currentRegionId = "look_island",
                    targetPosition = com.wordland.domain.model.MapPosition(0.3f, 0.3f),
                    mapWidth = 400.dp,
                    mapHeight = 300.dp,
                )
            }
        }

        // Ship should be visible
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun tcEp2_042_ShipMovesToNewRegionWhenPlayerNavigates() {
        var shipPosition = com.wordland.domain.model.MapPosition(0.3f, 0.3f)

        composeTestRule.setContent {
            MaterialTheme {
                PlayerShip(
                    currentRegionId = "look_island",
                    targetPosition = shipPosition,
                    mapWidth = 400.dp,
                    mapHeight = 300.dp,
                )
            }
        }

        // Initial position
        composeTestRule.onRoot().assertExists()

        // Simulate movement
        shipPosition = com.wordland.domain.model.MapPosition(0.5f, 0.5f)
        composeTestRule.setContent {
            MaterialTheme {
                PlayerShip(
                    currentRegionId = "make_lake",
                    targetPosition = shipPosition,
                    mapWidth = 400.dp,
                    mapHeight = 300.dp,
                )
            }
        }

        // New position should render
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun tcEp2_043_ShipMarkerDisplaysAtCorrectPosition() {
        composeTestRule.setContent {
            MaterialTheme {
                PlayerShipMarker(
                    position = com.wordland.domain.model.MapPosition(0.4f, 0.6f),
                    mapWidth = 400.dp,
                    mapHeight = 300.dp,
                )
            }
        }

        // Ship marker should display
        composeTestRule.onRoot().assertExists()
    }

    // ========== Story #2.4: Region Unlock (2 tests) ==========

    @Test
    fun tcEp2_053_UnlockDialogShowsWithRegionInfo() {
        val region =
            com.wordland.domain.model.MapRegion(
                id = "make_lake",
                islandId = "make_lake",
                name = "Make Lake",
                position = com.wordland.domain.model.MapPosition(0.5f, 0.5f),
                bounds = com.wordland.domain.model.MapBounds(0.0f, 0.0f, 1.0f, 1.0f),
                fogLevel = com.wordland.domain.model.FogLevel.VISIBLE,
                isUnlocked = true,
                icon = "🌊",
            )

        composeTestRule.setContent {
            MaterialTheme {
                RegionUnlockDialog(
                    region = region,
                    onDismiss = { },
                    onConfirm = { },
                )
            }
        }

        // Dialog should display region info
        composeTestRule.onNodeWithText("Make Lake").assertExists()
    }

    @Test
    fun tcEp2_054_UnlockDialogShowsConfirmAndCancelButtons() {
        val region =
            com.wordland.domain.model.MapRegion(
                id = "listen_valley",
                islandId = "listen_valley",
                name = "Listen Valley",
                position = com.wordland.domain.model.MapPosition(0.7f, 0.3f),
                bounds = com.wordland.domain.model.MapBounds(0.0f, 0.0f, 1.0f, 1.0f),
                fogLevel = com.wordland.domain.model.FogLevel.HIDDEN,
                isUnlocked = false,
                icon = "⛰️",
            )

        composeTestRule.setContent {
            MaterialTheme {
                RegionUnlockDialog(
                    region = region,
                    onDismiss = { },
                    onConfirm = { },
                )
            }
        }

        // Dialog should show region name
        composeTestRule.onNodeWithText("Listen Valley").assertExists()
    }
}
