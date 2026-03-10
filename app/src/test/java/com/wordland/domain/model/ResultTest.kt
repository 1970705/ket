package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Result wrapper class
 */
class ResultTest {
    @Test
    fun `Success contains correct data`() {
        // Given
        val data = "Test Data"
        val result = Result.Success(data)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(data, result.data)
    }

    @Test
    fun `Error contains correct exception`() {
        // Given
        val exception = RuntimeException("Test error")
        val result = Result.Error(exception)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(exception, result.exception)
        assertEquals("Test error", result.exception.message)
    }

    @Test
    fun `Loading is correctly identified`() {
        // Given
        val result = Result.Loading

        // Then
        assertTrue(result is Result.Loading)
    }

    @Test
    fun `Success with Int data`() {
        // Given
        val value = 42
        val result = Result.Success(value)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(42, result.data)
    }

    @Test
    fun `Success with List data`() {
        // Given
        val list = listOf("item1", "item2", "item3")
        val result = Result.Success(list)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(3, result.data.size)
        assertEquals("item1", result.data[0])
    }

    @Test
    fun `Error with different exception types`() {
        // Test with IllegalStateException
        val illegalStateException = IllegalStateException("Illegal state")
        val result1 = Result.Error(illegalStateException)
        assertTrue(result1 is Result.Error)
        assertEquals("Illegal state", result1.exception.message)

        // Test with NullPointerException
        val nullPointerException = NullPointerException("Null value")
        val result2 = Result.Error(nullPointerException)
        assertTrue(result2 is Result.Error)
        assertEquals("Null value", result2.exception.message)

        // Test with generic Exception
        val exception = Exception("Generic error")
        val result3 = Result.Error(exception)
        assertTrue(result3 is Result.Error)
        assertEquals("Generic error", result3.exception.message)
    }

    @Test
    fun `Success with complex data structure`() {
        // Given
        data class TestData(
            val id: String,
            val name: String,
            val value: Int,
        )

        val data = TestData("id1", "Test", 100)
        val result = Result.Success(data)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("id1", result.data.id)
        assertEquals("Test", result.data.name)
        assertEquals(100, result.data.value)
    }

    @Test
    fun `Error with null message`() {
        // Given
        val exception: RuntimeException = RuntimeException(null as String?)
        val result = Result.Error(exception)

        // Then
        assertTrue(result is Result.Error)
        assertNull(result.exception.message)
    }

    @Test
    fun `Loading can be used as singleton`() {
        // Given
        val loading1 = Result.Loading
        val loading2 = Result.Loading

        // Then - both should be Loading type
        assertTrue(loading1 is Result.Loading)
        assertTrue(loading2 is Result.Loading)
    }

    @Test
    fun `Result sealed class prevents direct instantiation`() {
        // Result is a sealed class, so we can only create Success, Error, or Loading
        // This test verifies the type hierarchy works correctly

        val success: Result<String> = Result.Success("data")
        val error: Result<String> = Result.Error(RuntimeException("error"))
        val loading: Result<String> = Result.Loading

        assertTrue(success is Result)
        assertTrue(error is Result)
        assertTrue(loading is Result)
    }

    @Test
    fun `Success with nullable data type`() {
        // Given
        val result: Result<String?> = Result.Success<String?>(null)

        // Then
        assertTrue(result is Result.Success)
        assertNull((result as Result.Success<String?>).data)
    }

    @Test
    fun `Error with chained exceptions`() {
        // Given
        val cause = RuntimeException("Root cause")
        val exception = RuntimeException("Wrapper error", cause)
        val result = Result.Error(exception)

        // Then
        assertTrue(result is Result.Error)
        assertEquals("Wrapper error", result.exception.message)
        assertEquals(cause, result.exception.cause)
    }
}
