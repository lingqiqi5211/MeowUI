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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.foundation.background
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
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

/**
 * 内容区的图层快照,供悬浮底栏等浮层做背景模糊。
 *
 * 只有 MeowScaffold 的内容会被捕获;不在 scaffold 里的浮层拿到 null,自动退回
 * 不透明底色。
 */
internal val LocalMeowBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * 顶栏与悬浮底栏共用的磨砂参数。
 *
 * 大半径 + 较高的底色不透明度呈现的是 iOS 式磨砂:内容被充分糊开,再被底色压住,
 * 不会有透镜式的边缘拖影,也不会漏出清晰的色块。
 */
internal const val MeowBlurSurfaceAlpha = 0.65f
internal val MeowBlurRadius = 25.dp
/**
 * scaffold 内的浮层插槽。
 *
 * 给需要盖住整个页面（含顶栏、底栏与系统栏）的展开态用，例如搜索框展开后
 * 的搜索面。画在 scaffold 自己的窗口里而不是开一个 Popup：inset 与页面完全一致，
 * 不会出现额外窗口被系统栏裁掉、或反过来越过屏幕边界时的各种怪象。
 */
@Stable
internal class MeowOverlayHostState {
    var content: (@Composable () -> Unit)? by mutableStateOf(null)
}

internal val LocalMeowOverlayHost = staticCompositionLocalOf<MeowOverlayHostState?> { null }

internal val LocalMeowScrollContext = staticCompositionLocalOf { MeowScrollContext() }

/**
 * 内容当前是否画在底部抽屉里。
 *
 * Miuix 抽屉的底色与页面里的分组卡底色是同一档,卡片贴在抽屉上几乎看不出边界。
 * 抽屉据此把分组卡抬高一档,页面里则保持原样。
 */
internal val LocalMeowOnSheet = compositionLocalOf { false }

/** MeowScaffold 提供给内容区的 PaddingValues，MeowPreferenceScreen 默认自动消费。 */
internal val LocalMeowScaffoldContentPadding = compositionLocalOf { PaddingValues(0.dp) }

/**
 * 把页面自己的滚动容器接到 MeowScaffold 顶栏的折叠行为上。
 *
 * MeowPreferenceScreen、MeowPullToRefresh 等库内容器自动接入；调用侧自建的
 * 滚动容器（LazyColumn / verticalScroll）在其祖先上加这个 modifier，向上
 * 滚动时顶栏才会跟着收起。不在 MeowScaffold 内时为空操作。
 */
@Composable
fun Modifier.meowScaffoldScroll(): Modifier {
    val connection = LocalMeowScrollContext.current.nestedScrollConnection ?: return this
    return this.nestedScroll(connection)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeowScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    onBackClick: (() -> Unit)? = null,
    /**
     * Applied to the back button built from [onBackClick].
     *
     * Ignored when [navigationIcon] is supplied, since that content brings its own
     * modifier.
     */
    navigationModifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actionItems: List<MeowTopBarAction> = emptyList(),
    bottomBar: @Composable () -> Unit = {},
    snackbarState: MeowSnackbarState? = null,
    effect: MeowScaffoldEffect = MeowScaffoldEffect(),
    content: @Composable (PaddingValues) -> Unit,
) {
    val backdrop = rememberLayerBackdrop()
    val overlayHost = remember { MeowOverlayHostState() }
    CompositionLocalProvider(
        LocalMeowScaffoldEffect provides effect,
        LocalMeowBackdrop provides backdrop,
        LocalMeowOverlayHost provides overlayHost,
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
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
                                navigationModifier = navigationModifier,
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
                                navigationModifier = navigationModifier,
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
        // 浮层最后画，因此盖在顶栏、底栏与内容之上。
        overlayHost.content?.invoke()
      }
    }
}

@Composable
private fun MeowScaffoldContent(
    paddingValues: PaddingValues,
    effect: MeowScaffoldEffect,
    content: @Composable (PaddingValues) -> Unit,
) {
    // 页面底色必须画进被捕获的图层:scaffold 自己刷的背景不在快照里,只采内容
    // 会让模糊混入透明底,磨砂出来发灰发暗、和页面色不搭。
    // 颜色必须与两个分支 scaffold 实际的 containerColor 一致:Material 分支上面
    // 传的是 surfaceContainer,Miuix 分支用 MiuixScaffold 的默认值 surface。刷错
    // role 会盖掉页面本来的底色(比如 Miuix 浅色下 background 更白,整页发白)。
    val pageColor = when (MeowTheme.style) {
        MeowUiStyle.MaterialExpressive -> MaterialTheme.colorScheme.surfaceContainer
        MeowUiStyle.Miuix -> MiuixTheme.colorScheme.surface
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            // 悬浮底栏与顶栏的背景模糊取自这份图层快照。
            .layerBackdrop(LocalMeowBackdrop.current ?: rememberLayerBackdrop())
            .background(pageColor)
            .then(effect.contentModifier),
    ) {
        CompositionLocalProvider(LocalMeowScaffoldContentPadding provides paddingValues) {
            content(paddingValues)
        }
    }
}
