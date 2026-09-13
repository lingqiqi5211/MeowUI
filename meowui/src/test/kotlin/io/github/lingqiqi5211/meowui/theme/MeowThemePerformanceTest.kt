package io.github.lingqiqi5211.meowui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class MeowThemePerformanceTest {
    @get:Rule val compose = createComposeRule()

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Test
    fun colorAnimationKeepsTheSameMotionScheme() {
        var dark by mutableStateOf(false)
        val frames = mutableListOf<Pair<Color, MotionScheme>>()
        compose.setContent {
            MeowTheme(MeowUiStyle.MaterialExpressive, darkTheme = dark, dynamicColor = false) {
                val frame = MeowTheme.colors.primary to MaterialTheme.motionScheme
                SideEffect { frames += frame }
                Text("Theme", color = frame.first)
            }
        }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { dark = true }
        repeat(12) {
            compose.mainClock.advanceTimeBy(32)
            compose.waitForIdle()
        }
        compose.runOnIdle {
            assertTrue(frames.map { it.first }.distinct().size > 1)
            val motion = frames.first().second
            frames.forEach { assertSame(motion, it.second) }
        }
        compose.mainClock.autoAdvance = true
    }
}
