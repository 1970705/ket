package com.wordland.ui.components

import com.wordland.domain.model.MapPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for PlayerShip component
 *
 * Story #2.3: Player Ship Display
 *
 * Test coverage:
 * - Ship state management
 * - Movement animations
 * - Arrival detection
 * - Position updates
 */
class PlayerShipTest {
    /**
     * TC-EP2-041: Ship Icon Visibility
     * Test ship state initialization
     */
    @Test
    fun shipState_initializesCorrectly() {
        val state = ShipState.Initial

        assertEquals(
            "Initial position should be at center (0.5, 0.5)",
            MapPosition(0.5f, 0.5f),
            state.position,
        )
        assertEquals(
            "Target position should match initial position",
            MapPosition(0.5f, 0.5f),
            state.targetPosition,
        )
        assertFalse("Initial ship should not be moving", state.isMoving)
        assertEquals("Initial region should be null", null, state.currentRegionId)
    }

    /**
     * TC-EP2-045: Ship position updates on region change
     */
    @Test
    fun shipState_updatesPositionOnRegionChange() {
        val initialState = ShipState.Initial
        val newPosition = MapPosition(0.3f, 0.7f)

        val updatedState =
            initialState.moveToRegion(
                regionId = "look_peninsula",
                newPosition = newPosition,
            )

        assertEquals(
            "Target position should update to new position",
            newPosition,
            updatedState.targetPosition,
        )
        assertTrue(
            "Ship should be marked as moving",
            updatedState.isMoving,
        )
        assertEquals(
            "Current region ID should update",
            "look_peninsula",
            updatedState.currentRegionId,
        )
        assertEquals(
            "Original position should remain until animation completes",
            initialState.position,
            updatedState.position,
        )
    }

    /**
     * Test ship movement completion
     */
    @Test
    fun shipState_completesMovement() {
        val movingState =
            ShipState(
                position = MapPosition(0.2f, 0.3f),
                targetPosition = MapPosition(0.4f, 0.6f),
                isMoving = true,
                currentRegionId = "make_lake",
            )

        val completedState = movingState.completeMovement()

        assertEquals(
            "Position should update to target after completion",
            MapPosition(0.4f, 0.6f),
            completedState.position,
        )
        assertEquals(
            "Target position should match position",
            completedState.position,
            completedState.targetPosition,
        )
        assertFalse(
            "Ship should not be moving after completion",
            completedState.isMoving,
        )
        assertEquals(
            "Region ID should be preserved",
            "make_lake",
            completedState.currentRegionId,
        )
    }

    /**
     * Test arrival detection
     */
    @Test
    fun shipState_detectsArrival() {
        // Not moving and at target
        val arrivedState =
            ShipState(
                position = MapPosition(0.5f, 0.5f),
                targetPosition = MapPosition(0.5f, 0.5f),
                isMoving = false,
                currentRegionId = "look_peninsula",
            )

        assertTrue(
            "Ship at target should detect arrival",
            arrivedState.hasArrived(),
        )

        // Moving state
        val movingState =
            ShipState(
                position = MapPosition(0.2f, 0.3f),
                targetPosition = MapPosition(0.4f, 0.6f),
                isMoving = true,
                currentRegionId = "look_peninsula",
            )

        assertFalse(
            "Moving ship should not detect arrival",
            movingState.hasArrived(),
        )

        // Close but not exactly at target
        val closeState =
            ShipState(
                position = MapPosition(0.5f, 0.5f),
                targetPosition = MapPosition(0.505f, 0.505f),
                isMoving = false,
                currentRegionId = "look_peninsula",
            )

        assertTrue(
            "Ship within threshold should detect arrival",
            closeState.hasArrived(),
        )

        // Far from target
        val farState =
            ShipState(
                position = MapPosition(0.2f, 0.3f),
                targetPosition = MapPosition(0.8f, 0.9f),
                isMoving = false,
                currentRegionId = "look_peninsula",
            )

        assertFalse(
            "Ship far from target should not detect arrival",
            farState.hasArrived(),
        )
    }

    /**
     * TC-EP2-046: Ship position saves to database
     * Test state preservation
     */
    @Test
    fun shipState_preservesAcrossOperations() {
        val originalState =
            ShipState(
                position = MapPosition(0.3f, 0.7f),
                targetPosition = MapPosition(0.3f, 0.7f),
                isMoving = false,
                currentRegionId = "think_forest",
            )

        // Simulate database save/load
        val savedPosition = originalState.position
        val savedRegionId = originalState.currentRegionId

        val restoredState =
            ShipState(
                position = savedPosition,
                targetPosition = savedPosition,
                isMoving = false,
                currentRegionId = savedRegionId,
            )

        assertEquals(
            "Position should be preserved",
            originalState.position,
            restoredState.position,
        )
        assertEquals(
            "Region ID should be preserved",
            originalState.currentRegionId,
            restoredState.currentRegionId,
        )
        assertTrue(
            "Restored ship should detect arrival",
            restoredState.hasArrived(),
        )
    }

