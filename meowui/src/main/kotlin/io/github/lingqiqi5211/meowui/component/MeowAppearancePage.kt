package io.github.lingqiqi5211.meowui.component

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowAppearance
import io.github.lingqiqi5211.meowui.theme.MeowAppearanceDefaults
import io.github.lingqiqi5211.meowui.theme.MeowBlur
import io.github.lingqiqi5211.meowui.theme.MeowColorSpec
import io.github.lingqiqi5211.meowui.theme.MeowPaletteStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import io.github.lingqiqi5211.meowui.theme.MeowThemeMode
import io.github.lingqiqi5211.meowui.theme.supportsSpec2025
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.Card as MiuixCard

/** 可替换的外观设置页文字。 */
@Immutable
data class MeowAppearanceLabels(
    val title: String = "Appearance",
    val themeColor: String = "Theme color",
    val themeMode: String = "Theme mode",
    val systemMode: String = "System",
    val lightMode: String = "Light",
    val darkMode: String = "Dark",
    val amoledDark: String = "AMOLED dark",
    val amoledDarkSummary: String = "Pure black backgrounds when dark theme is active",
    val colorSettings: String = "Colors",
    val paletteStyle: String = "Color style",
    val colorSpec: String = "Color standard",
    val miuixMonet: String = "Monet colors",
    val miuixMonetSummary: String = "Generate colors from wallpaper or a custom seed color",
    val interfaceSettings: String = "Interface",
    val interfaceStyle: String = "Interface style",
    val floatingNavigationBar: String = "Floating bottom bar",
    val floatingNavigationBarSummary: String = "Show the bottom bar as a floating pill",
    val blur: String = "Background blur",
    val blurSummary: String = "Frosted top bar and floating bottom bar",
    val predictiveBack: String = "Predictive back gesture",
    val predictiveBackSummary: String = "Preview the destination while swiping back",
    val interfaceScale: String = "Interface scale",
    val interfaceScaleSummary: String = "Adjust the size of the entire interface",
    val defaultValue: String = "Default",
    val customColor: String = "Custom color",
    val dialogConfirm: String = "Done",
    val dialogCancel: String = "Cancel",
)

/**
 * 外观页显示哪些功能块。默认全开，应用按需关掉。
 *
 * 一个分组里的功能块全关、也没有调用侧的条目时，整组连标题一起不显示。功能块自身的适用
 * 条件仍然生效：Monet 开关只在 Miuix 下、AMOLED 只在 Material 下、预测式返回只在 Android 14
 * 起、模糊只在设备能渲染时（见 [MeowBlur.isSupported]）出现。
 */
@Immutable
data class MeowAppearanceOptions(
    /** 顶部的主题预览头图。 */
    val preview: Boolean = true,
    /** 主题色：色票取色卡、Miuix 的 Monet 开关、色彩风格与色彩标准。 */
    val themeColor: Boolean = true,
    /** 深浅模式切换。 */
    val themeMode: Boolean = true,
    /** AMOLED 纯黑深色。 */
    val amoledDark: Boolean = true,
    /** Material / Miuix 界面风格切换。 */
    val interfaceStyle: Boolean = true,
    val floatingNavigationBar: Boolean = true,
    /**
     * 这个应用宽屏时是否把底栏换成侧边导航栏。
     *
     * 不对应设置里的任何一行，它说的是应用自己有没有接这个特性；头图据此决定画不画侧栏。
     * 外壳没接就置 false，否则头图会画出一个应用里根本不存在的样子。
     */
    val sideNavigationRail: Boolean = true,
    val blur: Boolean = true,
    val predictiveBack: Boolean = true,
    val interfaceScale: Boolean = true,
) {
    companion object {
        /** 全部功能块。 */
        val All: MeowAppearanceOptions = MeowAppearanceOptions()

        /** 一个内置功能块都不显示，只剩调用侧自己的内容。 */
        val None: MeowAppearanceOptions = MeowAppearanceOptions(
            preview = false,
            themeColor = false,
            themeMode = false,
            amoledDark = false,
            interfaceStyle = false,
            floatingNavigationBar = false,
            sideNavigationRail = false,
            blur = false,
            predictiveBack = false,
            interfaceScale = false,
        )
    }
}

