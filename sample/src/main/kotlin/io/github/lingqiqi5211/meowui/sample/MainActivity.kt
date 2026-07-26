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
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.blur.rememberMeowBlurScaffoldEffect
import io.github.lingqiqi5211.meowui.component.MeowActionPreference
import io.github.lingqiqi5211.meowui.component.MeowAlertDialog
import io.github.lingqiqi5211.meowui.component.MeowAlertStyle
import io.github.lingqiqi5211.meowui.component.MeowBottomSheet
import io.github.lingqiqi5211.meowui.component.MeowButton
import io.github.lingqiqi5211.meowui.component.MeowCheckboxPreference
import io.github.lingqiqi5211.meowui.component.MeowColorPickerDialog
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
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private object SamplePreferences {
    const val MaterialStyle = "material3_expressive"
    const val MiuixStyle = "miuix"

    val Style = PreferenceKey("ui_style", MaterialStyle)
    val DarkTheme = PreferenceKey("dark_theme", false)
    val DynamicColor = PreferenceKey("dynamic_color", true)
    val PaletteStyle = PreferenceKey("palette_style", MeowPaletteStyle.TonalSpot.name)
    val SeedColor = PreferenceKey("seed_color", 0xFF7B4DFF.toInt())
    val FloatingLabels = PreferenceKey("floating_labels", true)
    val Blur = PreferenceKey("background_blur", true)
    val FloatingNavigation = PreferenceKey("floating_navigation", true)
    val FeatureEnabled = PreferenceKey("feature_enabled", true)
    val ShowDetails = PreferenceKey("show_details", false)
    val Intensity = PreferenceKey("intensity", 0.65f)
    val Mode = PreferenceKey("mode", "Balanced")
    val Nickname = PreferenceKey("nickname", "Meow")
}

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
    val darkTheme by rememberMeowPreferenceValue(SamplePreferences.DarkTheme, store)
    val dynamicColor by rememberMeowPreferenceValue(SamplePreferences.DynamicColor, store)
    val paletteStyleName by rememberMeowPreferenceValue(SamplePreferences.PaletteStyle, store)
    val seedColorValue by rememberMeowPreferenceValue(SamplePreferences.SeedColor, store)
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

    MeowPreferenceProvider(store) {
        MeowTheme(
            style = style,
            darkTheme = darkTheme,
            dynamicColor = dynamicColor,
            seedColor = Color(seedColorValue),
            paletteStyle = paletteStyle,
        ) {
            SampleSettings(
                blurEnabled = blurEnabled,
                floatingNavigation = floatingNavigation,
                floatingLabels = floatingLabels,
            )
        }
    }
}

@Composable
private fun SampleSettings(
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
                style = if (floatingNavigation) {
                    MeowNavigationBarStyle.Floating
                } else {
                    MeowNavigationBarStyle.Standard
                },
                showFloatingLabels = floatingLabels,
            )
        },
        snackbarState = snackbarState,
        effect = effect,
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

@Composable
private fun SettingsPage(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
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
                    AppearanceSections()
                }
            } else {
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
                    item(key = "reset_controls") {
                        val resetIntensity = rememberMeowPreferenceWriter(SamplePreferences.Intensity)
                        val resetMode = rememberMeowPreferenceWriter(SamplePreferences.Mode)
                        MeowButton(
                            text = "Reset controls",
                            onClick = {
                                resetIntensity(SamplePreferences.Intensity.defaultValue)
                                resetMode(SamplePreferences.Mode.defaultValue)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceSections() {
    MeowPreferenceSection(title = "Appearance") {
        MeowPopupPreference(
            title = "UI style",
            key = SamplePreferences.Style,
            options = listOf(
                SamplePreferences.MaterialStyle,
                SamplePreferences.MiuixStyle,
            ),
            optionLabel = {
                if (it == SamplePreferences.MiuixStyle) {
                    "Miuix"
                } else {
                    "Material 3 Expressive"
                }
            },
        )
        MeowSwitchPreference(
            title = "Dark theme",
            key = SamplePreferences.DarkTheme,
        )
        item(key = "theme_color") {
            ThemeColorPreference()
        }
        MeowPopupPreference(
            title = "Palette style",
            summary = "How the seed color expands into the Material scheme",
            key = SamplePreferences.PaletteStyle,
            options = MeowPaletteStyle.entries.map { it.name },
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

@Composable
private fun ThemeColorPreference() {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val dynamicColor = rememberMeowPreferenceValue(SamplePreferences.DynamicColor).value
    val seedColorValue = rememberMeowPreferenceValue(SamplePreferences.SeedColor).value
    val writeDynamic = rememberMeowPreferenceWriter(SamplePreferences.DynamicColor)
    val writeSeed = rememberMeowPreferenceWriter(SamplePreferences.SeedColor)

    MeowActionPreference(
        title = "Theme color",
        summary = "Follow wallpaper or pick a custom seed color",
        value = if (dynamicColor) "Wallpaper" else "Custom",
        onClick = { showPicker = true },
    )
    MeowColorPickerDialog(
        show = showPicker,
        dynamicColor = dynamicColor,
        seedColor = Color(seedColorValue),
        onDynamicColorChange = { writeDynamic(it) },
        onSeedColorChange = { writeSeed(it.toArgb()) },
        onDismissRequest = { showPicker = false },
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
    MeowPreferenceProvider(store) {
        MeowTheme(
            style = MeowUiStyle.MaterialExpressive,
            darkTheme = false,
            dynamicColor = false,
        ) {
            SampleSettings(
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
    MeowPreferenceProvider(store) {
        MeowTheme(
            style = MeowUiStyle.Miuix,
            darkTheme = false,
            dynamicColor = false,
        ) {
            SampleSettings(
                blurEnabled = false,
                floatingNavigation = true,
                floatingLabels = true,
            )
        }
    }
}
