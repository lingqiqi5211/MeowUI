// Scheme construction follows Miuix ThemeController (compose-miuix-ui contributors).
// SPDX-License-Identifier: Apache-2.0

package io.github.lingqiqi5211.meowui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.dynamiccolor.DynamicScheme
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeCmf
import com.materialkolor.scheme.SchemeContent
import com.materialkolor.scheme.SchemeExpressive
import com.materialkolor.scheme.SchemeFidelity
import com.materialkolor.scheme.SchemeFruitSalad
import com.materialkolor.scheme.SchemeMonochrome
import com.materialkolor.scheme.SchemeNeutral
import com.materialkolor.scheme.SchemeRainbow
import com.materialkolor.scheme.SchemeTonalSpot
import com.materialkolor.scheme.SchemeVibrant
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

internal const val supportsColorSpec2026 = true

internal fun MeowColorSettings.miuixPalette(): ThemePaletteStyle = ThemePaletteStyle.valueOf(palette.name)

internal fun MeowColorSettings.miuixSpec(): ThemeColorSpec = ThemeColorSpec.valueOf(spec.name)

internal fun generateMaterialColorScheme(
    seedColor: Color,
    isDark: Boolean,
    paletteStyle: MeowPaletteStyle,
    colorSpec: MeowColorSpec?,
    isAmoled: Boolean,
): ColorScheme {
    val internalSpec =
        when (colorSpec) {
            MeowColorSpec.Spec2021 -> ColorSpec.SpecVersion.SPEC_2021
            MeowColorSpec.Spec2026 -> ColorSpec.SpecVersion.SPEC_2026
            else -> ColorSpec.SpecVersion.SPEC_2025
        }
    val hctColor = Hct.fromInt(seedColor.toArgb())
    val scheme: DynamicScheme =
        when (paletteStyle) {
            MeowPaletteStyle.TonalSpot -> {
                SchemeTonalSpot(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }

            MeowPaletteStyle.Neutral -> {
                SchemeNeutral(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }

            MeowPaletteStyle.Vibrant -> {
                SchemeVibrant(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }

            MeowPaletteStyle.Expressive -> {
                SchemeExpressive(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }

            MeowPaletteStyle.Rainbow -> {
                SchemeRainbow(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }

            MeowPaletteStyle.FruitSalad -> {
                SchemeFruitSalad(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }

            MeowPaletteStyle.Monochrome -> {
                SchemeMonochrome(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }

            MeowPaletteStyle.Fidelity -> {
                SchemeFidelity(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }

            MeowPaletteStyle.Content -> {
                SchemeContent(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }

            MeowPaletteStyle.Cmf -> {
                SchemeCmf(
                    sourceColorHct = hctColor,
                    isDark = isDark,
                    contrastLevel = 0.0,
                    specVersion = internalSpec,
                    platform = DynamicScheme.Platform.PHONE,
                )
            }
        }
    return ColorScheme(
        primary = Color(scheme.primary),
        onPrimary = Color(scheme.onPrimary),
        primaryContainer = Color(scheme.primaryContainer),
        onPrimaryContainer = Color(scheme.onPrimaryContainer),
        inversePrimary = Color(scheme.inversePrimary),
        secondary = Color(scheme.secondary),
        onSecondary = Color(scheme.onSecondary),
        secondaryContainer = Color(scheme.secondaryContainer),
        onSecondaryContainer = Color(scheme.onSecondaryContainer),
        tertiary = Color(scheme.tertiary),
        onTertiary = Color(scheme.onTertiary),
        tertiaryContainer = Color(scheme.tertiaryContainer),
        onTertiaryContainer = Color(scheme.onTertiaryContainer),
        background = Color(scheme.background),
        onBackground = Color(scheme.onBackground),
        surface = Color(scheme.surface),
        onSurface = Color(scheme.onSurface),
        surfaceVariant = Color(scheme.surfaceVariant),
        onSurfaceVariant = Color(scheme.onSurfaceVariant),
        surfaceTint = Color(scheme.surfaceTint),
        inverseSurface = Color(scheme.inverseSurface),
        inverseOnSurface = Color(scheme.inverseOnSurface),
        error = Color(scheme.error),
        onError = Color(scheme.onError),
        errorContainer = Color(scheme.errorContainer),
        onErrorContainer = Color(scheme.onErrorContainer),
        outline = Color(scheme.outline),
        outlineVariant = Color(scheme.outlineVariant),
        scrim = Color(scheme.scrim),
        surfaceBright = Color(scheme.surfaceBright),
        surfaceDim = Color(scheme.surfaceDim),
        surfaceContainer = Color(scheme.surfaceContainer),
        surfaceContainerHigh = Color(scheme.surfaceContainerHigh),
        surfaceContainerHighest = Color(scheme.surfaceContainerHighest),
        surfaceContainerLow = Color(scheme.surfaceContainerLow),
        surfaceContainerLowest = Color(scheme.surfaceContainerLowest),
        primaryFixed = Color(scheme.primaryFixed),
        primaryFixedDim = Color(scheme.primaryFixedDim),
        onPrimaryFixed = Color(scheme.onPrimaryFixed),
        onPrimaryFixedVariant = Color(scheme.onPrimaryFixedVariant),
        secondaryFixed = Color(scheme.secondaryFixed),
        secondaryFixedDim = Color(scheme.secondaryFixedDim),
        onSecondaryFixed = Color(scheme.onSecondaryFixed),
        onSecondaryFixedVariant = Color(scheme.onSecondaryFixedVariant),
        tertiaryFixed = Color(scheme.tertiaryFixed),
        tertiaryFixedDim = Color(scheme.tertiaryFixedDim),
        onTertiaryFixed = Color(scheme.onTertiaryFixed),
        onTertiaryFixedVariant = Color(scheme.onTertiaryFixedVariant),
    )
}
