package io.github.lingqiqi5211.meowui.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.animation.core.Easing
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.blur.rememberMeowBlurScaffoldEffect
import io.github.lingqiqi5211.meowui.component.MeowActionPreference
import io.github.lingqiqi5211.meowui.component.MeowAppearancePage
import io.github.lingqiqi5211.meowui.component.MeowAlertDialog
import io.github.lingqiqi5211.meowui.component.MeowAlertStyle
import io.github.lingqiqi5211.meowui.component.MeowBottomSheet
import io.github.lingqiqi5211.meowui.component.MeowButton
import io.github.lingqiqi5211.meowui.component.MeowCheckboxPreference
import io.github.lingqiqi5211.meowui.component.MeowPopupPreference
import io.github.lingqiqi5211.meowui.component.MeowLoadingDialog
import io.github.lingqiqi5211.meowui.component.MeowMenuItem
import io.github.lingqiqi5211.meowui.component.MeowNavigationBar
import io.github.lingqiqi5211.meowui.component.MeowNavigationBarStyle
import io.github.lingqiqi5211.meowui.component.MeowNavigationItem
import io.github.lingqiqi5211.meowui.component.MeowPreferenceScreen
import io.github.lingqiqi5211.meowui.component.MeowPreferenceSection
import io.github.lingqiqi5211.meowui.component.MeowPullToRefresh
import io.github.lingqiqi5211.meowui.component.MeowScaffold
import io.github.lingqiqi5211.meowui.component.MeowSingleChoiceDialog
import io.github.lingqiqi5211.meowui.component.MeowSliderPreference
import io.github.lingqiqi5211.meowui.component.MeowSnackbarResult
import io.github.lingqiqi5211.meowui.component.MeowSwitchPreference
import io.github.lingqiqi5211.meowui.component.MeowTabRow
import io.github.lingqiqi5211.meowui.component.MeowTabRowStyle
import io.github.lingqiqi5211.meowui.component.MeowTip
import io.github.lingqiqi5211.meowui.component.MeowTextInputDialog
import io.github.lingqiqi5211.meowui.component.MeowTextInputPreference
import io.github.lingqiqi5211.meowui.component.MeowTopBarAction
import io.github.lingqiqi5211.meowui.component.rememberMeowSnackbarState
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.core.preference.InMemoryPreferenceStore
import io.github.lingqiqi5211.meowui.core.preference.PreferenceKey
import io.github.lingqiqi5211.meowui.preference.MeowPreferenceProvider
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceValue
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceWriter
import io.github.lingqiqi5211.meowui.setMeowContent
import io.github.lingqiqi5211.meowui.theme.MeowPaletteStyle
import io.github.lingqiqi5211.meowui.theme.MeowAppearance
import io.github.lingqiqi5211.meowui.theme.MeowColorSpec
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import io.github.lingqiqi5211.meowui.theme.MeowThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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

@Serializable
private sealed interface SampleRoute : NavKey {
    @Serializable
    data object Main : SampleRoute

    @Serializable
    data object Appearance : SampleRoute
}

// 页面推入/弹出转场，参考 miuix-nav 的 MiuixDefault:整宽滑入、下层 1/4 视差并轻微压暗,
// 500ms 弹簧烘焙缓动(response 0.8 / damping 0.95),前段快、长缓尾。
private const val NavTransitionDuration = 500

// 欠阻尼弹簧的阶跃响应烘焙成 Easing,与 miuix-nav 的 NavSettleEasing 同式。
private fun navSettleEasing(response: Float, damping: Float): Easing {
    val omega = (2.0 * Math.PI / response).toFloat()
    val c = (damping * 4.0 * Math.PI / response).toFloat()
    val w = kotlin.math.sqrt(4f * omega * omega - c * c) / 2f
    val r = -c / 2f
    return Easing { fraction ->
        when {
            fraction <= 0f -> 0f
            fraction >= 1f -> 1f
            else -> kotlin.math.exp(r * fraction) *
                (-kotlin.math.cos(w * fraction) + (r / w) * kotlin.math.sin(w * fraction)) + 1f
        }
    }
}

private val NavPageEasing = navSettleEasing(response = 0.8f, damping = 0.95f)

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
        predictiveBackEnabled = predictiveBack,
        interfaceScale = interfaceScale,
    )

    MeowPreferenceProvider(store) {
        MeowTheme(appearance = appearance) {
            SampleSettings(
                appearance = appearance,
                blurEnabled = blurEnabled,
                floatingNavigation = floatingNavigation,
                floatingLabels = floatingLabels,
            )
        }
    }
}

