package com.wordland.domain.usecase.usecases.tutorial

import com.wordland.domain.repository.OnboardingRepository
import com.wordland.domain.usecase.usecases.CompleteTutorialWordUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Error handling tests for CompleteTutorialWordUseCase.
 * Tests exception cases and error scenarios.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CompleteTutorialWordErrorTest {
    private lateinit var useCase: CompleteTutorialWordUseCase
    private lateinit var repository: OnboardingRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        useCase = CompleteTutorialWordUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // === Error Handling Tests ===

    @Test
    fun `invoke throws IllegalStateException when no state exists`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When & Then
            var exception: IllegalStateException? = null
            try {
                useCase(3)
            } catch (e: IllegalStateException) {
                exception = e
            }
            assertNotNull(exception)
            assertEquals("Onboarding not started", exception?.message)
        }

    @Test
    fun `invoke exception message is descriptive`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When & Then
            var exception: IllegalStateException? = null
            try {
                useCase(3)
            } catch (e: IllegalStateException) {
                exception = e
            }
            assertTrue(exception?.message?.contains("Onboarding") == true)
        }

    @Test
    fun `invoke does not save when exception thrown`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When
            try {
                useCase(3)
            } catch (e: IllegalStateException) {
                // Expected
            }

            // Then
            coVerify(exactly = 0) { repository.saveOnboardingState(any()) }
        }

    @Test
    fun `invoke does not call getOnboardingState again after exception`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When
            try {
                useCase(3)
            } catch (e: IllegalStateException) {
                // Expected
            }

            // Then - should only call once to check state
            coVerify(exactly = 1) { repository.getOnboardingState() }
        }

    @Test
    fun `invoke propagates repository exceptions`() =
        runTest {
            // Given
            val expectedException = RuntimeException("Repository error")
            coEvery { repository.getOnboardingState() } throws expectedException

            // When & Then
            var caughtException: Throwable? = null
            try {
                useCase(3)
            } catch (e: Exception) {
                caughtException = e
            }

            assertTrue(caughtException is RuntimeException)
            assertEquals("Repository error", caughtException?.message)
        }

    @Test
    fun `invoke does not save when repository throws exception`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } throws RuntimeException("Database error")

            // When
            try {
                useCase(3)
            } catch (e: RuntimeException) {
                // Expected
            }

            // Then - save should not be called
            coVerify(exactly = 0) { repository.saveOnboardingState(any()) }
        }

    // === Null State Handling Tests ===

    @Test
    fun `isTutorialComplete returns false when state is null`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When
            val result = useCase.isTutorialComplete()

            // Then
            assertFalse(result)
        }

    @Test
    fun `getRemainingWords returns default when state is null`() =
        runTest {
            // Given
            coEvery { repository.getOnboardingState() } returns null

            // When
            val result = useCase.getRemainingWords()

            // Then - returns REQUIRED_WORDS_FOR_CHEST (5)
            assertEquals(5, result)
        }

    @Test
    fun `getRemainingWords handles null state gracefully`() =
        runTest {
            // Given - multiple calls with null state
            coEvery { repository.getOnboardingState() } returns null

            // When - call multiple times
            val result1 = useCase.getRemainingWords()
            val result2 = useCase.getRemainingWords()

            // Then - should consistently return 5
            assertEquals(5, result1)
            assertEquals(5, result2)
        }
}
