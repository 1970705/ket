package com.wordland.microbenchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordland.domain.algorithm.GuessingDetector
import com.wordland.domain.algorithm.MemoryStrengthAlgorithm
import com.wordland.domain.hint.HintGenerator
import com.wordland.domain.model.SpellBattleQuestion
import com.wordland.domain.model.UserWordProgress
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Microbenchmark for critical algorithms.
 *
 * Measures performance of core algorithms that run frequently during gameplay.
 * Target: All operations should complete in < 1ms to maintain 60fps.
 *
 * Run with: ./gradlew :microbenchmark:connectedCheck
 */
@RunWith(AndroidJUnit4::class)
class AlgorithmBenchmark {

    private val benchmarkRule = BenchmarkRule()

    /**
     * Benchmark memory strength calculation
     * Target: < 1ms per calculation
     * This runs on every answer submission
     */
    @Test
    fun benchmarkMemoryStrengthCalculation() {
        val algorithm = MemoryStrengthAlgorithm()
        val progress = UserWordProgress(
            userId = "user_001",
            wordId = "test_word",
            correctCount = 3,
            incorrectCount = 1,
            lastSeenAt = System.currentTimeMillis(),
            memoryStrength = 0.5f
        )

        benchmarkRule.measureRepeated(
            "MemoryStrengthCalculation"
        ) {
            algorithm.calculateNewStrength(
                progress = progress,
                isCorrect = true,
                responseTime = 2000L
            )
        }
    }

    /**
     * Benchmark answer validation (correct answer)
     * Target: < 0.1ms per validation
     * This runs on every answer submission
     */
    @Test
    fun benchmarkAnswerValidationCorrect() {
        val question = SpellBattleQuestion(
            wordId = "word_001",
            translation = "测试",
            targetWord = "example",
            hint = null
        )

        benchmarkRule.measureRepeated(
            "AnswerValidationCorrect"
        ) {
            question.isCorrect("example")
        }
    }

    /**
     * Benchmark answer validation (incorrect answer)
     * Target: < 0.1ms per validation
     */
    @Test
    fun benchmarkAnswerValidationIncorrect() {
        val question = SpellBattleQuestion(
            wordId = "word_001",
            translation = "测试",
            targetWord = "example",
            hint = null
        )

        benchmarkRule.measureRepeated(
            "AnswerValidationIncorrect"
        ) {
            question.isCorrect("examlpe")
        }
    }

    /**
     * Benchmark wrong positions calculation
     * Target: < 0.5ms per calculation
     * This runs on every keystroke to show feedback
     */
    @Test
    fun benchmarkWrongPositionsCalculation() {
        val question = SpellBattleQuestion(
            wordId = "word_001",
            translation = "测试",
            targetWord = "beautiful",
            hint = null
        )

        benchmarkRule.measureRepeated(
            "WrongPositionsCalculation"
        ) {
            question.getWrongPositions("beautifl")
        }
    }

    /**
     * Benchmark HintGenerator - Level 1 (first letter)
     * Target: < 0.1ms per hint
     */
    @Test
    fun benchmarkHintGenerationLevel1() {
        val generator = HintGenerator()

        benchmarkRule.measureRepeated(
            "HintGenerationLevel1"
        ) {
            generator.generateHint("beautiful", 1)
        }
    }

    /**
     * Benchmark HintGenerator - Level 2 (first half)
     * Target: < 0.5ms per hint
     */
    @Test
    fun benchmarkHintGenerationLevel2() {
        val generator = HintGenerator()

        benchmarkRule.measureRepeated(
            "HintGenerationLevel2"
        ) {
            generator.generateHint("beautiful", 2)
        }
    }

    /**
     * Benchmark HintGenerator - Level 3 (vowels masked)
     * Target: < 1ms per hint
     */
    @Test
    fun benchmarkHintGenerationLevel3() {
        val generator = HintGenerator()

        benchmarkRule.measureRepeated(
            "HintGenerationLevel3"
        ) {
            generator.generateHint("beautiful", 3)
        }
    }

    /**
     * Benchmark HintGenerator - Adaptive hint
     * Target: < 1ms per hint
     */
    @Test
    fun benchmarkHintGenerationAdaptive() {
        val generator = HintGenerator()

        benchmarkRule.measureRepeated(
            "HintGenerationAdaptive"
        ) {
            generator.generateAdaptiveHint("beautiful", 2)
        }
    }

    /**
     * Benchmark GuessingDetector - Pattern detection
     * Target: < 1ms per check
     */
    @Test
    fun benchmarkGuessingDetector() {
        val detector = GuessingDetector()
        val testAnswers = listOf("aaa", "abc", "xyz", "qqq", "test")

        benchmarkRule.measureRepeated(
            "GuessingDetector"
        ) {
            detector.detectGuessingPattern(testAnswers, "example")
        }
    }

    /**
     * Benchmark answer progress calculation
     * Target: < 0.5ms per calculation
     * Used to show real-time progress during typing
     */
    @Test
    fun benchmarkAnswerProgressCalculation() {
        val question = SpellBattleQuestion(
            wordId = "word_001",
            translation = "测试",
            targetWord = "beautiful",
            hint = null
        )

        benchmarkRule.measureRepeated(
            "AnswerProgressCalculation"
        ) {
            question.getAnswerProgress("beaut")
        }
    }

    /**
     * Benchmark hint letter extraction
     * Target: < 0.1ms per call
     */
    @Test
    fun benchmarkHintLetterExtraction() {
        val question = SpellBattleQuestion(
            wordId = "word_001",
            translation = "测试",
            targetWord = "example",
            hint = null
        )

        benchmarkRule.measureRepeated(
            "HintLetterExtraction"
        ) {
            question.getHintLetter()
        }
    }

    /**
     * Benchmark combined hint check flow
     * Simulates the full flow when user requests a hint
     */
    @Test
    fun benchmarkHintRequestFlow() {
        val generator = HintGenerator()
        val question = SpellBattleQuestion(
            wordId = "word_001",
            translation = "观看",
            targetWord = "watch",
            hint = null
        )

        benchmarkRule.measureRepeated(
            "HintRequestFlow"
        ) {
            // Simulate hint request flow
            val hintLetter = question.getHintLetter()
            val level1Hint = generator.generateHint(question.targetWord, 1)
            Triple(hintLetter, level1Hint, question.targetWord.length)
        }
    }
}