    /**
     * Test multiple sequential moves
     */
    @Test
    fun shipState_handlesSequentialMoves() {
        val state1 = ShipState.Initial

        // Move to first region
        val state2 = state1.moveToRegion("look_peninsula", MapPosition(0.2f, 0.3f))
        assertTrue("First move should start", state2.isMoving)

        // Complete first move
        val state3 = state2.completeMovement()
        assertEquals(
            "First move should complete",
            MapPosition(0.2f, 0.3f),
            state3.position,
        )
        assertTrue("Should arrive at first destination", state3.hasArrived())

        // Move to second region
        val state4 = state3.moveToRegion("make_lake", MapPosition(0.7f, 0.8f))
        assertEquals(
            "Second move should start from first destination",
            MapPosition(0.2f, 0.3f),
            state4.position,
        )
        assertEquals(
            "Second move target should be new region",
            MapPosition(0.7f, 0.8f),
            state4.targetPosition,
        )

        // Complete second move
        val state5 = state4.completeMovement()
        assertEquals(
            "Second move should complete",
            MapPosition(0.7f, 0.8f),
            state5.position,
        )
        assertEquals(
            "Second region ID should be set",
            "make_lake",
            state5.currentRegionId,
        )
    }

    /**
     * Test movement to same position
     */
    @Test
    fun shipState_handlesMoveToSamePosition() {
        val initialState =
            ShipState(
                position = MapPosition(0.5f, 0.5f),
                targetPosition = MapPosition(0.5f, 0.5f),
                isMoving = false,
                currentRegionId = "look_peninsula",
            )

        val updatedState =
            initialState.moveToRegion(
                regionId = "look_peninsula",
                newPosition = MapPosition(0.5f, 0.5f),
            )

        // Moving to same position should not trigger animation
        assertFalse(
            "Move to same position should not set moving flag",
            updatedState.isMoving,
        )
        assertEquals(
            "Position should remain unchanged",
            MapPosition(0.5f, 0.5f),
            updatedState.position,
        )
        assertTrue(
            "Should be considered arrived",
            updatedState.hasArrived(),
        )
    }

    /**
     * Test region ID updates
     */
    @Test
    fun shipState_updatesRegionIdCorrectly() {
        val state = ShipState.Initial

        val state1 = state.moveToRegion("look_peninsula", MapPosition(0.2f, 0.3f))
        assertEquals(
            "First region ID should be look_peninsula",
            "look_peninsula",
            state1.currentRegionId,
        )

        val state2 = state1.moveToRegion("make_lake", MapPosition(0.7f, 0.8f))
        assertEquals(
            "Second region ID should be make_lake",
            "make_lake",
            state2.currentRegionId,
        )

        val state3 = state2.moveToRegion("think_forest", MapPosition(0.4f, 0.5f))
        assertEquals(
            "Third region ID should be think_forest",
            "think_forest",
            state3.currentRegionId,
        )
    }

    /**
     * Test position coordinate bounds
     */
    @Test
    fun shipPosition_handlesBoundaryPositions() {
        // Test corner positions
        val corners =
            listOf(
                MapPosition(0f, 0f),
                MapPosition(1f, 0f),
                MapPosition(0f, 1f),
                MapPosition(1f, 1f),
            )

        corners.forEach { position ->
            val state =
                ShipState(
                    position = position,
                    targetPosition = position,
                    isMoving = false,
                    currentRegionId = "test",
                )

            assertTrue(
                "Corner position $position should be valid",
                state.hasArrived(),
            )
        }
    }

    /**
     * Test state immutability
     */
    @Test
    fun shipState_maintainsImmutability() {
        val original = ShipState.Initial

        // Operations should return new instances
        val updated = original.moveToRegion("look_peninsula", MapPosition(0.2f, 0.3f))

        // Original should be unchanged
        assertEquals(
            "Original position should not change",
            MapPosition(0.5f, 0.5f),
            original.position,
        )
        assertFalse(
            "Original should not be marked as moving",
            original.isMoving,
        )

        // Updated should have new values
        assertEquals(
            "Updated position should be new position",
            MapPosition(0.2f, 0.3f),
            updated.targetPosition,
        )
        assertTrue(
            "Updated should be marked as moving",
            updated.isMoving,
        )
    }
}
