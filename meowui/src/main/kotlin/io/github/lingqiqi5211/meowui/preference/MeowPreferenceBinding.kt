package io.github.lingqiqi5211.meowui.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.lingqiqi5211.meowui.core.preference.PreferenceConnectionState
import io.github.lingqiqi5211.meowui.core.preference.PreferenceKey
import io.github.lingqiqi5211.meowui.core.preference.PreferenceStore
import io.github.lingqiqi5211.meowui.core.preference.PreferenceWriteResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val LocalPreferenceStore = staticCompositionLocalOf<PreferenceStore?> { null }

private val LocalPreferenceWriteResultHandler =
    staticCompositionLocalOf<(PreferenceWriteResult) -> Unit> { { } }

@Composable
fun MeowPreferenceProvider(
    store: PreferenceStore,
    onWriteResult: (PreferenceWriteResult) -> Unit = {},
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalPreferenceStore provides store,
        LocalPreferenceWriteResultHandler provides onWriteResult,
        content = content,
    )
}

@Composable
fun currentMeowPreferenceStore(): PreferenceStore = LocalPreferenceStore.current
    ?: error(
        "No PreferenceStore found. Use MeowPreferenceProvider or setMeowXposedContent.",
    )

@Composable
fun <T : Any> rememberMeowPreferenceValue(
    key: PreferenceKey<T>,
    store: PreferenceStore? = null,
): State<T> {
    val resolvedStore = store ?: currentMeowPreferenceStore()
    val values = remember(resolvedStore, key) { resolvedStore.observe(key) }
    // initial 只读一次；observe 首次发射当前值，重组时不再触发同步读取。
    val initialValue = remember(resolvedStore, key) { resolvedStore.read(key) }
    return key(resolvedStore, key) { values.collectAsState(initial = initialValue) }
}

@Composable
fun <T : Any> rememberMeowPreferenceState(
    key: PreferenceKey<T>,
    store: PreferenceStore? = null,
): MutableState<T> {
    val observedValue = rememberMeowPreferenceValue(key, store)
    val write = rememberMeowPreferenceWriter(key, store)

    return remember(observedValue, write) {
        object : MutableState<T> {
            override var value: T
                get() = observedValue.value
                set(newValue) = write(newValue)

            override fun component1(): T = value

            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

@Composable
fun rememberMeowPreferenceConnectionState(
    store: PreferenceStore? = null,
): State<PreferenceConnectionState> {
    val resolvedStore = store ?: currentMeowPreferenceStore()
    return key(resolvedStore) { resolvedStore.connectionState.collectAsState() }
}

@Composable
fun <T : Any> rememberMeowPreferenceWriter(
    key: PreferenceKey<T>,
    store: PreferenceStore? = null,
    onWriteResult: ((PreferenceWriteResult) -> Unit)? = null,
): (T) -> Unit {
    val write = rememberMeowPreferenceSuspendingWriter(key, store, onWriteResult)
    val scope = rememberCoroutineScope()
    return remember(write, scope) {
        { value -> scope.launch { write(value) } }
    }
}

/** Shares result reporting with components that also need to await a write. */
@Composable
internal fun <T : Any> rememberMeowPreferenceSuspendingWriter(
    key: PreferenceKey<T>,
    store: PreferenceStore? = null,
    onWriteResult: ((PreferenceWriteResult) -> Unit)? = null,
): suspend (T) -> PreferenceWriteResult {
    val resolvedStore = store ?: currentMeowPreferenceStore()
    val providerCallback = LocalPreferenceWriteResultHandler.current
    return key(resolvedStore, key) {
        val latestCallback by rememberUpdatedState(onWriteResult ?: providerCallback)
        val writes = remember { Mutex() }
        remember {
            { value: T ->
                writes.withLock {
                    resolvedStore.write(key, value).also { latestCallback(it) }
                }
            }
        }
    }
}
