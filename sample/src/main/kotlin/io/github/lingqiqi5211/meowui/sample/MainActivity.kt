package io.github.lingqiqi5211.meowui.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.component.MeowActionPreference
import io.github.lingqiqi5211.meowui.component.MeowAlertDialog
import io.github.lingqiqi5211.meowui.component.MeowAlertStyle
import io.github.lingqiqi5211.meowui.component.MeowAppearancePage
import io.github.lingqiqi5211.meowui.component.MeowBottomSheet
import io.github.lingqiqi5211.meowui.component.MeowBreadcrumbBar
import io.github.lingqiqi5211.meowui.component.MeowBreadcrumbItem
import io.github.lingqiqi5211.meowui.component.MeowButton
import io.github.lingqiqi5211.meowui.component.MeowCard
import io.github.lingqiqi5211.meowui.component.MeowCheckboxPreference
import io.github.lingqiqi5211.meowui.component.MeowColorPaletteDialog
import io.github.lingqiqi5211.meowui.component.MeowColorPaletteMode
import io.github.lingqiqi5211.meowui.component.MeowLoadingDialog
import io.github.lingqiqi5211.meowui.component.MeowMenuCascade
import io.github.lingqiqi5211.meowui.component.MeowMenuItem
import io.github.lingqiqi5211.meowui.component.MeowNavHost
import io.github.lingqiqi5211.meowui.component.MeowNavigationBar
import io.github.lingqiqi5211.meowui.component.MeowNavigationBarStyle
import io.github.lingqiqi5211.meowui.component.MeowNavigationItem
import io.github.lingqiqi5211.meowui.component.MeowPopupPreference
import io.github.lingqiqi5211.meowui.component.MeowPreferenceScreen
import io.github.lingqiqi5211.meowui.component.MeowPreferenceSection
import io.github.lingqiqi5211.meowui.component.MeowPullToRefresh
import io.github.lingqiqi5211.meowui.component.MeowScaffold
import io.github.lingqiqi5211.meowui.component.MeowSearchBar
import io.github.lingqiqi5211.meowui.component.MeowSingleChoiceDialog
import io.github.lingqiqi5211.meowui.component.MeowSliderPreference
import io.github.lingqiqi5211.meowui.component.MeowSnackbarResult
import io.github.lingqiqi5211.meowui.component.MeowSwitchPreference
import io.github.lingqiqi5211.meowui.component.MeowTabRow
import io.github.lingqiqi5211.meowui.component.MeowTabRowStyle
import io.github.lingqiqi5211.meowui.component.MeowTextInputDialog
import io.github.lingqiqi5211.meowui.component.MeowTextInputPreference
import io.github.lingqiqi5211.meowui.component.MeowTip
import io.github.lingqiqi5211.meowui.component.MeowTopBarAction
import io.github.lingqiqi5211.meowui.component.rememberMeowSnackbarState
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.core.preference.InMemoryPreferenceStore
import io.github.lingqiqi5211.meowui.core.preference.PreferenceKey
import io.github.lingqiqi5211.meowui.preference.MeowPreferenceProvider
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceValue
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceWriter
import io.github.lingqiqi5211.meowui.setMeowContent
import io.github.lingqiqi5211.meowui.theme.MeowAppearance
import io.github.lingqiqi5211.meowui.theme.MeowColorSpec
import io.github.lingqiqi5211.meowui.theme.MeowPaletteStyle
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import io.github.lingqiqi5211.meowui.theme.MeowThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private object SamplePreferences {
    const val MaterialStyle = "material3_expressive"
    const val MiuixStyle = "miuix"

    val Style = PreferenceKey("ui_style", MaterialStyle)
    val ThemeMode = PreferenceKey("theme_mode", MeowThemeMode.System.name)
    val DynamicColor = PreferenceKey("dynamic_color", true)
    val PaletteStyle = PreferenceKey("palette_style", MeowPaletteStyle.TonalSpot.name)
    val ColorSpec = PreferenceKey("color_spec", MeowColorSpec.Spec2025.name)
    val SeedColor = PreferenceKey("seed_color", 0xFF7B4DFF.toInt())
    val MiuixMonet = PreferenceKey("miuix_monet", true)
    val AmoledDark = PreferenceKey("amoled_dark", false)
    val PredictiveBack = PreferenceKey("predictive_back", true)
    val InterfaceScale = PreferenceKey("interface_scale", 1f)
    val FloatingLabels = PreferenceKey("floating_labels", true)
    val Blur = PreferenceKey("background_blur", true)
    val FloatingNavigation = PreferenceKey("floating_navigation", true)
    val FeatureEnabled = PreferenceKey("feature_enabled", true)
    val IconRowSwitch = PreferenceKey("icon_row_switch", true)
    val ShowDetails = PreferenceKey("show_details", false)
    val Intensity = PreferenceKey("intensity", 0.65f)
    val Mode = PreferenceKey("mode", "Balanced")
    val Nickname = PreferenceKey("nickname", "Meow")
}

// MeowNavHost 的返回栈元素:枚举可以直接进 Bundle,rememberSaveable 无需自定义 saver。
private enum class SampleRoute { Main, Appearance }

private val navigationItems = listOf(
    MeowNavigationItem(
        label = "Settings",
        icon = Icons.Rounded.Settings,
    ),
    MeowNavigationItem(
        label = "Dialogs",
        icon = Icons.Rounded.ChatBubble,
        badge = "4",
    ),
    MeowNavigationItem(
        label = "About",
        icon = Icons.Rounded.Info,
    ),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMeowContent { SampleApp() }
    }
}

