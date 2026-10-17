package io.github.lingqiqi5211.meowui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle as MaterialKolorPaletteStyle
import com.materialkolor.dynamicColorScheme as materialKolorDynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

internal const val supportsColorSpec2026 = false

internal fun MeowColorSettings.miuixPalette(): ThemePaletteStyle = ThemePaletteStyle.valueOf(palette.name)
internal fun MeowColorSettings.miuixSpec(): ThemeColorSpec = ThemeColorSpec.valueOf(spec.name)

@Stable
internal fun generateMaterialColorScheme(
    seedColor: Color,
    isDark: Boolean,
    paletteStyle: MeowPaletteStyle,
    colorSpec: MeowColorSpec? = null,
    isAmoled: Boolean = false,
): ColorScheme {
    val style = when (paletteStyle) {
        MeowPaletteStyle.TonalSpot -> MaterialKolorPaletteStyle.TonalSpot
        MeowPaletteStyle.Neutral -> MaterialKolorPaletteStyle.Neutral
        MeowPaletteStyle.Vibrant -> MaterialKolorPaletteStyle.Vibrant
        MeowPaletteStyle.Expressive -> MaterialKolorPaletteStyle.Expressive
        MeowPaletteStyle.Rainbow -> MaterialKolorPaletteStyle.Rainbow
        MeowPaletteStyle.FruitSalad -> MaterialKolorPaletteStyle.FruitSalad
        MeowPaletteStyle.Monochrome -> MaterialKolorPaletteStyle.Monochrome
        MeowPaletteStyle.Fidelity -> MaterialKolorPaletteStyle.Fidelity
        MeowPaletteStyle.Content -> MaterialKolorPaletteStyle.Content
        MeowPaletteStyle.Cmf -> error("CMF must be normalized before generation")
    }
    val specVersion = when (colorSpec) {
        MeowColorSpec.Spec2021 -> ColorSpec.SpecVersion.SPEC_2021
        else -> ColorSpec.SpecVersion.SPEC_2025
    }

    return materialKolorDynamicColorScheme(
        seedColor = seedColor,
        isDark = isDark,
        isAmoled = isAmoled,
        style = style,
        specVersion = specVersion,
    )
}
