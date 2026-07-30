package io.github.lingqiqi5211.meowui.libxposed

import io.github.lingqiqi5211.meowui.core.preference.PreferenceConnectionState
import io.github.lingqiqi5211.meowui.core.preference.PreferenceKey
import io.github.lingqiqi5211.meowui.core.preference.PreferenceWriteResult
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LibXposedPreferenceStoreTest {
    @Test
    fun readsAndWritesEverySupportedType() = runBlocking {
        val preferences = FakeSharedPreferences(
            mapOf(
                "boolean" to true,
                "int" to 7,
                "long" to 8L,
                "float" to 1.5f,
                "string" to "meow",
                "set" to linkedSetOf("a", "b"),
            ),
        )
        val store = connectedStore(preferences)

        val booleanKey = PreferenceKey.boolean("boolean")
        val intKey = PreferenceKey.int("int")
        val longKey = PreferenceKey.long("long")
        val floatKey = PreferenceKey.float("float")
        val stringKey = PreferenceKey.string("string")
        val stringSetKey = PreferenceKey.stringSet("set")

        assertEquals(PreferenceConnectionState.Connected, store.connectionState.value)
        assertTrue(store.read(booleanKey))
        assertEquals(7, store.read(intKey))
        assertEquals(8L, store.read(longKey))
        assertEquals(1.5f, store.read(floatKey))
        assertEquals("meow", store.read(stringKey))

        val firstSet = store.read(stringSetKey)
        val secondSet = store.read(stringSetKey)
        assertEquals(setOf("a", "b"), firstSet)
        assertNotSame(firstSet, secondSet)

        assertEquals(PreferenceWriteResult.Success, store.write(booleanKey, false))
        assertEquals(PreferenceWriteResult.Success, store.write(intKey, 70))
        assertEquals(PreferenceWriteResult.Success, store.write(longKey, 80L))
        assertEquals(PreferenceWriteResult.Success, store.write(floatKey, 2.5f))
        assertEquals(PreferenceWriteResult.Success, store.write(stringKey, "ui"))
        assertEquals(PreferenceWriteResult.Success, store.write(stringSetKey, setOf("c")))

        assertFalse(preferences.getBoolean("boolean", true))
        assertEquals(70, preferences.getInt("int", 0))
        assertEquals(80L, preferences.getLong("long", 0L))
        assertEquals(2.5f, preferences.getFloat("float", 0f))
        assertEquals("ui", preferences.getString("string", null))
        assertEquals(setOf("c"), preferences.getStringSet("set", null))

        store.close()
    }

    @Test
    fun observeEmitsSharedPreferencesChanges() = runBlocking {
        val key = PreferenceKey.boolean("enabled")
        val preferences = FakeSharedPreferences(mapOf("enabled" to false))
        val store = connectedStore(preferences)
        val observedValues = mutableListOf<Boolean>()
        val observation = async(start = CoroutineStart.UNDISPATCHED) {
            store.observe(key).take(2).toList(observedValues)
        }

        preferences.edit().putBoolean("enabled", true).commit()
        observation.await()

        assertEquals(listOf(false, true), observedValues)
        store.close()
    }

    @Test
    fun serviceDisconnectMakesStoreUnavailableAndRejectsWrites() = runBlocking {
        val key = PreferenceKey.boolean("enabled")
        val preferences = FakeSharedPreferences()
        val connection = FakeRemotePreferencesConnection(
            RemotePreferencesSnapshot(preferences),
        )
        val store = ConnectionBackedPreferenceStore(connection)
        val disconnected = async(start = CoroutineStart.UNDISPATCHED) {
            store.connectionState.drop(1).first()
        }

        connection.publish(RemotePreferencesSnapshot(preferences = null))

        assertTrue(disconnected.await() is PreferenceConnectionState.Disconnected)
        assertFalse(store.read(key))
        assertTrue(store.write(key, true) is PreferenceWriteResult.NotConnected)
        assertFalse(preferences.contains(key.name))
        assertEquals(0, preferences.listenerCount)

        store.close()
    }

    @Test
    fun failedCommitIsReportedAndDoesNotChangeStoredValue() = runBlocking {
        val key = PreferenceKey.int("count", 1)
        val preferences = FakeSharedPreferences(mapOf("count" to 4)).apply {
            commitSucceeds = false
        }
        val store = connectedStore(preferences)

        val result = store.write(key, 9)

        assertTrue(result is PreferenceWriteResult.Failure)
        assertEquals(4, store.read(key))
        store.close()
    }

    @Test
    fun reconnectMovesListenerToNewPreferencesAndRefreshesObservation() = runBlocking {
        val key = PreferenceKey.string("theme", "system")
        val oldPreferences = FakeSharedPreferences(mapOf("theme" to "old"))
        val newPreferences = FakeSharedPreferences(mapOf("theme" to "new"))
        val connection = FakeRemotePreferencesConnection(
            RemotePreferencesSnapshot(oldPreferences),
        )
        val store = ConnectionBackedPreferenceStore(connection)
        val observedValues = mutableListOf<String>()
        val observation = async(start = CoroutineStart.UNDISPATCHED) {
            store.observe(key).take(2).toList(observedValues)
        }

        connection.publish(RemotePreferencesSnapshot(newPreferences))
        observation.await()

        assertEquals(listOf("old", "new"), observedValues)
        assertEquals(0, oldPreferences.listenerCount)
        assertEquals(1, newPreferences.listenerCount)

        oldPreferences.edit().putString("theme", "ignored").commit()
        assertEquals("new", store.read(key))

        store.close()
        assertEquals(0, newPreferences.listenerCount)
    }

    @Test
    fun deleteAndClearReturnRealCommitResults() = runBlocking {
        val firstKey = PreferenceKey.string("first")
        val secondKey = PreferenceKey.int("second")
        val preferences = FakeSharedPreferences(
            mapOf(
                "first" to "value",
                "second" to 2,
            ),
        )
        val store = connectedStore(preferences)

        assertEquals(PreferenceWriteResult.Success, store.delete(firstKey))
        assertFalse(preferences.contains(firstKey.name))

        preferences.commitSucceeds = false
        assertTrue(store.clear() is PreferenceWriteResult.Failure)
        assertTrue(preferences.contains(secondKey.name))

        preferences.commitSucceeds = true
        assertEquals(PreferenceWriteResult.Success, store.clear())
        assertTrue(preferences.all.isEmpty())

        store.close()
    }

    private fun connectedStore(
        preferences: FakeSharedPreferences,
    ): ConnectionBackedPreferenceStore = ConnectionBackedPreferenceStore(
        FakeRemotePreferencesConnection(RemotePreferencesSnapshot(preferences)),
    )
}

private class FakeRemotePreferencesConnection(
    initialSnapshot: RemotePreferencesSnapshot,
) : RemotePreferencesConnection {
    private val lock = Any()
    private val listeners = linkedSetOf<(RemotePreferencesSnapshot) -> Unit>()
    private var snapshot = initialSnapshot

    override fun subscribe(
        listener: (RemotePreferencesSnapshot) -> Unit,
    ): ConnectionSubscription {
        val currentSnapshot = synchronized(lock) {
            listeners += listener
            snapshot
        }
        listener(currentSnapshot)
        return ConnectionSubscription {
            synchronized(lock) {
                listeners -= listener
            }
        }
    }

    fun publish(snapshot: RemotePreferencesSnapshot) {
        val listenersSnapshot = synchronized(lock) {
            this.snapshot = snapshot
            listeners.toList()
        }
        listenersSnapshot.forEach { listener -> listener(snapshot) }
    }
}
