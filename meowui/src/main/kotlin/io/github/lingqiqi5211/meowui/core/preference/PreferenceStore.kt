package io.github.lingqiqi5211.meowui.core.preference

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface PreferenceConnectionState {
    data object Connecting : PreferenceConnectionState

    data object Connected : PreferenceConnectionState

    data class Disconnected(
        val cause: Throwable? = null,
    ) : PreferenceConnectionState
}

sealed interface PreferenceWriteResult {
    data object Success : PreferenceWriteResult

    data class NotConnected(
        val state: PreferenceConnectionState,
    ) : PreferenceWriteResult

    data class Failure(
        val cause: Throwable,
    ) : PreferenceWriteResult
}

interface PreferenceStore {
    val connectionState: StateFlow<PreferenceConnectionState>

    /** Returns the stored value, or the key default when no value exists. */
    fun <T : Any> read(key: PreferenceKey<T>): T

    /** Emits the current value immediately, then emits each distinct change. */
    fun <T : Any> observe(key: PreferenceKey<T>): Flow<T>

    suspend fun <T : Any> write(
        key: PreferenceKey<T>,
        value: T,
    ): PreferenceWriteResult

    suspend fun delete(key: PreferenceKey<*>): PreferenceWriteResult

    suspend fun clear(): PreferenceWriteResult
}
