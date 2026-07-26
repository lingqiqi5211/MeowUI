package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold as MaterialScaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior as MaterialTopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior as MiuixTopAppBarScrollBehavior

@Stable
data class MeowScaffoldEffect(
    val contentModifier: Modifier = Modifier,
    val topBarModifier: Modifier = Modifier,
    val topBarContainerColor: Color? = null,
    val bottomBarModifier: Modifier = Modifier,
    val bottomBarContainerColor: Color? = null,
    val floatingBottomBarModifier: Modifier = bottomBarModifier,
    val floatingBottomBarContainerColor: Color? = bottomBarContainerColor,
)

@Stable
internal data class MeowScrollContext(
    val nestedScrollConnection: NestedScrollConnection? = null,
    val materialTopBar: MaterialTopAppBarScrollBehavior? = null,
    val miuixTopBar: MiuixTopAppBarScrollBehavior? = null,
)

internal val LocalMeowScaffoldEffect = staticCompositionLocalOf { MeowScaffoldEffect() }
internal val LocalMeowScrollContext = staticCompositionLocalOf { MeowScrollContext() }

/** MeowScaffold 提供给内容区的 PaddingValues，MeowPreferenceScreen 默认自动消费。 */
internal val LocalMeowScaffoldContentPadding = compositionLocalOf { PaddingValues(0.dp) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeowScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    onBackClick: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actionItems: List<MeowTopBarAction> = emptyList(),
    bottomBar: @Composable () -> Unit = {},
    snackbarState: MeowSnackbarState? = null,
    effect: MeowScaffoldEffect = MeowScaffoldEffect(),
    content: @Composable (PaddingValues) -> Unit,
) {
    CompositionLocalProvider(LocalMeowScaffoldEffect provides effect) {
        MeowStyleContent(
            materialExpressive = {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
                    rememberTopAppBarState(),
                )
                CompositionLocalProvider(
                    LocalMeowScrollContext provides MeowScrollContext(
                        nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                        materialTopBar = scrollBehavior,
                    ),
                ) {
                    MaterialScaffold(
                        modifier = modifier,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            MeowTopBar(
                                title = title,
                                subtitle = subtitle,
                                onBackClick = onBackClick,
                                navigationIcon = navigationIcon,
                                actionItems = actionItems,
                            )
                        },
                        bottomBar = bottomBar,
                        snackbarHost = {
                            snackbarState?.let { MeowSnackbarHost(it) }
                        },
                        content = { paddingValues ->
                            MeowScaffoldContent(
                                paddingValues = paddingValues,
                                effect = effect,
                                content = content,
                            )
                        },
                    )
                }
            },
            miuix = {
                val scrollBehavior = MiuixScrollBehavior()
                CompositionLocalProvider(
                    LocalMeowScrollContext provides MeowScrollContext(
                        nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                        miuixTopBar = scrollBehavior,
                    ),
                ) {
                    MiuixScaffold(
                        modifier = modifier,
                        topBar = {
                            MeowTopBar(
                                title = title,
                                subtitle = subtitle,
                                onBackClick = onBackClick,
                                navigationIcon = navigationIcon,
                                actionItems = actionItems,
                            )
                        },
                        bottomBar = bottomBar,
                        snackbarHost = {
                            snackbarState?.let { MeowSnackbarHost(it) }
                        },
                        content = { paddingValues ->
                            MeowScaffoldContent(
                                paddingValues = paddingValues,
                                effect = effect,
                                content = content,
                            )
                        },
                    )
                }
            },
        )
    }
}

@Composable
private fun MeowScaffoldContent(
    paddingValues: PaddingValues,
    effect: MeowScaffoldEffect,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(effect.contentModifier),
    ) {
        CompositionLocalProvider(LocalMeowScaffoldContentPadding provides paddingValues) {
            content(paddingValues)
        }
    }
}
