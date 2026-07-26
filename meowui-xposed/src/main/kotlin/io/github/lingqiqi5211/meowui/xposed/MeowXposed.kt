package io.github.lingqiqi5211.meowui.xposed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.github.lingqiqi5211.meowui.core.preference.PreferenceWriteResult
import io.github.lingqiqi5211.meowui.libxposed.XposedServicePreferenceStore
import io.github.lingqiqi5211.meowui.preference.MeowPreferenceProvider
import io.github.lingqiqi5211.meowui.setMeowContent

/** Provides an Xposed-backed preference store for the current composition. */
@Composable
fun MeowXposedPreferenceHost(
    preferenceName: String,
    onWriteResult: (PreferenceWriteResult) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val store = remember(preferenceName) {
        XposedServicePreferenceStore(preferenceName)
    }

    DisposableEffect(store) {
        onDispose { store.close() }
    }

    MeowPreferenceProvider(
        store = store,
        onWriteResult = onWriteResult,
        content = content,
    )
}

/** Sets edge-to-edge Compose content backed by Xposed remote preferences. */
fun ComponentActivity.setMeowXposedContent(
    preferenceName: String,
    onWriteResult: (PreferenceWriteResult) -> Unit = {},
    content: @Composable () -> Unit,
) {
    setMeowContent {
        MeowXposedPreferenceHost(
            preferenceName = preferenceName,
            onWriteResult = onWriteResult,
            content = content,
        )
    }
}

abstract class MeowXposedActivity : ComponentActivity() {
    protected abstract val preferenceName: String

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMeowXposedContent(
            preferenceName = preferenceName,
            onWriteResult = ::onWriteResult,
        ) {
            Content()
        }
    }

    protected open fun onWriteResult(result: PreferenceWriteResult) = Unit

    @Composable
    protected abstract fun Content()
}
