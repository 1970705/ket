package com.wordland.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * Generic result wrapper for use case operations
 * Provides a standard way to handle success and error states
 *
 * Annotated with @Stable for the sealed class (hierarchy is stable)
 * Individual subclasses use @Immutable
 */
@Stable
sealed class Result<out T> {
    @Immutable
    data class Success<T>(val data: T) : Result<T>()

    @Immutable
    data class Error(val exception: Throwable) : Result<Nothing>()

    @Immutable
    object Loading : Result<Nothing>()
}

/**
 * Extension function to map success data
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        is Result.Loading -> this
    }
}

/**
 * Extension function to handle success and error states
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}
