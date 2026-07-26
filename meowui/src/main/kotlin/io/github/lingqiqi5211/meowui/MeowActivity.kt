package io.github.lingqiqi5211.meowui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable

fun ComponentActivity.setMeowContent(content: @Composable () -> Unit) {
    enableEdgeToEdge()
    setContent(content = content)
}

abstract class MeowActivity : ComponentActivity() {
    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMeowContent { Content() }
    }

    @Composable
    protected abstract fun Content()
}
