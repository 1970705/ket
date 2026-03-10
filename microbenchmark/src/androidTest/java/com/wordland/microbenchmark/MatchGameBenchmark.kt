package com.wordland.microbenchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordland.domain.model.BubbleColor
import com.wordland.domain.model.BubbleState
import com.wordland.domain.model.MatchGameState
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Microbenchmark for Word Match Game (单词消消乐) performance.
 *
 * Measures performance of critical game operations to ensure 60fps (16.67ms/frame) target.
 *
 * Performance Targets:
 * - Bubble selection: < 1ms (O(1) lookup with Set)
 * - State update: < 5ms (avoid full list remapping)
 * - Match checking: < 2ms
 * - Progress calculation: < 0.1ms
 *
 * Run with: ./gradlew :microbenchmark:connectedCheck -P android.testInstrumentationRunnerArguments.class=com.wordland.microbenchmark.MatchGameBenchmark
 */
@RunWith(AndroidJUnit4::class)
class MatchGameBenchmark {

    private val benchmarkRule = BenchmarkRule()

    // Test data - typical game state with 12 bubbles (6 pairs)
    private val testBubbles = listOf(
        BubbleState("b1_en", "look", "look", isSelected = false, isMatched = false, color = BubbleColor.PINK),
        BubbleState("b1_zh", "看", "look", isSelected = false, isMatched = false, color = BubbleColor.PINK),
        BubbleState("b2_en", "see", "see", isSelected = false, isMatched = false, color = BubbleColor.GREEN),
        BubbleState("b2_zh", "看见", "see", isSelected = false, isMatched = false, color = BubbleColor.GREEN),
        BubbleState("b3_en", "watch", "watch", isSelected = false, isMatched = false, color = BubbleColor.PURPLE),
        BubbleState("b3_zh", "观看", "watch", isSelected = false, isMatched = false, color = BubbleColor.PURPLE),
        BubbleState("b4_en", "color", "color", isSelected = false, isMatched = false, color = BubbleColor.ORANGE),
        BubbleState("b4_zh", "颜色", "color", isSelected = false, isMatched = false, color = BubbleColor.ORANGE),
        BubbleState("b5_en", "red", "red", isSelected = false, isMatched = false, color = BubbleColor.BROWN),
        BubbleState("b5_zh", "红色", "red", isSelected = false, isMatched = false, color = BubbleColor.BROWN),
        BubbleState("b6_en", "blue", "blue", isSelected = false, isMatched = false, color = BubbleColor.BLUE),
        BubbleState("b6_zh", "蓝色", "blue", isSelected = false, isMatched = false, color = BubbleColor.BLUE),
    )

    /**
     * Benchmark O(1) lookup with Set (optimized approach)
     * Target: < 0.01ms per lookup
     * This replaces O(n) List.contains() for selection state
     */
    @Test
    fun bubbleSelectionLookupWithSet() {
        val selectedIds = setOf("b1_en", "b2_zh")

        benchmarkRule.measureRepeated("BubbleSelectionLookupSet") {
            var count = 0
            testBubbles.forEach { bubble ->
                if (bubble.id in selectedIds) {
                    count++
                }
            }
            count
        }
    }

    /**
     * Benchmark O(n) lookup with List (old approach for comparison)
     * Expected: Significantly slower than Set approach
     * This shows the performance gain from optimization
     */
    @Test
    fun bubbleSelectionLookupWithList() {
        val selectedIds = listOf("b1_en", "b2_zh")

        benchmarkRule.measureRepeated("BubbleSelectionLookupList") {
            var count = 0
            testBubbles.forEach { bubble ->
                if (selectedIds.contains(bubble.id)) {
                    count++
                }
            }
            count
        }
    }

    /**
     * Benchmark bubble selection state update
     * Target: < 1ms for full update
     * This runs on every bubble click
     */
    @Test
    fun bubbleSelectionStateUpdate() {
        val state = MatchGameState.Playing(
            bubbles = testBubbles,
            selectedBubbleIds = emptySet(),
            matchedPairs = 0,
        )

        benchmarkRule.measureRepeated("BubbleSelectionUpdate") {
            // Simulate selecting a bubble
            val newSelectedIds = state.selectedBubbleIds + "b1_en"
            state.copy(
                bubbles = state.bubbles.map { bubble ->
                    if (bubble.id == "b1_en") {
                        bubble.copy(isSelected = true)
                    } else {
                        bubble
                    }
                },
                selectedBubbleIds = newSelectedIds,
            )
        }
    }

    /**
     * Benchmark optimized bubble selection state update
     * Target: < 0.5ms - Only update changed bubble
     * This is the optimized approach
     */
    @Test
    fun optimizedBubbleSelectionUpdate() {
        val state = MatchGameState.Playing(
            bubbles = testBubbles,
            selectedBubbleIds = emptySet(),
            matchedPairs = 0,
        )

        benchmarkRule.measureRepeated("OptimizedBubbleSelectionUpdate") {
            val newSelectedIds = state.selectedBubbleIds + "b1_en"
            val wasSelected = "b1_en" in state.selectedBubbleIds
            val isSelected = "b1_en" in newSelectedIds

            // Only copy if state changed
            state.copy(
                bubbles = state.bubbles.map { bubble ->
                    if (bubble.id == "b1_en" && wasSelected != isSelected) {
                        bubble.copy(isSelected = isSelected)
                    } else {
                        bubble
                    }
                },
                selectedBubbleIds = newSelectedIds,
            )
        }
    }