/**
 * 统一的外观设置页。
 *
 * 页面本身不保存正式设置；每次变更都通过 [onAppearanceChange] 回传。
 *
 * - [options] 决定显示哪些功能块；[showPreview] 为 false 等同于 `options.preview = false`。
 * - 头图默认是 [MeowAppearancePreview]，[previewContent] 不为 null 时替换。
 * - [colorItems]、[interfaceItems] 追加到「颜色」「界面」分组末尾；给了它们，对应分组就算
 *   内置功能块全关也会连标题一起显示。[extraContent] 追加在全部内置分组之后，可放整组。
 */
@Composable
fun MeowAppearancePage(
    appearance: MeowAppearance,
    onAppearanceChange: (MeowAppearance) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    navigationModifier: Modifier = Modifier,
    labels: MeowAppearanceLabels = MeowAppearanceLabels(),
    showPreview: Boolean = true,
    previewContent: (@Composable (MeowAppearance) -> Unit)? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {},
    options: MeowAppearanceOptions = MeowAppearanceOptions.All,
    colorItems: (@Composable MeowPreferenceSectionScope.() -> Unit)? = null,
    interfaceItems: (@Composable MeowPreferenceSectionScope.() -> Unit)? = null,
) {
    MeowPreferencePage(
        title = labels.title,
        modifier = modifier,
        onBackClick = onBackClick,
        navigationModifier = navigationModifier,
    ) {
        MeowAppearanceContent(
            appearance = appearance,
            onAppearanceChange = onAppearanceChange,
            labels = labels,
            showPreview = showPreview,
            previewContent = previewContent,
            extraContent = extraContent,
            options = options,
            colorItems = colorItems,
            interfaceItems = interfaceItems,
        )
    }
}

/**
 * 外观设置的内容本体，不含页面外壳。
 *
 * 供已经自己持有顶栏的宿主使用：把外观设置放进宿主已有的页面里，而不是再套一层带顶栏
 * 的 [MeowPreferencePage]。参数含义与 [MeowAppearancePage] 相同。
 *
 * 需要放在能提供滚动容器的地方，例如 [MeowPreferenceScreen] 的内容槽位。
 */