@Composable
private fun SampleApp() {
    val store = remember { InMemoryPreferenceStore() }
    val styleName by rememberMeowPreferenceValue(SamplePreferences.Style, store)
    val themeModeName by rememberMeowPreferenceValue(SamplePreferences.ThemeMode, store)
    val dynamicColor by rememberMeowPreferenceValue(SamplePreferences.DynamicColor, store)
    val paletteStyleName by rememberMeowPreferenceValue(SamplePreferences.PaletteStyle, store)
    val colorSpecName by rememberMeowPreferenceValue(SamplePreferences.ColorSpec, store)
    val seedColorValue by rememberMeowPreferenceValue(SamplePreferences.SeedColor, store)
    val miuixMonet by rememberMeowPreferenceValue(SamplePreferences.MiuixMonet, store)
    val amoledDark by rememberMeowPreferenceValue(SamplePreferences.AmoledDark, store)
    val predictiveBack by rememberMeowPreferenceValue(SamplePreferences.PredictiveBack, store)
    val interfaceScale by rememberMeowPreferenceValue(SamplePreferences.InterfaceScale, store)
    val blurEnabled by rememberMeowPreferenceValue(SamplePreferences.Blur, store)
    val floatingNavigation by rememberMeowPreferenceValue(
        SamplePreferences.FloatingNavigation,
        store,
    )
    val floatingLabels by rememberMeowPreferenceValue(SamplePreferences.FloatingLabels, store)
    val style = if (styleName == SamplePreferences.MiuixStyle) {
        MeowUiStyle.Miuix
    } else {
        MeowUiStyle.MaterialExpressive
    }
    val paletteStyle = MeowPaletteStyle.entries.firstOrNull { it.name == paletteStyleName }
        ?: MeowPaletteStyle.TonalSpot
    val themeMode = MeowThemeMode.entries.firstOrNull { it.name == themeModeName }
        ?: MeowThemeMode.System
    val colorSpec = MeowColorSpec.entries.firstOrNull { it.name == colorSpecName }
        ?: MeowColorSpec.Spec2025
    val appearance = MeowAppearance(
        style = style,
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        seedColor = Color(seedColorValue),
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
        miuixMonetEnabled = miuixMonet,
        amoledDarkEnabled = amoledDark,
        blurEnabled = blurEnabled,
        floatingNavigationBarEnabled = floatingNavigation,
        predictiveBackEnabled = predictiveBack,
        interfaceScale = interfaceScale,
    )

    MeowPreferenceProvider(store) {
        MeowTheme(appearance = appearance) {
            SampleSettings(
                appearance = appearance,
                floatingLabels = floatingLabels,
            )
        }
    }
}

