package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowAppearance
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import io.github.lingqiqi5211.meowui.theme.MeowThemeMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MeowRtlTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun breadcrumbCentersAndClicksTheHighlightedSegmentInBothDirections() {
        val direction = mutableStateOf(LayoutDirection.Rtl)
        val style = mutableStateOf(MeowUiStyle.MaterialExpressive)
        val highlighted = mutableIntStateOf(6)
        var clicked = -1
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction.value) {
                MeowTheme(
                    appearance =
                        MeowAppearance(
                            style = style.value,
                            themeMode = MeowThemeMode.Light,
                            dynamicColor = false,
                            blurEnabled = false,
                        ),
                ) {
                    MeowBreadcrumbBar(
                        items = (0..11).map { MeowBreadcrumbItem("segment-$it") },
                        onItemClick = { clicked = it },
                        modifier = Modifier.size(300.dp, 64.dp).testTag("bar"),
                        highlightIndex = highlighted.intValue,
                    )
                }
            }
        }

        fun checkCenter(index: Int) {
            val bar = compose.onNodeWithTag("bar").fetchSemanticsNode().boundsInRoot
            val item = compose.onNodeWithText("segment-$index").fetchSemanticsNode().boundsInRoot
            // Miuix 原生居中单位包含尾随的 18dp 分隔符，文字中心相差半个分隔符。
            val separatorHalf = with(compose.density) { 9.dp.toPx() }
            val expected =
                bar.center.x +
                    if (style.value == MeowUiStyle.Miuix) {
                        if (direction.value == LayoutDirection.Rtl) separatorHalf else -separatorHalf
                    } else {
                        0f
                    }
            assertEquals("${style.value} ${direction.value} index=$index", expected, item.center.x, 3f)
            compose.onNodeWithText("segment-$index").performClick()
            compose.runOnIdle { assertEquals(index, clicked) }
        }
        for (uiStyle in MeowUiStyle.entries) {
            for (layout in LayoutDirection.entries) {
                compose.runOnIdle {
                    style.value = uiStyle
                    direction.value = layout
                    highlighted.intValue = 6
                }
                checkCenter(6)
                compose.runOnIdle { highlighted.intValue = 3 }
                checkCenter(3)
                // 边界项不能居中，但必须完整可见并可点击。
                for (edge in listOf(0, 11)) {
                    compose.runOnIdle { highlighted.intValue = edge }
                    compose.onNodeWithText("segment-$edge").assertIsDisplayed().performClick()
                    compose.runOnIdle { assertEquals(edge, clicked) }
                }
            }
        }
    }

    @Test
    @Config(sdk = [32], qualifiers = "w411dp-h891dp")
    @GraphicsMode(GraphicsMode.Mode.LEGACY)
    fun incomingPagesAndCoveredPagesMirrorInBothStyles() {
        val direction = mutableStateOf(LayoutDirection.Ltr)
        val style = mutableStateOf(MeowUiStyle.MaterialExpressive)
        val stack = mutableStateOf(listOf("root"))
        val positions = mutableMapOf<String, Float>()
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction.value) {
                MeowTheme(appearance = MeowAppearance(style = style.value, dynamicColor = false, blurEnabled = false)) {
                    MeowNavHost(stack.value) { page ->
                        Text(page, Modifier.fillMaxSize().onGloballyPositioned { positions[page] = it.positionInRoot().x })
                    }
                }
            }
        }
        for (uiStyle in MeowUiStyle.entries) {
            for (layout in LayoutDirection.entries) {
                compose.runOnIdle {
                    stack.value = listOf("root")
                    style.value = uiStyle
                    direction.value = layout
                }
                compose.waitForIdle()
                compose.mainClock.autoAdvance = false
                compose.runOnIdle { stack.value = listOf("root", "page") }
                compose.mainClock.advanceTimeByFrame()
                compose.waitForIdle()
                compose.mainClock.advanceTimeBy(112)
                compose.waitForIdle()
                val incoming = checkNotNull(positions["page"])
                val covered = checkNotNull(positions["root"])
                if (layout == LayoutDirection.Rtl) {
                    assertTrue("$uiStyle RTL incoming=$incoming", incoming < -1f)
                    assertTrue("$uiStyle RTL covered=$covered", covered > 1f)
                } else {
                    assertTrue("$uiStyle LTR incoming=$incoming", incoming > 1f)
                    assertTrue("$uiStyle LTR covered=$covered", covered < -1f)
                }
                compose.mainClock.autoAdvance = true
                compose.waitForIdle()
                compose.mainClock.autoAdvance = false
                compose.runOnIdle { stack.value = listOf("root") }
                compose.mainClock.advanceTimeByFrame()
                compose.waitForIdle()
                compose.mainClock.advanceTimeBy(112)
                compose.waitForIdle()
                val outgoing = checkNotNull(positions["page"])
                val revealed = checkNotNull(positions["root"])
                assertTrue("$uiStyle $layout outgoing=$outgoing", if (layout == LayoutDirection.Rtl) outgoing < -1f else outgoing > 1f)
                assertTrue("$uiStyle $layout revealed=$revealed", if (layout == LayoutDirection.Rtl) revealed > 1f else revealed < -1f)
                compose.mainClock.autoAdvance = true
                compose.waitForIdle()
            }
        }
    }

    @Test
    fun fadeMirrorsAtBothScrollLimitsAndDoesNotFadeShortContent() {
        val direction = mutableStateOf(LayoutDirection.Rtl)
        val longContent = mutableStateOf(true)
        lateinit var scroll: ScrollState
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction.value) {
                scroll = remember { ScrollState(0) }
                Box(Modifier.background(Color.White)) {
                    Box(
                        Modifier
                            .size(100.dp, 40.dp)
                            .testTag("edges")
                            .breadcrumbEdgeFade(scroll, 20.dp, 0.dp)
                            .background(Color.Red)
                            .horizontalScroll(scroll),
                    ) {
                        Box(Modifier.size(if (longContent.value) 300.dp else 50.dp, 40.dp).background(Color.Red))
                    }
                }
            }
        }

        fun checkEdges(
            leftFades: Boolean,
            rightFades: Boolean,
        ) {
            val pixels = compose.onNodeWithTag("edges").captureToImage().toPixelMap()
            val y = pixels.height / 2
            val left = pixels[2, y].green
            val right = pixels[pixels.width - 3, y].green
            assertTrue("left=$left expected fade=$leftFades", if (leftFades) left > 0.5f else left < 0.05f)
            assertTrue("right=$right expected fade=$rightFades", if (rightFades) right > 0.5f else right < 0.05f)
        }
        for (layout in LayoutDirection.entries) {
            compose.runOnIdle {
                direction.value = layout
                runBlocking { scroll.scrollTo(0) }
            }
            checkEdges(layout == LayoutDirection.Rtl, layout == LayoutDirection.Ltr)
            compose.runOnIdle { runBlocking { scroll.scrollTo(scroll.maxValue / 2) } }
            checkEdges(true, true)
            compose.runOnIdle { runBlocking { scroll.scrollTo(scroll.maxValue) } }
            checkEdges(layout == LayoutDirection.Ltr, layout == LayoutDirection.Rtl)
        }
        compose.runOnIdle { longContent.value = false }
        checkEdges(false, false)
    }

    @Test
    @Config(sdk = [26], qualifiers = "w411dp-h891dp")
    @GraphicsMode(GraphicsMode.Mode.LEGACY)
    fun unsupportedBlurRetainsBoundsAndBackClicksInBothStyles() = checkBlurFallback()

    @Test
    @Config(sdk = [32], qualifiers = "w411dp-h891dp")
    @GraphicsMode(GraphicsMode.Mode.LEGACY)
    fun missingBackdropRetainsBoundsAndBackClicksInBothStyles() = checkBlurFallback()

    private fun checkBlurFallback() {
        val style = mutableStateOf(MeowUiStyle.MaterialExpressive)
        val direction = mutableStateOf(LayoutDirection.Rtl)
        val enabled = mutableStateOf(false)
        var clicks = 0
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction.value) {
                MeowTheme(appearance = MeowAppearance(style = style.value, blurEnabled = enabled.value, dynamicColor = false)) {
                    MeowTopBar(
                        title = "Fallback",
                        modifier = Modifier.testTag("topbar"),
                        navigationModifier = Modifier.testTag("back"),
                        onBackClick = { clicks++ },
                    )
                }
            }
        }
        for (uiStyle in MeowUiStyle.entries) {
            for (layout in LayoutDirection.entries) {
                compose.runOnIdle {
                    style.value = uiStyle
                    direction.value = layout
                    enabled.value = false
                }
                val baseline = compose.onNodeWithTag("topbar").fetchSemanticsNode().boundsInRoot
                val backBounds = compose.onNodeWithTag("back").fetchSemanticsNode().boundsInRoot
                compose.runOnIdle { enabled.value = true }
                val titles = compose.onAllNodesWithText("Fallback")
                assertTrue(titles.fetchSemanticsNodes().indices.any { titles[it].isDisplayed() })
                assertEquals(baseline, compose.onNodeWithTag("topbar").fetchSemanticsNode().boundsInRoot)
                assertEquals(backBounds, compose.onNodeWithTag("back").fetchSemanticsNode().boundsInRoot)
                val previous = clicks
                compose.onNodeWithTag("back").assertIsDisplayed().performClick()
                compose.runOnIdle { assertEquals(previous + 1, clicks) }
            }
        }
    }

    @Test
    fun rtlFadeUsesTheLeftEdgeForForwardContent() {
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val scroll = remember { ScrollState(0) }
                // ScrollState before measurement exposes forward content, never backward content.
                Box(Modifier.background(Color.White)) {
                    Box(
                        Modifier
                            .size(100.dp, 40.dp)
                            .testTag("fade")
                            .breadcrumbEdgeFade(scroll, 20.dp, 0.dp)
                            .background(Color.Red),
                    )
                }
            }
        }
        val pixels = compose.onNodeWithTag("fade").captureToImage().toPixelMap()
        val y = pixels.height / 2
        val left = pixels[2, y]
        val right = pixels[pixels.width - 3, y]
        assertTrue("Left edge must fade: $left", left.green > 0.5f)
        assertTrue("Right edge must remain opaque: $right", right.green < 0.05f)
    }
}
