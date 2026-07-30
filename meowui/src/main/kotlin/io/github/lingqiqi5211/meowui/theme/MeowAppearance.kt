package io.github.lingqiqi5211.meowui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.github.lingqiqi5211.meowui.core.MeowUiStyle

/** 深浅色跟随方式。 */
enum class MeowThemeMode {
    System,
    Light,
    Dark,
}

/** 生成主题色时采用的 Material color specification。 */
enum class MeowColorSpec {
    Spec2021,
    Spec2025,
}

/**
 * MeowUI 外观设置的统一受控状态。
 *
 * 可同时传给 `MeowTheme` 与 `MeowAppearancePage`，让主题应用和设置页面共用一份状态。
 * `miuixMonetEnabled` 关闭时使用 Miuix 原生默认配色；Material 分支忽略该值。
 * `predictiveBackEnabled` 由应用的导航层读取并决定是否注册预测式返回处理。
 */
@Immutable
data class MeowAppearance(
    val style: MeowUiStyle = MeowUiStyle.MaterialExpressive,
    val themeMode: MeowThemeMode = MeowThemeMode.System,
    val dynamicColor: Boolean = true,
    val seedColor: Color = Color(0xFF6750A4),
    val paletteStyle: MeowPaletteStyle = MeowPaletteStyle.TonalSpot,
    val colorSpec: MeowColorSpec = MeowColorSpec.Spec2025,
    val miuixMonetEnabled: Boolean = true,
    val predictiveBackEnabled: Boolean = true,
    val interfaceScale: Float = 1f,
)

object MeowAppearanceDefaults {
    const val MinInterfaceScale = 0.8f
    const val MaxInterfaceScale = 1.1f
}
