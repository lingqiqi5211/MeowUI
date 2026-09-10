package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.core.preference.InMemoryPreferenceStore
import io.github.lingqiqi5211.meowui.core.preference.PreferenceKey
import io.github.lingqiqi5211.meowui.core.preference.PreferenceStore
import io.github.lingqiqi5211.meowui.core.preference.PreferenceWriteResult
import io.github.lingqiqi5211.meowui.preference.MeowPreferenceProvider
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceValue
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceWriter
import io.github.lingqiqi5211.meowui.theme.MeowAppearance
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import io.github.lingqiqi5211.meowui.theme.MeowThemeMode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp")
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class MeowUiStateRegressionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun latestNavigationSelectionWinsDuringFadeOut() {
        lateinit var selection: MeowNavigationSelection
        val settled = mutableListOf<Int>()
        compose.setContent {
            selection = rememberMeowNavigationSelection(rememberPagerState { 5 }, settled::add)
            MeowNavigationPager(selection) { Box(Modifier.fillMaxSize()) }
        }
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { selection.select(4) }
        compose.mainClock.advanceTimeBy(64)
        compose.runOnIdle {
            assertEquals(4, selection.index)
            selection.select(2)
            selection.select(1)
        }
        compose.mainClock.advanceTimeBy(1_000)
        compose.runOnIdle {
            assertEquals(1, selection.index)
            assertEquals(1, selection.pagerState.currentPage)
            assertEquals(1f, selection.jumpAlpha.value)
            assertFalse(selection.selecting)
            assertEquals(listOf(1), settled)
        }
    }

    @Test
    fun switchingStoreNeverPublishesThePreviousStoresValue() {
        val key = PreferenceKey("value", 0)
        val first = InMemoryPreferenceStore()
        val second = InMemoryPreferenceStore()
        runBlocking { first.write(key, 1); second.write(key, 2) }
        var store: PreferenceStore by mutableStateOf(first)
        val observed = mutableListOf<Pair<PreferenceStore, Int>>()
        compose.setContent {
            val value by rememberMeowPreferenceValue(key, store)
            val currentStore = store
            SideEffect { observed += currentStore to value }
        }
        compose.runOnIdle { store = second }
        compose.runOnIdle {
            assertTrue(observed.any { it.first === second })
            assertTrue(observed.filter { it.first === second }.all { it.second == 2 })
        }
    }

    @Test
    fun delayedWritesKeepInvocationOrder() {
        val key = PreferenceKey("value", 0)
        val gate = CompletableDeferred<Unit>()
        val backing = InMemoryPreferenceStore()
        val started = mutableListOf<Any>()
        val store = object : PreferenceStore by backing {
            override suspend fun <T : Any> write(key: PreferenceKey<T>, value: T): PreferenceWriteResult {
                started += value
                if (value == 1) gate.await()
                return backing.write(key, value)
            }
        }
        lateinit var write: (Int) -> Unit
        compose.setContent { write = rememberMeowPreferenceWriter(key, store) }
        compose.runOnIdle { write(1); write(2) }
        compose.runOnIdle {
            assertEquals(listOf(1), started)
            gate.complete(Unit)
        }
        compose.runOnIdle {
            assertEquals(listOf(1, 2), started)
            assertEquals(2, backing.read(key))
        }
    }

    @Test
    fun failedSliderWriteClearsDraftAndReportsFailureOnce() {
        val key = PreferenceKey("slider", 0.25f)
        val gate = CompletableDeferred<PreferenceWriteResult>()
        val reports = mutableListOf<PreferenceWriteResult>()
        val store = object : PreferenceStore by InMemoryPreferenceStore() {
            override suspend fun <T : Any> write(key: PreferenceKey<T>, value: T) = gate.await()
        }
        compose.setContent {
            MeowTheme(appearance = appearance) {
                MeowPreferenceProvider(store, reports::add) {
                    MeowSliderPreference(key, "Slider", valueText = { "value=$it" })
                }
            }
        }
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)).performSemanticsAction(SemanticsActions.SetProgress) {
            it(0.75f)
        }
        compose.onNodeWithText("value=0.75").assertExists()
        val failure = PreferenceWriteResult.Failure(IllegalStateException("write failed"))
        compose.runOnIdle { gate.complete(failure) }
        compose.onNodeWithText("value=0.25").assertExists()
        compose.runOnIdle { assertEquals(listOf(failure), reports) }
    }

    @Test
    fun snackbarStyleSwitchReleasesCurrentMessageAndRoutesQueue() {
        val state = MeowSnackbarState()
        lateinit var scope: CoroutineScope
        val results = mutableListOf<MeowSnackbarResult>()
        compose.setContent { scope = rememberCoroutineScope() }
        compose.runOnIdle {
            scope.launch { results += state.show("first") }
            scope.launch { results += state.show("second") }
        }
        compose.runOnIdle {
            assertEquals("first", state.materialHostState.currentSnackbarData?.visuals?.message)
            state.currentStyle = MeowUiStyle.Miuix
        }
        compose.runOnIdle {
            assertEquals(listOf(MeowSnackbarResult.Dismissed), results)
            assertNull(state.materialHostState.currentSnackbarData)
            runBlocking {
                assertEquals("second", state.miuixHostState.newestSnackbarData()?.visuals?.message)
                state.miuixHostState.newestSnackbarData()?.dismiss()
            }
        }
        compose.runOnIdle { assertEquals(2, results.size) }
    }

    @Test
    fun immediateStyleRoundTripStillDismissesTheOldSnackbar() {
        val state = MeowSnackbarState()
        lateinit var scope: CoroutineScope
        var result: MeowSnackbarResult? = null
        compose.setContent { scope = rememberCoroutineScope() }
        compose.runOnIdle { scope.launch { result = state.show("old") } }
        compose.runOnIdle {
            state.currentStyle = MeowUiStyle.Miuix
            state.currentStyle = MeowUiStyle.MaterialExpressive
        }
        compose.runOnIdle {
            assertEquals(MeowSnackbarResult.Dismissed, result)
            assertNull(state.materialHostState.currentSnackbarData)
        }
    }

    @Test
    fun searchOverlayRetainsItsPreferenceProviderInsideAnOffsetScaffold() {
        val key = PreferenceKey("search", 42)
        compose.setContent {
            MeowTheme(appearance = appearance) {
                MeowScaffold(
                    title = "Search",
                    modifier = Modifier.padding(top = 32.dp).testTag("scaffold"),
                ) {
                    MeowPreferenceProvider(remember { InMemoryPreferenceStore() }) {
                        MeowSearchBar(
                            query = "",
                            onQueryChange = {},
                            expanded = true,
                            onExpandedChange = {},
                            autoFocus = false,
                        ) {
                            val value by rememberMeowPreferenceValue(key)
                            Text("search=$value")
                        }
                    }
                }
            }
        }
        compose.onNodeWithText("search=42").assertExists()
        compose.onNodeWithTag("scaffold").assertTopPositionInRootIsEqualTo(32.dp)
    }

    @Test
    fun changingBackHandlingDoesNotRecreateNavigationContent() {
        var handlesBack by mutableStateOf(false)
        var creations = 0
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = NavigationEventDispatcher()
        }
        compose.setContent {
            MeowTheme(appearance = appearance) {
                CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                    MeowNavHost(listOf("page"), onBack = if (handlesBack) ({}) else null) {
                        val instance = remember { ++creations }
                        Text("instance=$instance")
                    }
                }
            }
        }
        compose.onNodeWithText("instance=1").assertExists()
        compose.runOnIdle { handlesBack = true }
        compose.onNodeWithText("instance=1").assertExists()
        compose.runOnIdle { handlesBack = false }
        compose.runOnIdle { assertEquals(1, creations) }
    }

    private val appearance = MeowAppearance(
        themeMode = MeowThemeMode.Light,
        dynamicColor = false,
        blurEnabled = false,
    )
}
