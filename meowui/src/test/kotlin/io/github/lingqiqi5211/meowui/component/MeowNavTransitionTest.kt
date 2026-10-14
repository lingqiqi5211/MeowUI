package io.github.lingqiqi5211.meowui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import top.yukonga.miuix.kmp.nav.runtime.NavChange
import top.yukonga.miuix.kmp.nav.transition.NavGesture
import top.yukonga.miuix.kmp.nav.transition.NavRole
import top.yukonga.miuix.kmp.nav.transition.NavSettle
import top.yukonga.miuix.kmp.nav.transition.NavSettlePhase
import top.yukonga.miuix.kmp.nav.transition.NavSettleSpec
import top.yukonga.miuix.kmp.nav.transition.NavSwipeEdge
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope

class MeowNavTransitionTest {
    @Test
    fun creatingEntryModifierDoesNotReadDepthOrSettleAndKeepsScopeLive() {
        val source = TestScope()
        lateinit var deferredScope: NavTransitionScope
        val base =
            NavTransition { scope ->
                deferredScope = scope
                this
            }
        val transition = navNoPredictiveTransition(base)
        with(transition) { Modifier.transformEntry(source) }
        assertEquals(0, source.depthReads)
        assertEquals(0, source.settleReads)

        assertEquals(0f, deferredScope.relativeDepth, 0.00001f)
        // 复用同一个 Modifier 持有的 scope，不重新调用 transformEntry。
        source.progress = 0.7f
        source.depth = -0.7f
        assertEquals(0f, deferredScope.relativeDepth, 0.00001f)
        source.progress = null
        source.depth = -0.25f
        assertEquals(-0.25f, deferredScope.relativeDepth, 0.00001f)
    }

    @Test
    fun commitAndCancelKeepEntryAndScrimContinuousWithTheSameTween() {
        for (settlePhase in listOf(NavSettlePhase.Commit, NavSettlePhase.Cancel)) {
            for (direction in LayoutDirection.entries) {
                val source = TestScope(direction)
                lateinit var deferredScope: NavTransitionScope
                val base =
                    NavTransition { scope ->
                        deferredScope = scope
                        this
                    }
                val transition = navNoPredictiveTransition(base)
                val spec = transition.motion.commit as NavSettleSpec.Tween
                assertSame(spec, transition.motion.cancel)
                assertEquals(350, spec.durationMillis)
                with(transition) { Modifier.transformEntry(source) }
                source.currentSettle =
                    object : NavSettle {
                        override val phase = settlePhase
                        override val releaseVelocity = 0f
                        override val elapsedMillis: Float get() = source.elapsed
                    }
                for (elapsed in listOf(0f, 80f, 175f, 300f, 350f, 400f)) {
                    source.elapsed = elapsed
                    val eased = spec.easing.transform((elapsed / spec.durationMillis).coerceIn(0f, 1f))
                    val expectedTop = if (settlePhase == NavSettlePhase.Commit) -eased else 0f
                    source.depth =
                        if (settlePhase == NavSettlePhase.Commit) {
                            -0.4f - 0.6f * eased
                        } else {
                            -0.4f * (1f - eased)
                        }
                    assertEquals(expectedTop, deferredScope.relativeDepth, 0.00001f)
                    source.depth += 1f
                    assertEquals((expectedTop + 1f).coerceIn(0f, 1f), transition.scrimFraction(source), 0.00001f)
                }
            }
        }
    }

    @Test
    fun ordinaryNavigationRetainsTheBaseMotionAndScope() {
        val source = TestScope().apply { progress = null }
        lateinit var receivedScope: NavTransitionScope
        val base =
            NavTransition { scope ->
                receivedScope = scope
                this
            }
        val transition = navNoPredictiveTransition(base)
        assertSame(base.motion.programmatic, transition.motion.programmatic)
        for (change in listOf(NavChange.Push, NavChange.Pop)) {
            source.change = change
            with(transition) { Modifier.transformEntry(source) }
            assertSame(source, receivedScope)
        }
    }

    private class TestScope(
        override val layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    ) : NavTransitionScope {
        var depth = -0.4f
        var progress: Float? = 0.4f
        var elapsed = 0f
        var currentSettle: NavSettle? = null
        var depthReads = 0
        var settleReads = 0
        override val relativeDepth: Float get() {
            depthReads++
            return depth
        }
        override val gesture: NavGesture?
            get() = progress?.let { NavGesture(it, NavSwipeEdge.Left, touchY = 0f) }
        override val settle: NavSettle? get() {
            settleReads++
            return currentSettle
        }
        override val role = NavRole.Covered
        override var change: NavChange = NavChange.None
        override val layoutSize = IntSize(1080, 2400)
        override val density = Density(1f)
    }
}
