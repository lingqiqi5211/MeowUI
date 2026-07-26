package io.github.lingqiqi5211.meowui.libxposed

import android.content.SharedPreferences

internal class FakeSharedPreferences(
    initialValues: Map<String, Any> = emptyMap(),
) : SharedPreferences {
    private val lock = Any()
    private val values = initialValues.mapValuesTo(linkedMapOf()) { (_, value) ->
        value.copyForStorage()
    }
    private val listeners = linkedSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    var commitSucceeds: Boolean = true
    var commitFailure: Throwable? = null

    val listenerCount: Int
        get() = synchronized(lock) { listeners.size }

    override fun getAll(): MutableMap<String, *> = synchronized(lock) {
        values.mapValuesTo(linkedMapOf()) { (_, value) -> value.copyForRead() }
    }

    override fun getString(key: String, defaultValue: String?): String? =
        valueOrDefault(key, defaultValue)

    override fun getStringSet(
        key: String,
        defaultValues: MutableSet<String>?,
    ): MutableSet<String>? = valueOrDefault<Set<String>?>(key, defaultValues)?.toMutableSet()

    override fun getInt(key: String, defaultValue: Int): Int =
        valueOrDefault(key, defaultValue)

    override fun getLong(key: String, defaultValue: Long): Long =
        valueOrDefault(key, defaultValue)

    override fun getFloat(key: String, defaultValue: Float): Float =
        valueOrDefault(key, defaultValue)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        valueOrDefault(key, defaultValue)

    override fun contains(key: String): Boolean = synchronized(lock) {
        values.containsKey(key)
    }

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        synchronized(lock) {
            listeners += listener
        }
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) {
        synchronized(lock) {
            listeners -= listener
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> valueOrDefault(key: String, defaultValue: T): T = synchronized(lock) {
        val value = values[key] ?: return@synchronized defaultValue
        try {
            value.copyForRead() as T
        } catch (cause: ClassCastException) {
            throw ClassCastException("Preference '$key' has an incompatible type.").apply {
                initCause(cause)
            }
        }
    }

    private inner class Editor : SharedPreferences.Editor {
        private val updates = linkedMapOf<String, Any?>()
        private val removals = linkedSetOf<String>()
        private var clearRequested = false

        override fun putString(key: String, value: String?): SharedPreferences.Editor = apply {
            putValue(key, value)
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?,
        ): SharedPreferences.Editor = apply {
            putValue(key, values?.toSet())
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor = apply {
            putValue(key, value)
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor = apply {
            putValue(key, value)
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = apply {
            putValue(key, value)
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = apply {
            putValue(key, value)
        }

        override fun remove(key: String): SharedPreferences.Editor = apply {
            updates -= key
            removals += key
        }

        override fun clear(): SharedPreferences.Editor = apply {
            clearRequested = true
            updates.clear()
            removals.clear()
        }

        override fun commit(): Boolean {
            commitFailure?.let { throw it }
            if (!commitSucceeds) return false

            val changedKeys: Set<String>
            val listenersSnapshot: List<SharedPreferences.OnSharedPreferenceChangeListener>
            synchronized(lock) {
                changedKeys = applyChanges()
                listenersSnapshot = listeners.toList()
            }
            changedKeys.forEach { key ->
                listenersSnapshot.forEach { listener ->
                    listener.onSharedPreferenceChanged(this@FakeSharedPreferences, key)
                }
            }
            return true
        }

        override fun apply() {
            commit()
        }

        private fun putValue(key: String, value: Any?) {
            removals -= key
            updates[key] = value?.copyForStorage()
        }

        private fun applyChanges(): Set<String> {
            val changedKeys = linkedSetOf<String>()
            if (clearRequested) {
                changedKeys += values.keys
                values.clear()
            }

            removals.forEach { key ->
                if (values.remove(key) != null) {
                    changedKeys += key
                }
            }

            updates.forEach { (key, value) ->
                if (value == null) {
                    if (values.remove(key) != null) {
                        changedKeys += key
                    }
                } else if (values[key] != value) {
                    values[key] = value.copyForStorage()
                    changedKeys += key
                }
            }
            return changedKeys
        }
    }
}

private fun Any.copyForStorage(): Any = when (this) {
    is Set<*> -> mapTo(linkedSetOf()) { value -> value as String }
    else -> this
}

private fun Any.copyForRead(): Any = copyForStorage()
