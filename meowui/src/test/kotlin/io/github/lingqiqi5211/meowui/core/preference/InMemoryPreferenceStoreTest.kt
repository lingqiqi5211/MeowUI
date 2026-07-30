package io.github.lingqiqi5211.meowui.core.preference

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryPreferenceStoreTest {
    @Test
    fun observeEmitsDefaultWritesAndDeletes() = runBlocking {
        val store = InMemoryPreferenceStore()
        val key = PreferenceKey.boolean("enabled")
        val emissions = Channel<Boolean>(Channel.UNLIMITED)
        val observer = launch(Dispatchers.Default) {
            store.observe(key).collect(emissions::send)
        }

        try {
            assertFalse(emissions.receiveWithTimeout())
            assertEquals(PreferenceWriteResult.Success, store.write(key, true))
            assertTrue(emissions.receiveWithTimeout())
            assertEquals(PreferenceWriteResult.Success, store.delete(key))
            assertFalse(emissions.receiveWithTimeout())
        } finally {
            observer.cancelAndJoin()
            emissions.cancel()
        }
    }

    @Test
    fun connectionStateIsObservableAndBlocksMutations() = runBlocking {
        val disconnected = PreferenceConnectionState.Disconnected(
            IllegalStateException("service unavailable"),
        )
        val store = InMemoryPreferenceStore(disconnected)
        val key = PreferenceKey.int("count", 2)
        val states = Channel<PreferenceConnectionState>(Channel.UNLIMITED)
        val observer = launch(Dispatchers.Default) {
            store.connectionState.collect(states::send)
        }

        try {
            assertEquals(disconnected, states.receiveWithTimeout())
            assertEquals(
                PreferenceWriteResult.NotConnected(disconnected),
                store.write(key, 5),
            )
            assertEquals(2, store.read(key))

            store.updateConnectionState(PreferenceConnectionState.Connected)
            assertEquals(PreferenceConnectionState.Connected, states.receiveWithTimeout())
            assertEquals(PreferenceWriteResult.Success, store.write(key, 5))
            assertEquals(5, store.read(key))
        } finally {
            observer.cancelAndJoin()
            states.cancel()
        }
    }

    @Test
    fun conflictingTypesFailWithoutReplacingTheStoredValue() = runBlocking {
        val store = InMemoryPreferenceStore()
        val booleanKey = PreferenceKey.boolean("shared")
        val intKey = PreferenceKey.int("shared")

        assertEquals(PreferenceWriteResult.Success, store.write(booleanKey, true))

        val result = store.write(intKey, 1)
        assertTrue(result is PreferenceWriteResult.Failure)
        assertTrue((result as PreferenceWriteResult.Failure).cause is PreferenceTypeMismatchException)
        assertTrue(store.read(booleanKey))
        assertThrows(PreferenceTypeMismatchException::class.java) {
            store.read(intKey)
        }
        Unit
    }

    @Test
    fun clearResetsAllKeysAndDoesNotRunWhileDisconnected() = runBlocking {
        val store = InMemoryPreferenceStore()
        val enabled = PreferenceKey.boolean("enabled")
        val title = PreferenceKey.string("title", "default")

        assertEquals(PreferenceWriteResult.Success, store.write(enabled, true))
        assertEquals(PreferenceWriteResult.Success, store.write(title, "custom"))

        val disconnected = PreferenceConnectionState.Disconnected()
        store.updateConnectionState(disconnected)
        assertEquals(
            PreferenceWriteResult.NotConnected(disconnected),
            store.clear(),
        )
        assertTrue(store.read(enabled))
        assertEquals("custom", store.read(title))

        store.updateConnectionState(PreferenceConnectionState.Connected)
        assertEquals(PreferenceWriteResult.Success, store.clear())
        assertFalse(store.read(enabled))
        assertEquals("default", store.read(title))
    }

    @Test
    fun stringSetsAreCopiedBeforeStorage() = runBlocking {
        val store = InMemoryPreferenceStore()
        val key = PreferenceKey.stringSet("items")
        val source = linkedSetOf("one", "two")

        assertEquals(PreferenceWriteResult.Success, store.write(key, source))
        source += "three"

        assertEquals(setOf("one", "two"), store.read(key))
    }

    private suspend fun <T> Channel<T>.receiveWithTimeout(): T =
        withTimeout(2_000) { receive() }
}
