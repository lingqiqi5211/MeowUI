package io.github.lingqiqi5211.meowui.core.preference

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class PreferenceKeyTest {
    @Test
    fun directCreationInfersEverySupportedType() {
        val booleanKey: PreferenceKey<Boolean> = PreferenceKey("enabled", false)
        val intKey: PreferenceKey<Int> = PreferenceKey("mode", 0)
        val longKey: PreferenceKey<Long> = PreferenceKey("duration", 1L)
        val floatKey: PreferenceKey<Float> = PreferenceKey("opacity", 0.5f)
        val stringKey: PreferenceKey<String> = PreferenceKey("name", "")
        val stringSetKey: PreferenceKey<Set<String>> = PreferenceKey("tags", setOf("one", "two"))

        assertSame(PreferenceType.Boolean, booleanKey.type)
        assertSame(PreferenceType.Int, intKey.type)
        assertSame(PreferenceType.Long, longKey.type)
        assertSame(PreferenceType.Float, floatKey.type)
        assertSame(PreferenceType.String, stringKey.type)
        assertSame(PreferenceType.StringSet, stringSetKey.type)
        assertEquals(false, booleanKey.defaultValue)
        assertEquals(0, intKey.defaultValue)
        assertEquals(1L, longKey.defaultValue)
        assertEquals(0.5f, floatKey.defaultValue)
        assertEquals("", stringKey.defaultValue)
        assertEquals(setOf("one", "two"), stringSetKey.defaultValue)
    }

    @Test
    fun factoriesDeclareEverySupportedType() {
        assertSame(PreferenceType.Boolean, PreferenceKey.boolean("boolean").type)
        assertSame(PreferenceType.Int, PreferenceKey.int("int").type)
        assertSame(PreferenceType.Long, PreferenceKey.long("long").type)
        assertSame(PreferenceType.Float, PreferenceKey.float("float").type)
        assertSame(PreferenceType.String, PreferenceKey.string("string").type)
        assertSame(PreferenceType.StringSet, PreferenceKey.stringSet("string_set").type)
    }

    @Test
    fun factoriesKeepTypedDefaults() {
        assertEquals(true, PreferenceKey.boolean("boolean", true).defaultValue)
        assertEquals(7, PreferenceKey.int("int", 7).defaultValue)
        assertEquals(8L, PreferenceKey.long("long", 8L).defaultValue)
        assertEquals(1.5f, PreferenceKey.float("float", 1.5f).defaultValue)
        assertEquals("value", PreferenceKey.string("string", "value").defaultValue)
        assertEquals(setOf("one", "two"), PreferenceKey.stringSet("set", setOf("one", "two")).defaultValue)
    }

    @Test
    fun stringSetDefaultIsDefensivelyCopied() {
        val source = linkedSetOf("one", "two")
        val key = PreferenceKey.stringSet("set", source)

        source += "three"
        val firstRead = key.defaultValue
        val secondRead = key.defaultValue

        assertEquals(setOf("one", "two"), firstRead)
        assertEquals(firstRead, secondRead)
        assertNotSame(firstRead, secondRead)
    }

    @Test
    fun blankNamesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            PreferenceKey.boolean("   ")
        }
    }

    @Test
    fun unsupportedDefaultTypesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            PreferenceKey("double", 1.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PreferenceKey("list", listOf("value"))
        }
    }

    @Test
    fun nonStringSetsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            PreferenceKey("numbers", setOf(1, 2))
        }
        assertThrows(IllegalArgumentException::class.java) {
            PreferenceKey("empty_numbers", emptySet<Int>())
        }
    }
}