@Composable
fun ColumnScope.MeowAppearanceContent(
    appearance: MeowAppearance,
    onAppearanceChange: (MeowAppearance) -> Unit,
    labels: MeowAppearanceLabels = MeowAppearanceLabels(),
    showPreview: Boolean = true,
    previewContent: (@Composable (MeowAppearance) -> Unit)? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {},
    options: MeowAppearanceOptions = MeowAppearanceOptions.All,
    colorItems: (@Composable MeowPreferenceSectionScope.() -> Unit)? = null,
    interfaceItems: (@Composable MeowPreferenceSectionScope.() -> Unit)? = null,
) {
    // Miuix 关闭 Monet 后使用原生配色，种子色与调色板设置不再生效，相关选项一并隐藏。
    val colorCustomizable = appearance.style != MeowUiStyle.Miuix || appearance.miuixMonetEnabled
    val showMiuixMonet = options.themeColor && appearance.style == MeowUiStyle.Miuix
    val showPalette = options.themeColor && colorCustomizable
    // AMOLED 纯黑深色仅 Material 分支生效，作为深色模式的叠加开关。
    val showAmoledDark = options.amoledDark && appearance.style == MeowUiStyle.MaterialExpressive
    val showBlur = options.blur && MeowBlur.isSupported
    val showPredictiveBack = options.predictiveBack &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    val showColorSection = showMiuixMonet || showAmoledDark || showPalette || colorItems != null
    val showInterfaceSection = options.interfaceStyle || options.floatingNavigationBar ||
        showBlur || showPredictiveBack || options.interfaceScale || interfaceItems != null

    if (showPreview && options.preview) {
        previewContent?.invoke(appearance) ?: MeowAppearancePreview(
            appearance = appearance,
            sideNavigationRail = options.sideNavigationRail,
        )
    }

    if (options.themeColor) {
        AnimatedVisibility(
            visible = colorCustomizable,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            AppearanceColorCard(title = labels.themeColor) {
                MeowColorPicker(
                    dynamicColor = appearance.dynamicColor,
                    seedColor = appearance.seedColor,
                    paletteStyle = appearance.paletteStyle,
                    colorSpec = appearance.colorSpec,
                    customColorTitle = labels.customColor,
                    confirmText = labels.dialogConfirm,
                    cancelText = labels.dialogCancel,
                    onDynamicColorChange = { enabled ->
                        onAppearanceChange(appearance.copy(dynamicColor = enabled))
                    },
                    onSeedColorChange = { color ->
                        onAppearanceChange(
                            appearance.copy(
                                dynamicColor = false,
                                seedColor = color,
                            ),
                        )
                    },
                )
            }
        }
    }

    if (options.themeMode) {
        Column {
            AppearanceSectionTitle(labels.themeMode)
            val modes = MeowThemeMode.entries
            MeowTabRow(
                tabs = listOf(labels.systemMode, labels.lightMode, labels.darkMode),
                selectedIndex = modes.indexOf(appearance.themeMode),
                onTabSelected = { index ->
                    onAppearanceChange(appearance.copy(themeMode = modes[index]))
                },
                modifier = Modifier.fillMaxWidth(),
                style = MeowTabRowStyle.Contour,
            )
        }
    }

    if (showColorSection) MeowPreferenceSection(title = labels.colorSettings) {
        item(key = "miuixMonet", visible = showMiuixMonet, container = false) {
            io.github.lingqiqi5211.meowui.component.MeowSwitchPreference(
                title = labels.miuixMonet,
                summary = labels.miuixMonetSummary,
                checked = appearance.miuixMonetEnabled,
                onCheckedChange = { enabled ->
                    onAppearanceChange(appearance.copy(miuixMonetEnabled = enabled))
                },
            )
        }
        item(key = "amoledDark", visible = showAmoledDark, container = false) {
            io.github.lingqiqi5211.meowui.component.MeowSwitchPreference(
                title = labels.amoledDark,
                summary = labels.amoledDarkSummary,
                checked = appearance.amoledDarkEnabled,
                onCheckedChange = { enabled ->
                    onAppearanceChange(appearance.copy(amoledDarkEnabled = enabled))
                },
            )
        }
        item(key = "paletteStyle", visible = showPalette, container = false) {
            io.github.lingqiqi5211.meowui.component.MeowPopupPreference(
                title = labels.paletteStyle,
                value = appearance.paletteStyle,
                options = MeowPaletteStyle.entries,
                optionLabel = MeowPaletteStyle::displayName,
                onValueChange = { style ->
                    onAppearanceChange(
                        appearance.copy(
                            paletteStyle = style,
                            colorSpec = if (style.supportsSpec2025) {
                                appearance.colorSpec
                            } else {
                                MeowColorSpec.Spec2021
                            },
                        ),
                    )
                },
            )
        }
        item(key = "colorSpec", visible = showPalette, container = false) {
            val availableColorSpecs = if (appearance.paletteStyle.supportsSpec2025) {
                MeowColorSpec.entries
            } else {
                listOf(MeowColorSpec.Spec2021)
            }
            io.github.lingqiqi5211.meowui.component.MeowPopupPreference(
                title = labels.colorSpec,
                value = appearance.colorSpec.takeIf(availableColorSpecs::contains)
                    ?: MeowColorSpec.Spec2021,
                options = availableColorSpecs,
                optionLabel = { spec ->
                    when (spec) {
                        MeowColorSpec.Spec2021 -> "2021"
                        MeowColorSpec.Spec2025 -> "2025"
                    }
                },
                onValueChange = { spec ->
                    onAppearanceChange(appearance.copy(colorSpec = spec))
                },
            )
        }
        colorItems?.invoke(this)
    }

    if (showInterfaceSection) MeowPreferenceSection(title = labels.interfaceSettings) {
        item(key = "interfaceStyle", visible = options.interfaceStyle, container = false) {
            io.github.lingqiqi5211.meowui.component.MeowPopupPreference(
                title = labels.interfaceStyle,
                value = appearance.style,
                options = MeowUiStyle.entries,
                optionLabel = { style ->
                    when (style) {
                        MeowUiStyle.MaterialExpressive -> "Material 3 Expressive"
                        MeowUiStyle.Miuix -> "Miuix"
                    }
                },
                onValueChange = { style ->
                    onAppearanceChange(appearance.copy(style = style))
                },
            )
        }
        item(key = "floatingNavigationBar", visible = options.floatingNavigationBar, container = false) {
            io.github.lingqiqi5211.meowui.component.MeowSwitchPreference(
                title = labels.floatingNavigationBar,
                summary = labels.floatingNavigationBarSummary,
                checked = appearance.floatingNavigationBarEnabled,
                onCheckedChange = { enabled ->
                    onAppearanceChange(appearance.copy(floatingNavigationBarEnabled = enabled))
                },
            )
        }
        item(key = "blur", visible = showBlur, container = false) {
            io.github.lingqiqi5211.meowui.component.MeowSwitchPreference(
                title = labels.blur,
                summary = labels.blurSummary,
                checked = appearance.blurEnabled,
                onCheckedChange = { enabled ->
                    onAppearanceChange(appearance.copy(blurEnabled = enabled))
                },
            )
        }
        item(key = "predictiveBack", visible = showPredictiveBack, container = false) {
            io.github.lingqiqi5211.meowui.component.MeowSwitchPreference(
                title = labels.predictiveBack,
                summary = labels.predictiveBackSummary,
                checked = appearance.predictiveBackEnabled,
                onCheckedChange = { enabled ->
                    onAppearanceChange(appearance.copy(predictiveBackEnabled = enabled))
                },
            )
        }
        item(key = "interfaceScale", visible = options.interfaceScale, container = false) {
            InterfaceScaleItem(
                appearance = appearance,
                onAppearanceChange = onAppearanceChange,
                labels = labels,
            )
        }
        interfaceItems?.invoke(this)
    }

    extraContent()
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun InterfaceScaleItem(
    appearance: MeowAppearance,
    onAppearanceChange: (MeowAppearance) -> Unit,
    labels: MeowAppearanceLabels,
) {
    val normalizedScale = appearance.interfaceScale
        .takeIf(Float::isFinite)
        ?.coerceIn(
            MeowAppearanceDefaults.MinInterfaceScale,
            MeowAppearanceDefaults.MaxInterfaceScale,
        ) ?: 1f
    var draftScale by remember(normalizedScale) {
        mutableFloatStateOf(normalizedScale)
    }
    MeowSliderPreference(
        title = labels.interfaceScale,
        summary = labels.interfaceScaleSummary,
        value = draftScale,
        // 拖动期间不量化，滑块才连续跟手；松手时量化到 1% 再提交。
        onValueChange = { draftScale = it },
        onValueChangeFinished = {
            val committed = (draftScale * 100).roundToInt() / 100f
            onAppearanceChange(appearance.copy(interfaceScale = committed))
        },
        valueRange = MeowAppearanceDefaults.MinInterfaceScale..MeowAppearanceDefaults.MaxInterfaceScale,
        valueText = { "${(it * 100).roundToInt()}%" },
        defaultValue = 1f,
        defaultText = labels.defaultValue,
    )
}

@Composable
private fun AppearanceColorCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppearanceSectionTitle(title)
        Spacer(Modifier.height(8.dp))
        MeowStyleContent(
            materialExpressive = {
                MaterialSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceBright,
                ) {
                    AppearanceColorCardContent(content)
                }
            },
            miuix = {
                MiuixCard(
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(0.dp),
                ) {
                    AppearanceColorCardContent(content)
                }
            },
        )
    }
}

@Composable
private fun AppearanceColorCardContent(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        content()
    }
}

@Composable
private fun AppearanceSectionTitle(text: String) {
    BasicText(
        text = text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        style = MeowTheme.typography.sectionTitle.copy(color = MeowTheme.colors.primary),
    )
}

private fun MeowPaletteStyle.displayName(): String = when (this) {
    MeowPaletteStyle.TonalSpot -> "Tonal spot"
    MeowPaletteStyle.Neutral -> "Neutral"
    MeowPaletteStyle.Vibrant -> "Vibrant"
    MeowPaletteStyle.Expressive -> "Expressive"
    MeowPaletteStyle.Rainbow -> "Rainbow"
    MeowPaletteStyle.FruitSalad -> "Fruit salad"
    MeowPaletteStyle.Monochrome -> "Monochrome"
    MeowPaletteStyle.Fidelity -> "Fidelity"
    MeowPaletteStyle.Content -> "Content"
}
