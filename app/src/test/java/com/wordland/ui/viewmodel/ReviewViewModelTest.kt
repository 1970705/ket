package com.wordland.ui.viewmodel

import com.wordland.domain.model.Result
import com.wordland.domain.model.ReviewWordItem
import com.wordland.domain.model.Word
import com.wordland.domain.usecase.usecases.GetReviewWordsUseCase
import com.wordland.ui.uistate.ReviewUiState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ReviewViewModel
 * Tests the review screen ViewModel with mocked UseCase
 */
class ReviewViewModelTest {
    private lateinit var viewModel: ReviewViewModel
    private lateinit var getReviewWords: GetReviewWordsUseCase

    private val testUserId = "test_user"

    private val testWord =
        Word(
            id = "word1",
            word = "test",
            translation = "测试",
            pronunciation = "/test/",
            audioPath = null,
            partOfSpeech = "noun",
            difficulty = 1,
            frequency = 100,
            theme = "test",
            islandId = "test_island",
            levelId = "test_level",
            order = 1,
            ketLevel = true,
            petLevel = false,
            exampleSentences = null,
            relatedWords = null,
            root = null,
            prefix = null,
            suffix = null,
        )

    @Before
    fun setup() {
        getReviewWords = mockk(relaxed = true)
        viewModel = ReviewViewModel(getReviewWords)
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `loadReviewWords updates uiState to Ready when words exist`() =
        runTest {
            val reviewWords =
                listOf(
                    ReviewWordItem(
                        word = testWord,
                        memoryStrength = 50,
                        lastReviewTime = System.currentTimeMillis() - 1000,
                        correctRate = 0.8f,
                    ),
                )
            coEvery { getReviewWords(testUserId, 20) } returns Result.Success(reviewWords)

            viewModel.loadReviewWords(testUserId, 20)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is ReviewUiState.Ready)
            assertEquals(true, (state as ReviewUiState.Ready).hasWords)
            assertEquals(1, viewModel.reviewWords.value.size)
        }

    @Test
    fun `loadReviewWords shows empty state when no words`() =
        runTest {
            coEvery { getReviewWords(testUserId, 20) } returns Result.Success(emptyList())

            viewModel.loadReviewWords(testUserId, 20)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is ReviewUiState.Ready)
            assertEquals(false, (state as ReviewUiState.Ready).hasWords)
            assertEquals(0, viewModel.reviewWords.value.size)
        }

    @Test
    fun `loadReviewWords updates uiState to Error on failure`() =
        runTest {
            coEvery { getReviewWords(testUserId, 20) } returns Result.Error(Exception("Test error"))

            viewModel.loadReviewWords(testUserId, 20)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state is ReviewUiState.Error)
            assertEquals("Test error", (state as ReviewUiState.Error).message)
        }

    @Test
    fun `loadReviewWords splits words into due and learning`() =
        runTest {
            val now = System.currentTimeMillis()
            val reviewWords =
                listOf(
                    ReviewWordItem(
                        word = testWord.copy(id = "word1", word = "due", translation = "到期"),
                        memoryStrength = 50,
                        lastReviewTime = now - 10000,
                        correctRate = 0.7f,
                    ),
                    ReviewWordItem(
                        word = testWord.copy(id = "word2", word = "learning", translation = "学习中"),
                        memoryStrength = 20,
                        lastReviewTime = null,
                        correctRate = 0.5f,
                    ),
                )
            coEvery { getReviewWords(testUserId, 20) } returns Result.Success(reviewWords)

            viewModel.loadReviewWords(testUserId, 20)
            advanceUntilIdle()

            assertEquals(1, viewModel.reviewWords.value.size)
            assertEquals("due", viewModel.reviewWords.value[0].word.word)
            assertEquals(1, viewModel.learningWords.value.size)
            assertEquals("learning", viewModel.learningWords.value[0].word.word)
        }

    @Test
    fun `initial uiState is Loading`() {
        val state = viewModel.uiState.value
        assertTrue(state is ReviewUiState.Loading)
    }
}
