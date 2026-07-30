package io.github.lingqiqi5211.meowui.libxposed

import android.content.SharedPreferences
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

internal sealed interface BackendConnectionState {
    data object Available : BackendConnectionState

    data class Unavailable(
        val cause: Throwable? = null,
    ) : BackendConnectionState
}

internal sealed interface BackendWriteResult {
    data object Success : BackendWriteResult

    data class Unavailable(
        val cause: Throwable? = null,
    ) : BackendWriteResult

    data class Failure(
        val cause: Throwable? = null,
    ) : BackendWriteResult
}

internal class SharedPreferencesStoreBackend(
    preferences: SharedPreferences? = null,
    unavailableCause: Throwable? = null,
) : AutoCloseable {
    private val lock = Any()
    private var currentPreferences: SharedPreferences? = null
    private var closed = false

    private val _connectionState = MutableStateFlow<BackendConnectionState>(
        BackendConnectionState.Unavailable(unavailableCause),
    )
    val connectionState: StateFlow<BackendConnectionState> = _connectionState.asStateFlow()

    private val _changes = MutableSharedFlow<String?>(
        replay = 1,
        extraBufferCapacity = CHANGE_BUFFER_CAPACITY - 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val changes: SharedFlow<String?> = _changes.asSharedFlow()

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            _changes.tryEmit(key)
        }

    init {
        replacePreferences(preferences, unavailableCause)
    }

    fun replacePreferences(
        preferences: SharedPreferences?,
        unavailableCause: Throwable? = null,
    ) {
        synchronized(lock) {
            if (closed || currentPreferences === preferences) {
                if (!closed && preferences == null) {
                    _connectionState.value = BackendConnectionState.Unavailable(unavailableCause)
                }
                return
            }

            unregisterCurrentListener()
            currentPreferences = preferences

            if (preferences == null) {
                _connectionState.value = BackendConnectionState.Unavailable(unavailableCause)
                _changes.tryEmit(null)
                return
            }

            try {
                preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
                _connectionState.value = BackendConnectionState.Available
                _changes.tryEmit(null)
            } catch (error: Throwable) {
                currentPreferences = null
                _connectionState.value = BackendConnectionState.Unavailable(error)
                _changes.tryEmit(null)
            }
        }
    }

    fun getBoolean(name: String, defaultValue: Boolean): Boolean =
        read(defaultValue) { getBoolean(name, defaultValue) }

    fun getInt(name: String, defaultValue: Int): Int =
        read(defaultValue) { getInt(name, defaultValue) }

    fun getLong(name: String, defaultValue: Long): Long =
        read(defaultValue) { getLong(name, defaultValue) }

    fun getFloat(name: String, defaultValue: Float): Float =
        read(defaultValue) { getFloat(name, defaultValue) }

    fun getString(name: String, defaultValue: String): String =
        read(defaultValue) { getString(name, defaultValue) ?: defaultValue }

    fun getStringSet(name: String, defaultValue: Set<String>): Set<String> =
        read(defaultValue) {
            getStringSet(name, defaultValue)?.toSet() ?: defaultValue
        }

    fun putBoolean(name: String, value: Boolean): BackendWriteResult =
        write { putBoolean(name, value) }

    fun putInt(name: String, value: Int): BackendWriteResult =
        write { putInt(name, value) }

    fun putLong(name: String, value: Long): BackendWriteResult =
        write { putLong(name, value) }

    fun putFloat(name: String, value: Float): BackendWriteResult =
        write { putFloat(name, value) }

    fun putString(name: String, value: String): BackendWriteResult =
        write { putString(name, value) }

    fun putStringSet(name: String, value: Set<String>): BackendWriteResult =
        write { putStringSet(name, value.toSet()) }

    fun remove(name: String): BackendWriteResult = write { remove(name) }

    fun clear(): BackendWriteResult = write { clear() }

    override fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
            unregisterCurrentListener()
            currentPreferences = null
            _connectionState.value = BackendConnectionState.Unavailable()
            _changes.tryEmit(null)
        }
    }

    private inline fun <T> read(
        defaultValue: T,
        readValue: SharedPreferences.() -> T,
    ): T {
        val preferences = synchronized(lock) { currentPreferences } ?: return defaultValue
        return preferences.readValue()
    }

    private inline fun write(
        operation: SharedPreferences.Editor.() -> SharedPreferences.Editor,
    ): BackendWriteResult {
        val preferences = synchronized(lock) { currentPreferences }
            ?: return BackendWriteResult.Unavailable(
                (_connectionState.value as? BackendConnectionState.Unavailable)?.cause,
            )

        return try {
            val editor = preferences.edit()
            editor.operation()
            if (editor.commit()) {
                BackendWriteResult.Success
            } else {
                BackendWriteResult.Failure()
            }
        } catch (error: Throwable) {
            BackendWriteResult.Failure(error)
        }
    }

    private fun unregisterCurrentListener() {
        try {
            currentPreferences?.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        } catch (_: Throwable) {
            // A dead remote preference instance no longer needs local listener cleanup.
        }
    }

    private companion object {
        const val CHANGE_BUFFER_CAPACITY = 64
    }
}