@Composable
private fun SampleSettings(
    appearance: MeowAppearance,
    blurEnabled: Boolean,
    floatingNavigation: Boolean,
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
    val effect = rememberMeowBlurScaffoldEffect(enabled = blurEnabled)
    val snackbarState = rememberMeowSnackbarState()

    // NavDisplay 会缓存 entry 的内容 lambda，直接捕获的参数会停在旧快照上
    //（表现为外观页开关点了不刷新）；经 rememberUpdatedState 中转后，
    // 缓存的 lambda 每次重组都能读到最新值。
    val currentAppearance by rememberUpdatedState(appearance)
    val currentEffect by rememberUpdatedState(effect)
    val currentFloatingNavigation by rememberUpdatedState(floatingNavigation)
    val currentFloatingLabels by rememberUpdatedState(floatingLabels)
    val writeStyle = rememberMeowPreferenceWriter(SamplePreferences.Style)
    val writeThemeMode = rememberMeowPreferenceWriter(SamplePreferences.ThemeMode)
    val writeDynamicColor = rememberMeowPreferenceWriter(SamplePreferences.DynamicColor)
    val writeSeedColor = rememberMeowPreferenceWriter(SamplePreferences.SeedColor)
    val writePaletteStyle = rememberMeowPreferenceWriter(SamplePreferences.PaletteStyle)
    val writeColorSpec = rememberMeowPreferenceWriter(SamplePreferences.ColorSpec)
    val writeMiuixMonet = rememberMeowPreferenceWriter(SamplePreferences.MiuixMonet)
    val writePredictiveBack = rememberMeowPreferenceWriter(SamplePreferences.PredictiveBack)
    val writeInterfaceScale = rememberMeowPreferenceWriter(SamplePreferences.InterfaceScale)
    val serializersModule = remember {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclass(SampleRoute.Main::class)
                subclass(SampleRoute.Appearance::class)
            }
        }
    }
    val savedStateConfiguration = remember(serializersModule) {
        SavedStateConfiguration {
            this.serializersModule = serializersModule
        }
    }
    val backStack = rememberNavBackStack(
        configuration = savedStateConfiguration,
        SampleRoute.Main,
    )
    val closeAppearance = {
        if (backStack.size > 1) backStack.removeLastOrNull()
        Unit
    }
    val openAppearance = {
        if (backStack.lastOrNull() != SampleRoute.Appearance) {
            backStack.add(SampleRoute.Appearance)
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
            if (updated.predictiveBackEnabled != current.predictiveBackEnabled) {
                writePredictiveBack(updated.predictiveBackEnabled)
            }
            if (updated.interfaceScale != current.interfaceScale) {
                writeInterfaceScale(updated.interfaceScale)
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = closeAppearance,
        transitionSpec = {
            val fade = tween<Float>(NavTransitionDuration, easing = NavPageEasing)
            val slide = tween<IntOffset>(NavTransitionDuration, easing = NavPageEasing)
            (slideInHorizontally(slide) { it }) togetherWith
                (slideOutHorizontally(slide) { -it / 4 } + fadeOut(fade, targetAlpha = 0.9f))
        },
        popTransitionSpec = {
            val fade = tween<Float>(NavTransitionDuration, easing = NavPageEasing)
            val slide = tween<IntOffset>(NavTransitionDuration, easing = NavPageEasing)
            (slideInHorizontally(slide) { -it / 4 } + fadeIn(fade, initialAlpha = 0.9f)) togetherWith
                (slideOutHorizontally(slide) { it })
        },
        entryProvider = entryProvider<NavKey> {
            entry<SampleRoute.Main> {
                MeowScaffold(
        title = navigationItems[selectedPage].label,
        subtitle = "One page, two native styles",
        actionItems = listOf(
            MeowTopBarAction.Icon(
                icon = Icons.Rounded.Info,
                contentDescription = "About MeowUI",
                onClick = { showAlert = true },
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
                ),
            ),
        ),
        bottomBar = {
            MeowNavigationBar(
                items = navigationItems,
                selectedIndex = selectedPage,
                onItemSelected = { selectedPage = it },
                style = if (currentFloatingNavigation) {
                    MeowNavigationBarStyle.Floating
                } else {
                    MeowNavigationBarStyle.Standard
                },
                showFloatingLabels = currentFloatingLabels,
            )
        },
        snackbarState = snackbarState,
        effect = currentEffect,
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
    MeowBottomSheet(
        show = showBottomSheet,
        title = "Bottom Sheet",
        onDismissRequest = { showBottomSheet = false },
    ) {
        MeowPreferenceSection(
            title = "Shared content",
            modifier = Modifier.padding(16.dp),
        ) {
            MeowActionPreference(
                title = "Close sheet",
                summary = "Material and Miuix render this container separately.",
                onClick = { showBottomSheet = false },
            )
        }
                }
            }
            entry<SampleRoute.Appearance> {
                // 关闭预测式返回时在页面内部拦截系统预测手势（参考 InstallerX-Revived）：
                // 拖动期间没有任何预览，松手确认后才以普通 pop 转场出栈，避免闪跳。
                val backEventState = rememberNavigationEventState(NavigationEventInfo.None)
                NavigationBackHandler(
                    state = backEventState,
                    isBackEnabled = !currentAppearance.predictiveBackEnabled,
                    onBackCompleted = closeAppearance,
                )
                // 头图使用库内置的 MeowAppearancePreview（按手机/折叠/平板自适应），
                // 不需要时传 showPreview = false，或用 previewContent 自定义。
                MeowAppearancePage(
                    appearance = currentAppearance,
                    onAppearanceChange = updateAppearance,
                    onBackClick = closeAppearance,
                )
            }
        },
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
                    AppearanceSections(onOpenAppearance = onOpenAppearance)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
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
private fun SampleOptionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
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
            text = "0.1.0 · Android 8+ · Material 3 Expressive · Miuix",
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
    )
    MeowPreferenceProvider(store) {
        MeowTheme(appearance = appearance) {
            SampleSettings(
                appearance = appearance,
                blurEnabled = false,
                floatingNavigation = true,
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
    )
    MeowPreferenceProvider(store) {
        MeowTheme(appearance = appearance) {
            SampleSettings(
                appearance = appearance,
                blurEnabled = false,
                floatingNavigation = true,
                floatingLabels = true,
            )
        }
    }
}