    /**
     * Benchmark match verification
     * Target: < 0.1ms per match check
     * This runs when two bubbles are selected
     */
    @Test
    fun bubbleMatchVerification() {
        val state = MatchGameState.Playing(
            bubbles = testBubbles,
            selectedBubbleIds = setOf("b1_en", "b1_zh"),
            matchedPairs = 0,
        )

        benchmarkRule.measureRepeated("BubbleMatchVerification") {
            val bubble1 = state.bubbles.firstOrNull { it.id == "b1_en" }
            val bubble2 = state.bubbles.firstOrNull { it.id == "b1_zh" }
            val isMatch = bubble1?.pairId == bubble2?.pairId && bubble1?.id != bubble2?.id
            isMatch
        }
    }

    /**
     * Benchmark progress calculation
     * Target: < 0.01ms per calculation
     * This runs to update progress bar
     */
    @Test
    fun progressCalculation() {
        val state = MatchGameState.Playing(
            bubbles = testBubbles,
            selectedBubbleIds = emptySet(),
            matchedPairs = 3, // Half completed
        )

        benchmarkRule.measureRepeated("ProgressCalculation") {
            val totalPairs = state.bubbles.size / 2
            if (totalPairs == 0) 0f else state.matchedPairs.toFloat() / totalPairs
        }
    }

    /**
     * Benchmark completion check
     * Target: < 0.01ms per check
     * OLD approach: bubbles.all { it.isMatched } - O(n)
     * NEW approach: matchedPairs >= totalPairs - O(1)
     */
    @Test
    fun completionCheckOld() {
        val state = MatchGameState.Playing(
            bubbles = testBubbles,
            selectedBubbleIds = emptySet(),
            matchedPairs = 5,
        )

        benchmarkRule.measureRepeated("CompletionCheckOld") {
            state.bubbles.all { it.isMatched }
        }
    }

    /**
     * Benchmark optimized completion check
     * Target: < 0.001ms per check
     * NEW approach: Simple comparison
     */
    @Test
    fun completionCheckOptimized() {
        val state = MatchGameState.Playing(
            bubbles = testBubbles,
            selectedBubbleIds = emptySet(),
            matchedPairs = 5,
        )

        benchmarkRule.measureRepeated("CompletionCheckOptimized") {
            val totalPairs = state.bubbles.size / 2
            state.matchedPairs >= totalPairs
        }
    }

    /**
     * Benchmark match state update
     * Target: < 1ms per match
     * This runs when a correct match is made
     */
    @Test
    fun matchStateUpdate() {
        val state = MatchGameState.Playing(
            bubbles = testBubbles,
            selectedBubbleIds = setOf("b1_en", "b1_zh"),
            matchedPairs = 0,
        )

        benchmarkRule.measureRepeated("MatchStateUpdate") {
            val newBubbles = state.bubbles.map { bubble ->
                if (bubble.id in state.selectedBubbleIds) {
                    bubble.copy(isSelected = false, isMatched = true)
                } else {
                    bubble
                }
            }
            state.copy(
                bubbles = newBubbles,
                selectedBubbleIds = emptySet(),
                matchedPairs = state.matchedPairs + 1,
            )
        }
    }

    /**
     * Benchmark wrong match clear
     * Target: < 1ms per clear
     * This runs when a wrong match is made (after delay)
     */
    @Test
    fun wrongMatchClear() {
        val state = MatchGameState.Playing(
            bubbles = testBubbles.map { it.copy(isSelected = true) },
            selectedBubbleIds = setOf("b1_en", "b2_zh"),
            matchedPairs = 0,
        )

        benchmarkRule.measureRepeated("WrongMatchClear") {
            state.copy(
                bubbles = state.bubbles.map { it.copy(isSelected = false) },
                selectedBubbleIds = emptySet(),
            )
        }
    }

    /**
     * Benchmark full click interaction
     * Target: < 5ms total (well under 16.67ms for 60fps)
     * Simulates complete flow: click → select → check → update
     */
    @Test
    fun fullClickInteraction() {
        val state = MatchGameState.Playing(
            bubbles = testBubbles,
            selectedBubbleIds = emptySet(),
            matchedPairs = 0,
        )

        benchmarkRule.measureRepeated("FullClickInteraction") {
            // 1. Select first bubble
            val firstState = state.copy(
                bubbles = state.bubbles.map { it.copy(isSelected = it.id == "b1_en") },
                selectedBubbleIds = setOf("b1_en"),
            )

            // 2. Select second bubble
            val secondState = firstState.copy(
                bubbles = firstState.bubbles.map { it.copy(isSelected = it.id in setOf("b1_en", "b1_zh")) },
                selectedBubbleIds = setOf("b1_en", "b1_zh"),
            )

            // 3. Check match
            val bubble1 = secondState.bubbles.firstOrNull { it.id == "b1_en" }
            val bubble2 = secondState.bubbles.firstOrNull { it.id == "b1_zh" }
            val isMatch = bubble1?.pairId == bubble2?.pairId

            // 4. Update state if match
            if (isMatch) {
                secondState.copy(
                    bubbles = secondState.bubbles.map {
                        if (it.id in setOf("b1_en", "b1_zh")) it.copy(isSelected = false, isMatched = true)
                        else it
                    },
                    selectedBubbleIds = emptySet(),
                    matchedPairs = 1,
                )
            } else {
                secondState
            }
        }
    }
}
