package io.github.lingqiqi5211.meowui.core.preference

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryPreferenceStore(
    initialConnectionState: PreferenceConnectionState = PreferenceConnectionState.Connected,
) : PreferenceStore {
    private data class StoredPreference(
        val type: PreferenceType<*>,
        val value: Any,
    )

    private val mutationMutex = Mutex()
    private val values = MutableStateFlow<Map<String, StoredPreference>>(emptyMap())
    private val mutableConnectionState = MutableStateFlow(initialConnectionState)

    override val connectionState: StateFlow<PreferenceConnectionState> =
        mutableConnectionState.asStateFlow()

    fun updateConnectionState(state: PreferenceConnectionState) {
        mutableConnectionState.value = state
    }

    override fun <T : Any> read(key: PreferenceKey<T>): T =
        readFrom(values.value, key)

    override fun <T : Any> observe(key: PreferenceKey<T>): Flow<T> =
        values
            .map { readFrom(it, key) }
            .distinctUntilChanged()

    override suspend fun <T : Any> write(
        key: PreferenceKey<T>,
        value: T,
    ): PreferenceWriteResult = mutationMutex.withLock {
        notConnectedResult()?.let { return@withLock it }

        val currentValues = values.value
        val currentValue = currentValues[key.name]
        if (currentValue != null && currentValue.type != key.type) {
            return@withLock PreferenceWriteResult.Failure(
                typeMismatch(key, currentValue.type),
            )
        }

        val storedValue = try {
            key.type.copyValue(value)
        } catch (cause: IllegalArgumentException) {
            return@withLock PreferenceWriteResult.Failure(cause)
        }

        values.value = currentValues + (
            key.name to StoredPreference(
                type = key.type,
                value = storedValue,
            )
        )
        PreferenceWriteResult.Success
    }

    override suspend fun delete(key: PreferenceKey<*>): PreferenceWriteResult =
        mutationMutex.withLock {
            notConnectedResult()?.let { return@withLock it }

            val currentValues = values.value
            val currentValue = currentValues[key.name]
                ?: return@withLock PreferenceWriteResult.Success

            if (currentValue.type != key.type) {
                return@withLock PreferenceWriteResult.Failure(
                    typeMismatch(key, currentValue.type),
                )
            }

            values.value = currentValues - key.name
            PreferenceWriteResult.Success
        }

    override suspend fun clear(): PreferenceWriteResult = mutationMutex.withLock {
        notConnectedResult()?.let { return@withLock it }
        values.value = emptyMap()
        PreferenceWriteResult.Success
    }

    private fun notConnectedResult(): PreferenceWriteResult.NotConnected? {
        val state = mutableConnectionState.value
        return if (state == PreferenceConnectionState.Connected) {
            null
        } else {
            PreferenceWriteResult.NotConnected(state)
        }
    }

    private fun <T : Any> readFrom(
        currentValues: Map<String, StoredPreference>,
        key: PreferenceKey<T>,
    ): T {
        val storedValue = currentValues[key.name] ?: return key.defaultValue
        if (storedValue.type != key.type) {
            throw typeMismatch(key, storedValue.type)
        }
        return key.type.copyValue(storedValue.value)
    }

    private fun typeMismatch(
        key: PreferenceKey<*>,
        actualType: PreferenceType<*>,
    ): PreferenceTypeMismatchException = PreferenceTypeMismatchException(
        keyName = key.name,
        expectedType = key.type,
        actualType = actualType,
    )
}