@Composable
private fun SampleSettings(
    appearance: MeowAppearance,
    floatingLabels: Boolean,
) {
    var selectedPage by rememberSaveable { mutableIntStateOf(0) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showAlert by rememberSaveable { mutableStateOf(false) }
    var showWarning by rememberSaveable { mutableStateOf(false) }
    var showChoice by rememberSaveable { mutableStateOf(false) }
    var showInput by rememberSaveable { mutableStateOf(false) }
    var showLoading by rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    var dialogMode by rememberSaveable { mutableStateOf("Balanced") }
    var dialogText by rememberSaveable { mutableStateOf("Meow") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = rememberMeowSnackbarState()

    // NavDisplay 会缓存 entry 的内容 lambda，直接捕获的参数会停在旧快照上
    //（表现为外观页开关点了不刷新）；经 rememberUpdatedState 中转后，
    // 缓存的 lambda 每次重组都能读到最新值。
    val currentAppearance by rememberUpdatedState(appearance)
    val currentFloatingLabels by rememberUpdatedState(floatingLabels)
    val writeStyle = rememberMeowPreferenceWriter(SamplePreferences.Style)
    val writeThemeMode = rememberMeowPreferenceWriter(SamplePreferences.ThemeMode)
    val writeDynamicColor = rememberMeowPreferenceWriter(SamplePreferences.DynamicColor)
    val writeSeedColor = rememberMeowPreferenceWriter(SamplePreferences.SeedColor)
    val writePaletteStyle = rememberMeowPreferenceWriter(SamplePreferences.PaletteStyle)
    val writeColorSpec = rememberMeowPreferenceWriter(SamplePreferences.ColorSpec)
    val writeMiuixMonet = rememberMeowPreferenceWriter(SamplePreferences.MiuixMonet)
    val writeAmoledDark = rememberMeowPreferenceWriter(SamplePreferences.AmoledDark)
    val writePredictiveBack = rememberMeowPreferenceWriter(SamplePreferences.PredictiveBack)
    val writeInterfaceScale = rememberMeowPreferenceWriter(SamplePreferences.InterfaceScale)
    val writeBlur = rememberMeowPreferenceWriter(SamplePreferences.Blur)
    val writeFloatingNavigation = rememberMeowPreferenceWriter(SamplePreferences.FloatingNavigation)
    // MeowNavHost 的返回栈就是调用侧的一个普通列表:推入/弹出即换一个列表,
    // 转场、预测式返回拖拽与页面层级全部由宿主接管。
    // 顶栏菜单形态（照 miuix 官方示例首页）：级联（下钻 / 下沉堆叠两种样式）
    // / 分组单选 / 多选。
    var sinkSort by rememberSaveable { mutableIntStateOf(0) }
    var sinkView by rememberSaveable { mutableIntStateOf(0) }
    var sinkFilter by rememberSaveable { mutableIntStateOf(0) }
    var cascadeSort by rememberSaveable { mutableIntStateOf(0) }
    var cascadeView by rememberSaveable { mutableIntStateOf(0) }
    var cascadeFilter by rememberSaveable { mutableIntStateOf(0) }
    var groupPickA by rememberSaveable { mutableIntStateOf(0) }
    var groupPickB by rememberSaveable { mutableIntStateOf(1) }
    var groupPickC by rememberSaveable { mutableIntStateOf(2) }
    var multiPicks by rememberSaveable { mutableStateOf(setOf("Alpha", "Gamma")) }
    var backStack by rememberSaveable { mutableStateOf(listOf(SampleRoute.Main)) }
    val closeAppearance = {
        if (backStack.size > 1) backStack = backStack.dropLast(1)
    }
    val openAppearance = {
        if (backStack.last() != SampleRoute.Appearance) {
            backStack = backStack + SampleRoute.Appearance
        }
    }
    // remember 保证 lambda 稳定；内部经 currentAppearance 读取最新状态做差量写入。
    val updateAppearance: (MeowAppearance) -> Unit = remember {
        { updated ->
            val current = currentAppearance
            if (updated.style != current.style) {
                writeStyle(
                    if (updated.style == MeowUiStyle.Miuix) {
                        SamplePreferences.MiuixStyle
                    } else {
                        SamplePreferences.MaterialStyle
                    },
                )
            }
            if (updated.themeMode != current.themeMode) {
                writeThemeMode(updated.themeMode.name)
            }
            if (updated.dynamicColor != current.dynamicColor) {
                writeDynamicColor(updated.dynamicColor)
            }
            if (updated.seedColor != current.seedColor) {
                writeSeedColor(updated.seedColor.toArgb())
            }
            if (updated.paletteStyle != current.paletteStyle) {
                writePaletteStyle(updated.paletteStyle.name)
            }
            if (updated.colorSpec != current.colorSpec) {
                writeColorSpec(updated.colorSpec.name)
            }
            if (updated.miuixMonetEnabled != current.miuixMonetEnabled) {
                writeMiuixMonet(updated.miuixMonetEnabled)
            }
            if (updated.amoledDarkEnabled != current.amoledDarkEnabled) {
                writeAmoledDark(updated.amoledDarkEnabled)
            }
            if (updated.predictiveBackEnabled != current.predictiveBackEnabled) {
                writePredictiveBack(updated.predictiveBackEnabled)
            }
            if (updated.interfaceScale != current.interfaceScale) {
                writeInterfaceScale(updated.interfaceScale)
            }
            if (updated.blurEnabled != current.blurEnabled) {
                writeBlur(updated.blurEnabled)
            }
            if (updated.floatingNavigationBarEnabled != current.floatingNavigationBarEnabled) {
                writeFloatingNavigation(updated.floatingNavigationBarEnabled)
            }
        }
    }

    // MeowNavHost:activity 式推入/弹出转场与预测式返回拖拽由宿主内置,
    // 关闭预测式返回时自动退化为普通返回键出栈。
    MeowNavHost(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = closeAppearance,
        predictiveBackEnabled = currentAppearance.predictiveBackEnabled,
    ) { route ->
        when (route) {
            SampleRoute.Main ->
                MeowScaffold(
                    title = navigationItems[selectedPage].label,
                    subtitle = "One page, two native styles",
                    actionItems = listOf(
                        MeowTopBarAction.Icon(
                            icon = Icons.Rounded.Info,
                            contentDescription = "About MeowUI",
                            onClick = { showAlert = true },
                        ),
                        // 级联菜单样式 A（下钻）：子菜单替换主菜单内容，顶部一行回上级。
                        // 选完即收，子菜单里的选项带选中标记。
                        MeowTopBarAction.Menu(
                            icon = Icons.Rounded.Tune,
                            contentDescription = "Cascading options",
                            collapseOnSelection = true,
                            groups = listOf(
                                listOf("Sort by capture date", "Sort by date added")
                                    .mapIndexed { index, label ->
                                        MeowMenuItem(
                                            text = label,
                                            selected = cascadeSort == index,
                                            onClick = { cascadeSort = index },
                                        )
                                    },
                                listOf(
                                    MeowMenuItem(
                                        text = "View mode",
                                        children = listOf("Group by date", "Compact")
                                            .mapIndexed { index, label ->
                                                MeowMenuItem(
                                                    text = label,
                                                    selected = cascadeView == index,
                                                    onClick = { cascadeView = index },
                                                )
                                            },
                                    ),
                                    MeowMenuItem(
                                        text = "Filter",
                                        children = listOf("All items", "Camera album")
                                            .mapIndexed { index, label ->
                                                MeowMenuItem(
                                                    text = label,
                                                    selected = cascadeFilter == index,
                                                    onClick = { cascadeFilter = index },
                                                )
                                            },
                                    ),
                                ),
                            ),
                        ),
                        // 级联菜单样式 B（下沉堆叠）：主菜单原地下沉压暗，子菜单从父项
                        // 那一行长出来盖在上面——复刻 miuix 级联弹窗的观感。故意不自动
                        // 收起：选完留在原地才看得出勾选有没有跟着变。
                        MeowTopBarAction.Menu(
                            icon = Icons.Rounded.Layers,
                            contentDescription = "Cascading options (sink)",
                            cascade = MeowMenuCascade.Sink,
                            collapseOnSelection = false,
                            groups = listOf(
                                listOf("Sort by capture date", "Sort by date added")
                                    .mapIndexed { index, label ->
                                        MeowMenuItem(
                                            text = label,
                                            selected = sinkSort == index,
                                            onClick = { sinkSort = index },
                                        )
                                    },
                                listOf(
                                    MeowMenuItem(
                                        text = "View mode",
                                        children = listOf("Group by date", "Compact")
                                            .mapIndexed { index, label ->
                                                MeowMenuItem(
                                                    text = label,
                                                    selected = sinkView == index,
                                                    onClick = { sinkView = index },
                                                )
                                            },
                                    ),
                                    MeowMenuItem(
                                        text = "Filter",
                                        children = listOf("All items", "Camera album")
                                            .mapIndexed { index, label ->
                                                MeowMenuItem(
                                                    text = label,
                                                    selected = sinkFilter == index,
                                                    onClick = { sinkFilter = index },
                                                )
                                            },
                                    ),
                                ),
                            ),
                        ),
                        // 分组单选：三组各自单选，选完不收起，可连续改几组。
                        MeowTopBarAction.Menu(
                            icon = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Sort options",
                            collapseOnSelection = false,
                            groups = listOf(
                                listOf("Selection A-1", "Selection A-2")
                                    .mapIndexed { index, label ->
                                        MeowMenuItem(
                                            text = label,
                                            selected = groupPickA == index,
                                            onClick = { groupPickA = index },
                                        )
                                    },
                                listOf("Selection B-1", "Selection B-2", "Selection B-3")
                                    .mapIndexed { index, label ->
                                        MeowMenuItem(
                                            text = label,
                                            selected = groupPickB == index,
                                            onClick = { groupPickB = index },
                                        )
                                    },
                                listOf("C-1", "C-2", "C-3", "C-4")
                                    .mapIndexed { index, label ->
                                        MeowMenuItem(
                                            text = label,
                                            // 奇数项禁用，展示单项 enabled。
                                            enabled = index % 2 == 0,
                                            selected = groupPickC == index,
                                            onClick = { groupPickC = index },
                                        )
                                    },
                            ),
                        ),
                        // 多选：✓ 可多个，选完不收起。
                        MeowTopBarAction.Menu(
                            icon = Icons.Rounded.Checklist,
                            contentDescription = "Multi selection",
                            collapseOnSelection = false,
                            groups = listOf(
                                listOf("Alpha", "Beta"),
                                listOf("Gamma", "Delta", "Epsilon"),
                            ).map { group ->
                                group.map { label ->
                                    MeowMenuItem(
                                        text = label,
                                        selected = label in multiPicks,
                                        onClick = {
                                            multiPicks = if (label in multiPicks) {
                                                multiPicks - label
                                            } else {
                                                multiPicks + label
                                            }
                                        },
                                    )
                                }
                            },
                        ),
                        MeowTopBarAction.Menu(
                            icon = Icons.Rounded.MoreVert,
                            contentDescription = "More actions",
                            items = listOf(
                                MeowMenuItem(
                                    text = "Open bottom sheet",
                                    icon = Icons.Rounded.ViewAgenda,
                                    onClick = { showBottomSheet = true },
                                ),
                                MeowMenuItem(
                                    text = "About MeowUI",
                                    icon = Icons.Rounded.Info,
                                    onClick = { showAlert = true },
                                ),
                                // 级联子菜单示例:Miuix 原生堆叠弹窗,Material 同弹窗内下钻。
                                MeowMenuItem(
                                    text = "More",
                                    children = listOf(
                                        MeowMenuItem(
                                            text = "Warning dialog",
                                            onClick = { showWarning = true },
                                        ),
                                        MeowMenuItem(
                                            text = "Loading dialog",
                                            onClick = { showLoading = true },
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    bottomBar = {
                        MeowNavigationBar(
                            items = navigationItems,
                            selectedIndex = selectedPage,
                            onItemSelected = { selectedPage = it },
                            style = if (currentAppearance.floatingNavigationBarEnabled) {
                                MeowNavigationBarStyle.Floating
                            } else {
                                MeowNavigationBarStyle.Standard
                            },
                            showFloatingLabels = currentFloatingLabels,
                        )
                    },
                    snackbarState = snackbarState,
                ) { _ ->
                    MeowPullToRefresh(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            if (!isRefreshing) {
                                isRefreshing = true
                                coroutineScope.launch {
                                    delay(900)
                                    isRefreshing = false
                                    snackbarState.show("Preferences refreshed")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        AnimatedContent(
                            targetState = selectedPage,
                            transitionSpec = {
                                val direction = if (targetState > initialState) 1 else -1
                                (slideInHorizontally { it * direction / 5 } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it * direction / 5 } + fadeOut())
                            },
                            label = "sample-page",
                        ) { page ->
                            when (page) {
                                0 -> SettingsPage(
                                    selectedTab = selectedTab,
                                    onTabSelected = { selectedTab = it },
                                    onOpenAppearance = openAppearance,
                                )

                                1 -> DialogsPage(
                                    onAlert = { showAlert = true },
                                    onWarning = { showWarning = true },
                                    onChoice = { showChoice = true },
                                    onInput = { showInput = true },
                                    onLoading = { showLoading = true },
                                    onBottomSheet = { showBottomSheet = true },
                                    onSnackbar = {
                                        coroutineScope.launch {
                                            val result = snackbarState.show(
                                                message = "Mode saved",
                                                actionLabel = "Undo",
                                            )
                                            if (result == MeowSnackbarResult.ActionPerformed) {
                                                snackbarState.show("Change undone")
                                            }
                                        }
                                    },
                                )

                                else -> AboutPage()
                            }
                        }
                    }
                    // 底部抽屉:Miuix 分支经 Scaffold 的 popup host 渲染,必须放在 Scaffold 组合
                    // 子树内——与 Scaffold 平级时 Miuix 风格下不会显示。
                    MeowBottomSheet(
                        show = showBottomSheet,
                        title = "Bottom Sheet",
                        onDismissRequest = { showBottomSheet = false },
                    ) {
                        // 抽屉内容的左右与底部边距由组件自身提供,这里不再叠加。
                        MeowPreferenceSection(title = "Shared content") {
                            MeowActionPreference(
                                title = "Close sheet",
                                summary = "Material and Miuix render this container separately.",
                                onClick = { showBottomSheet = false },
                            )
                        }
                    }
                }

            SampleRoute.Appearance ->
                // 头图使用库内置的 MeowAppearancePreview（按手机/折叠/平板自适应），
                // 不需要时传 showPreview = false，或用 previewContent 自定义。
                MeowAppearancePage(
                    appearance = currentAppearance,
                    onAppearanceChange = updateAppearance,
                    onBackClick = closeAppearance,
                )
        }
    }

    MeowAlertDialog(
        show = showAlert,
        title = "MeowUI",
        message = "The same state drives a Material 3 Expressive dialog or a Miuix window dialog.",
        onConfirm = { showAlert = false },
        onDismissRequest = { showAlert = false },
        cancelText = null,
    )
    MeowAlertDialog(
        show = showWarning,
        title = "Disable this feature?",
        message = "This warning keeps the same behavior while each style uses its own visual language.",
        style = MeowAlertStyle.Warning,
        confirmText = "Disable",
        onConfirm = { showWarning = false },
        onDismissRequest = { showWarning = false },
    )
    MeowSingleChoiceDialog(
        show = showChoice,
        title = "Choose a mode",
        selected = dialogMode,
        options = listOf("Balanced", "Performance", "Battery saver"),
        onSelected = {
            dialogMode = it
            showChoice = false
        },
        onDismissRequest = { showChoice = false },
    )
    MeowTextInputDialog(
        show = showInput,
        title = "Edit nickname",
        initialValue = dialogText,
        placeholder = "At least 2 characters",
        allowBlank = false,
        validator = { value ->
            if (value.trim().length < 2) "Enter at least 2 characters" else null
        },
        onConfirm = {
            dialogText = it
            showInput = false
        },
        onDismissRequest = { showInput = false },
    )
    MeowLoadingDialog(
        show = showLoading,
        title = "Loading settings",
        message = "Press back or tap outside to close this sample.",
        onDismissRequest = { showLoading = false },
    )
}

@Composable
private fun SettingsPage(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onOpenAppearance: () -> Unit,
) {
    MeowPreferenceScreen {
        MeowTip(
            title = "Shared setting page",
            message = "Pull down to preview refresh. Every setting below is bound directly to a key.",
        )
        MeowTabRow(
            tabs = listOf("Appearance", "Controls"),
            selectedIndex = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.fillMaxWidth(),
            style = MeowTabRowStyle.Contour,
        )

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * direction / 5 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it * direction / 5 } + fadeOut())
            },
            label = "settings-tab",
        ) { tab ->
            if (tab == 0) {
                Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    // KernelSU 首页式状态卡:容器色由运行状态决定,内容色一并传入,
                    // 卡内文字与图标默认就是可读的对比色。
                    MeowCard(
                        containerColor = MeowTheme.colors.primary,
                        contentColor = MeowTheme.colors.onPrimary,
                        onClick = {},
                    ) {
                        BasicText(
                            text = "Working",
                            style = MeowTheme.typography.title
                                .copy(color = MeowTheme.colors.onPrimary),
                        )
                        BasicText(
                            text = "MeowUI sample · two native styles",
                            style = MeowTheme.typography.summary
                                .copy(color = MeowTheme.colors.onPrimary),
                        )
                    }
                    // 警告色调状态卡:同一张卡换 error 语义色即是 KernelSU 的警告卡。
                    MeowCard(
                        containerColor = MeowTheme.colors.error,
                        contentColor = MeowTheme.colors.onError,
                    ) {
                        BasicText(
                            text = "Update required",
                            style = MeowTheme.typography.title
                                .copy(color = MeowTheme.colors.onError),
                        )
                        BasicText(
                            text = "Warning tone via container and content colors",
                            style = MeowTheme.typography.summary
                                .copy(color = MeowTheme.colors.onError),
                        )
                    }
                    // 分段拼卡:index/count 让相邻卡片在 Material 下拼成一张分组卡片
                    // （首尾大圆角、中间小圆角），Miuix 下保持独立卡片。适合条目数由
                    // 数据决定的列表卡。外面包一层 Column,避免外层 spacedBy 插进组内。
                    Column {
                        val modules = listOf(
                            "Meow theme engine" to "Enabled",
                            "Paw gesture pack" to "Enabled",
                            "Whisker debug bridge" to "Disabled",
                        )
                        modules.forEachIndexed { index, (name, state) ->
                            MeowCard(index = index, count = modules.size) {
                                BasicText(
                                    text = name,
                                    style = MeowTheme.typography.title
                                        .copy(color = MeowTheme.colors.onSurface),
                                )
                                BasicText(
                                    text = state,
                                    style = MeowTheme.typography.summary
                                        .copy(color = MeowTheme.colors.onSurfaceVariant),
                                )
                            }
                        }
                    }
                    AppearanceSections(onOpenAppearance = onOpenAppearance)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    // 搜索框示例:点一下输入条即展开(自动抢焦点弹键盘),结果就地在下方
                    // 展开;取消/系统返回由组件自己清词收起,这里只持有状态并做过滤。
                    // 查询词为空时照 KernelSU 的做法给一份默认结果,而不是空白一片。
                    var searchQuery by rememberSaveable { mutableStateOf("") }
                    var searchExpanded by rememberSaveable { mutableStateOf(false) }
                    MeowSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        expanded = searchExpanded,
                        onExpandedChange = { searchExpanded = it },
                        placeholder = "Search controls",
                    ) {
                        // 空查询词不铺结果：真实接入里候选集可能是全部已安装应用，
                        // 一展开就把它们全画出来既浪费一次列表构建，也把“输什么”这件事埋了。
                        val matches = if (searchQuery.isBlank()) {
                            emptyList()
                        } else {
                            listOf(
                                "Enable feature",
                                "Show advanced details",
                                "Intensity",
                                "Mode",
                                "Nickname",
                            ).filter { it.contains(searchQuery, ignoreCase = true) }
                        }
                        if (matches.isEmpty()) {
                            BasicText(
                                text = if (searchQuery.isBlank()) {
                                    "Type to search controls"
                                } else {
                                    "No matching control"
                                },
                                style = MeowTheme.typography.summary
                                    .copy(color = MeowTheme.colors.onSurfaceVariant),
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 12.dp,
                                ),
                            )
                        } else {
                            // 结果用普通分组：Material 下每行自己一块分段卡片，
                            // Miuix 下是一张原生分组卡，和页面里的列表长得一样。
                            MeowPreferenceSection {
                                matches.forEach { name ->
                                    MeowActionPreference(
                                        title = name,
                                        // 选中某项:留下查询词并收起,演示「调用侧程序化收起」
                                        // 与组件自己的取消(会清词)是两条不同的路径。
                                        onClick = {
                                            searchQuery = name
                                            searchExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // 面包屑：文件选择器那套路径导航。点某一段就回到那一级，
                    // 路径长到超出屏宽时横向滚动而不是折叠，高亮段会自动滚到中间。
                    var breadcrumbPath by rememberSaveable {
                        mutableStateOf(listOf("Internal storage", "Android", "data"))
                    }
                    MeowPreferenceSection(title = "Breadcrumb") {
                        MeowActionPreference(
                            title = "Go deeper",
                            summary = breadcrumbPath.joinToString("/"),
                            onClick = {
                                breadcrumbPath = breadcrumbPath +
                                    SampleBreadcrumbSegments[
                                        breadcrumbPath.size % SampleBreadcrumbSegments.size,
                                    ]
                            },
                        )
                    }
                    MeowBreadcrumbBar(
                        items = breadcrumbPath.map { segment -> MeowBreadcrumbItem(segment) },
                        onItemClick = { index ->
                            breadcrumbPath = breadcrumbPath.take(index + 1)
                        },
                    )

                    // Spinner 形态（照 miuix 示例 Spinner 区）：带图标+副文本的选项、
                    // 分组不收起、单项禁用。
                    var spinnerColor by rememberSaveable { mutableStateOf("Red") }
                    var spinnerGrouped by rememberSaveable { mutableStateOf("Fast") }
                    MeowPreferenceSection(title = "Spinner") {
                        MeowPopupPreference(
                            title = "Accent swatch",
                            value = spinnerColor,
                            options = listOf("Red", "Green", "Blue", "Yellow"),
                            onValueChange = { spinnerColor = it },
                            optionSummary = { "The $it swatch" },
                            optionLeading = { option ->
                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .size(20.dp)
                                        .background(
                                            color = when (option) {
                                                "Red" -> Color(0xFFFF5B29)
                                                "Green" -> Color(0xFF36D167)
                                                "Blue" -> Color(0xFF3482FF)
                                                else -> Color(0xFFFFB21D)
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                        ),
                                )
                            },
                        )
                        MeowPopupPreference(
                            title = "Grouped picker",
                            value = spinnerGrouped,
                            options = emptyList(),
                            groups = listOf(
                                listOf("Fast", "Balanced"),
                                listOf("Slow", "Manual", "Off"),
                            ),
                            onValueChange = { spinnerGrouped = it },
                            // 选完不收：可连着比较几个选项的效果。
                            collapseOnSelection = false,
                            optionEnabled = { it != "Manual" },
                        )
                    }

                    MeowPreferenceSection(title = "Controls") {
                        MeowSwitchPreference(
                            title = "Enable feature",
                            summary = "The same page code is used by both styles",
                            key = SamplePreferences.FeatureEnabled,
                        )
                        MeowCheckboxPreference(
                            title = "Show advanced details",
                            key = SamplePreferences.ShowDetails,
                        )
                        MeowSliderPreference(
                            title = "Intensity",
                            key = SamplePreferences.Intensity,
                            valueText = { "${(it * 100).toInt()}%" },
                        )
                        MeowPopupPreference(
                            title = "Mode",
                            key = SamplePreferences.Mode,
                            options = listOf("Balanced", "Performance", "Battery saver"),
                        )
                        MeowTextInputPreference(
                            title = "Nickname",
                            key = SamplePreferences.Nickname,
                            placeholder = "Enter a name",
                            allowBlank = false,
                            validator = { value ->
                                if (value.trim().length < 2) "Enter at least 2 characters" else null
                            },
                        )
                    }

                    // HyperCeiler 首页式应用列表：大图标 + 应用名/包名，图标可为空。
                    MeowPreferenceSection(title = "App list") {
                        MeowActionPreference(
                            title = "Meow Music",
                            summary = "com.meow.music",
                            leading = { SampleAppIcon(Color(0xFF7B4DFF), "M") },
                            onClick = {},
                        )
                        MeowActionPreference(
                            title = "Paw Gallery",
                            summary = "com.meow.gallery",
                            leading = { SampleAppIcon(Color(0xFF006D39), "P") },
                            onClick = {},
                        )
                        MeowActionPreference(
                            title = "No icon app",
                            summary = "com.meow.plain",
                            onClick = {},
                        )
                    }

                    // KernelSU 设置式选项列表：小图标 + 常规列表项，图标可为空。
                    MeowPreferenceSection(title = "Options") {
                        MeowActionPreference(
                            title = "Theme palette",
                            summary = "Seed color and palette style",
                            leading = { SampleOptionIcon(Icons.Rounded.Palette) },
                            onClick = {},
                        )
                        MeowSwitchPreference(
                            title = "Notifications",
                            summary = "Row with a leading icon and a switch",
                            leading = { SampleOptionIcon(Icons.Rounded.Notifications) },
                            key = SamplePreferences.IconRowSwitch,
                        )
                        MeowActionPreference(
                            title = "Without icon",
                            summary = "The leading slot is optional",
                            onClick = {},
                        )
                    }

                    // 按钮不属于列表分组，放在分组外面。
                    val resetIntensity = rememberMeowPreferenceWriter(SamplePreferences.Intensity)
                    val resetMode = rememberMeowPreferenceWriter(SamplePreferences.Mode)
                    MeowButton(
                        text = "Reset controls",
                        onClick = {
                            resetIntensity(SamplePreferences.Intensity.defaultValue)
                            resetMode(SamplePreferences.Mode.defaultValue)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceSections(onOpenAppearance: () -> Unit) {
    MeowPreferenceSection(title = "Appearance") {
        MeowActionPreference(
            title = "Theme color",
            summary = "Colors, theme mode, interface style, gestures, and scale",
            value = "Open",
            onClick = onOpenAppearance,
        )
        MeowSwitchPreference(
            title = "Background blur",
            summary = "Both styles use real blur when supported and an opaque fallback otherwise",
            key = SamplePreferences.Blur,
        )
        MeowSwitchPreference(
            title = "Floating bottom bar",
            summary = "Switch between each style’s standard and floating navigation",
            key = SamplePreferences.FloatingNavigation,
        )
        MeowSwitchPreference(
            title = "Floating bar labels",
            summary = "Show names under the floating bottom bar icons",
            key = SamplePreferences.FloatingLabels,
        )
    }
}

/** 调色盘示例行尾的当前颜色圆点。 */
@Composable
private fun SamplePaletteDot(color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color),
    )
}

private val SampleBreadcrumbSegments = listOf(
    "com.meow.sample",
    "files",
    "cache",
    "a-very-long-folder-name-that-gets-truncated",
)

/** 示例用的“应用图标”：40dp 圆形色块加首字母，实际应用应换成真实应用图标。 */
@Composable
private fun SampleAppIcon(color: Color, letter: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = letter,
            style = MeowTheme.typography.title.copy(color = Color.White),
        )
    }
}

/** 示例用的选项图标：24dp 矢量图标，跟随主题的次级前景色。 */
@Composable
private fun SampleOptionIcon(icon: ImageVector) {
    Image(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        colorFilter = ColorFilter.tint(MeowTheme.colors.onSurfaceVariant),
    )
}

@Composable
private fun DialogsPage(
    onAlert: () -> Unit,
    onWarning: () -> Unit,
    onChoice: () -> Unit,
    onInput: () -> Unit,
    onLoading: () -> Unit,
    onBottomSheet: () -> Unit,
    onSnackbar: () -> Unit,
) {
    var showSliderPalette by rememberSaveable { mutableStateOf(false) }
    var showGridPalette by rememberSaveable { mutableStateOf(false) }
    var paletteColor by rememberSaveable { mutableIntStateOf(0xFF7B4DFF.toInt()) }

    MeowColorPaletteDialog(
        show = showSliderPalette,
        initialColor = Color(paletteColor),
        onConfirm = { color ->
            paletteColor = color.toArgb()
            showSliderPalette = false
        },
        onDismissRequest = { showSliderPalette = false },
    )
    MeowColorPaletteDialog(
        show = showGridPalette,
        initialColor = Color(paletteColor),
        mode = MeowColorPaletteMode.Grid,
        onConfirm = { color ->
            paletteColor = color.toArgb()
            showGridPalette = false
        },
        onDismissRequest = { showGridPalette = false },
    )

    MeowPreferenceScreen {
        MeowPreferenceSection(title = "Dialogs") {
            MeowActionPreference(
                title = "Information dialog",
                summary = "Single primary action",
                onClick = onAlert,
            )
            MeowActionPreference(
                title = "Warning dialog",
                summary = "Destructive emphasis",
                onClick = onWarning,
            )
            MeowActionPreference(
                title = "Single-choice dialog",
                summary = "Changes only after confirmation",
                onClick = onChoice,
            )
            MeowActionPreference(
                title = "Validated text dialog",
                summary = "Invalid input disables confirmation",
                onClick = onInput,
            )
            MeowActionPreference(
                title = "Loading dialog",
                summary = "Optional dismissal behavior",
                onClick = onLoading,
            )
            MeowActionPreference(
                title = "Color palette (sliders)",
                summary = "Pick any color with HSV sliders",
                trailing = { SamplePaletteDot(Color(paletteColor)) },
                onClick = { showSliderPalette = true },
            )
            MeowActionPreference(
                title = "Color palette (grid)",
                summary = "Pick from the HSV color grid",
                trailing = { SamplePaletteDot(Color(paletteColor)) },
                onClick = { showGridPalette = true },
            )
        }
        MeowPreferenceSection(title = "Window") {
            MeowActionPreference(
                title = "Open Bottom Sheet",
                summary = "Uses the native container for each style",
                onClick = onBottomSheet,
            )
        }
        MeowPreferenceSection(title = "Feedback") {
            MeowActionPreference(
                title = "Show snackbar",
                summary = "Cross-style snackbar with an undo action",
                onClick = onSnackbar,
            )
        }
    }
}

@Composable
private fun AboutPage() {
    val uriHandler = LocalUriHandler.current
    var showUpdateSheet by rememberSaveable { mutableStateOf(false) }
    var showLicenses by rememberSaveable { mutableStateOf(false) }

    MeowPreferenceScreen {
        AboutHero()
        MeowPreferenceSection(title = "About") {
            MeowActionPreference(
                title = "View source code",
                summary = "github.com/lingqiqi5211/MeowUI",
                onClick = { uriHandler.openUri("https://github.com/lingqiqi5211/MeowUI") },
            )
            MeowActionPreference(
                title = "Open source licenses",
                summary = "Compose · Miuix · materialKolor · libxposed",
                onClick = { showLicenses = true },
            )
            MeowActionPreference(
                title = "Get updates",
                summary = "Releases and project home",
                onClick = { showUpdateSheet = true },
            )
        }
    }

    MeowAlertDialog(
        show = showLicenses,
        title = "Open source licenses",
        message = "Jetpack Compose / Material 3 Expressive · Apache-2.0\n" +
            "Miuix · Apache-2.0\n" +
            "materialKolor · MIT\n" +
            "libxposed API · Apache-2.0",
        onConfirm = { showLicenses = false },
        onDismissRequest = { showLicenses = false },
        cancelText = null,
    )
    MeowBottomSheet(
        show = showUpdateSheet,
        title = "Get updates",
        onDismissRequest = { showUpdateSheet = false },
    ) {
        // 左右与底部边距由 MeowBottomSheet 自身提供,这里只留上下留白。
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MeowButton(
                text = "GitHub Releases",
                onClick = { uriHandler.openUri("https://github.com/lingqiqi5211/MeowUI/releases") },
                modifier = Modifier.fillMaxWidth(),
            )
            MeowButton(
                text = "Project home",
                onClick = { uriHandler.openUri("https://github.com/lingqiqi5211/MeowUI") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AboutHero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MeowTheme.colors.primary),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                imageVector = Icons.Rounded.Pets,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                colorFilter = ColorFilter.tint(MeowTheme.colors.onPrimary),
            )
        }
        Spacer(Modifier.height(12.dp))
        BasicText(
            text = "MeowUI",
            style = MeowTheme.typography.pageTitle.copy(color = MeowTheme.colors.onBackground),
        )
        Spacer(Modifier.height(4.dp))
        BasicText(
            text = "Android 8+ · Material 3 Expressive · Miuix",
            style = MeowTheme.typography.summary.copy(color = MeowTheme.colors.onSurfaceVariant),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MaterialPreview() {
    val store = remember { InMemoryPreferenceStore() }
    val appearance = MeowAppearance(
        style = MeowUiStyle.MaterialExpressive,
        themeMode = MeowThemeMode.Light,
        dynamicColor = false,
        blurEnabled = false,
    )
    MeowPreferenceProvider(store) {
        MeowTheme(appearance = appearance) {
            SampleSettings(
                appearance = appearance,
                floatingLabels = true,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MiuixPreview() {
    val store = remember { InMemoryPreferenceStore() }
    val appearance = MeowAppearance(
        style = MeowUiStyle.Miuix,
        themeMode = MeowThemeMode.Light,
        dynamicColor = false,
        blurEnabled = false,
    )
    MeowPreferenceProvider(store) {
        MeowTheme(appearance = appearance) {
            SampleSettings(
                appearance = appearance,
                floatingLabels = true,
            )
        }
    }
}
