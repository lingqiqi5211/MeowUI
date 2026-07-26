package io.github.lingqiqi5211.meowui.libxposed

import io.github.lingqiqi5211.meowui.core.preference.PreferenceConnectionState
import io.github.lingqiqi5211.meowui.core.preference.PreferenceKey
import io.github.lingqiqi5211.meowui.core.preference.PreferenceStore
import io.github.lingqiqi5211.meowui.core.preference.PreferenceType
import io.github.lingqiqi5211.meowui.core.preference.PreferenceWriteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Keeps all coupling to the meowui-core preference API in one place. */
internal class CorePreferenceStoreAdapter : PreferenceStore, AutoCloseable {
    private val backend = SharedPreferencesStoreBackend()
    private val mutationMutex = Mutex()
    private val mutableConnectionState = MutableStateFlow<PreferenceConnectionState>(
        PreferenceConnectionState.Connecting,
    )

    override val connectionState: StateFlow<PreferenceConnectionState> =
        mutableConnectionState.asStateFlow()

    fun updateConnection(snapshot: RemotePreferencesSnapshot) {
        backend.replacePreferences(
            preferences = snapshot.preferences,
            unavailableCause = snapshot.unavailableCause,
        )
        mutableConnectionState.value = backend.connectionState.value.toCoreState()
    }

    override fun <T : Any> read(key: PreferenceKey<T>): T =
        backend.readValue(key)

    override fun <T : Any> observe(key: PreferenceKey<T>): Flow<T> =
        backend.changes
            .filter { changedKey -> changedKey == null || changedKey == key.name }
            .map { read(key) }
            .onStart { emit(read(key)) }
            .distinctUntilChanged()

    override suspend fun <T : Any> write(
        key: PreferenceKey<T>,
        value: T,
    ): PreferenceWriteResult = mutationMutex.withLock {
        withContext(Dispatchers.IO) {
            backend.writeValue(key, value).toCoreResult()
        }
    }

    override suspend fun delete(key: PreferenceKey<*>): PreferenceWriteResult =
        mutationMutex.withLock {
            withContext(Dispatchers.IO) {
                backend.remove(key.name).toCoreResult()
            }
        }

    override suspend fun clear(): PreferenceWriteResult = mutationMutex.withLock {
        withContext(Dispatchers.IO) {
            backend.clear().toCoreResult()
        }
    }

    override fun close() {
        backend.close()
        mutableConnectionState.value = PreferenceConnectionState.Disconnected()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> SharedPreferencesStoreBackend.readValue(
        key: PreferenceKey<T>,
    ): T = when (key.type) {
        PreferenceType.Boolean -> getBoolean(key.name, key.defaultValue as Boolean)
        PreferenceType.Int -> getInt(key.name, key.defaultValue as Int)
        PreferenceType.Long -> getLong(key.name, key.defaultValue as Long)
        PreferenceType.Float -> getFloat(key.name, key.defaultValue as Float)
        PreferenceType.String -> getString(key.name, key.defaultValue as String)
        PreferenceType.StringSet -> getStringSet(
            key.name,
            key.defaultValue as Set<String>,
        )
    } as T

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> SharedPreferencesStoreBackend.writeValue(
        key: PreferenceKey<T>,
        value: T,
    ): BackendWriteResult = when (key.type) {
        PreferenceType.Boolean -> putBoolean(key.name, value as Boolean)
        PreferenceType.Int -> putInt(key.name, value as Int)
        PreferenceType.Long -> putLong(key.name, value as Long)
        PreferenceType.Float -> putFloat(key.name, value as Float)
        PreferenceType.String -> putString(key.name, value as String)
        PreferenceType.StringSet -> putStringSet(key.name, value as Set<String>)
    }

    private fun BackendConnectionState.toCoreState(): PreferenceConnectionState = when (this) {
        BackendConnectionState.Available -> PreferenceConnectionState.Connected
        is BackendConnectionState.Unavailable -> PreferenceConnectionState.Disconnected(cause)
    }

    private fun BackendWriteResult.toCoreResult(): PreferenceWriteResult = when (this) {
        BackendWriteResult.Success -> PreferenceWriteResult.Success
        is BackendWriteResult.Unavailable -> PreferenceWriteResult.NotConnected(
            PreferenceConnectionState.Disconnected(cause),
        )
        is BackendWriteResult.Failure -> PreferenceWriteResult.Failure(
            cause ?: SharedPreferencesCommitException(),
        )
    }
}

private class SharedPreferencesCommitException :
    IllegalStateException("SharedPreferences rejected the write.")
