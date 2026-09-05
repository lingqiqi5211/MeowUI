package io.github.lingqiqi5211.meowui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle as MaterialKolorPaletteStyle
import com.materialkolor.dynamicColorScheme as materialKolorDynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec

/**
 * Material 3 Expressive 分支的配色生成。
 *
 * 用 materialKolor 从种子色展开完整的 MD3 tonal palette（含 container、surface 系列与
 * fixed 色 role）。旧主题入口会为支持的风格自动启用 2025 spec，统一外观入口可明确指定。
 */
@Stable
internal fun meowMaterialColorScheme(
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
    }
    val specVersion = when (colorSpec) {
        MeowColorSpec.Spec2021 -> ColorSpec.SpecVersion.SPEC_2021
        MeowColorSpec.Spec2025 -> if (paletteStyle.supportsSpec2025) {
            ColorSpec.SpecVersion.SPEC_2025
        } else {
            ColorSpec.SpecVersion.SPEC_2021
        }
        null -> if (paletteStyle.supportsSpec2025) {
            ColorSpec.SpecVersion.SPEC_2025
        } else {
            ColorSpec.SpecVersion.SPEC_2021
        }
    }

    return materialKolorDynamicColorScheme(
        seedColor = seedColor,
        isDark = isDark,
        isAmoled = isAmoled,
        style = style,
        specVersion = specVersion,
    ).amoledBackground(isAmoled && isDark)
}

/**
 * AMOLED 纯黑深色（参考 KernelSU）。
 *
 * 只把"页面底"这一层的 role 压成纯黑：背景、surface,以及 surfaceContainer 及以下
 * ——MeowScaffold 用 surfaceContainer 作页面色,所以这一层必须黑。
 *
 * surfaceContainerHigh / Highest 与 surfaceBright 保持生成值不动：搜索框、卡片和分组
 * 列表项都取自这几个 role,一起压黑会让它们与纯黑页面完全融为一体,控件等于消失。
 */
private fun ColorScheme.amoledBackground(amoled: Boolean): ColorScheme =
    if (!amoled) {
        this
    } else {
        copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainer = Color.Black,
        )
    }

// 2025 color spec 仅覆盖这四种风格，其余风格回退 2021 spec。
internal val MeowPaletteStyle.supportsSpec2025: Boolean
    get() = when (this) {
        MeowPaletteStyle.TonalSpot,
        MeowPaletteStyle.Neutral,
        MeowPaletteStyle.Vibrant,
        MeowPaletteStyle.Expressive,
        -> true

        else -> false
    }

/**
 * 对 [ColorScheme] 内全部色 role 做 spring 过渡，
 * 让浅深色、种子色或调色板风格切换时颜色平滑变化而不是硬切。
 *
 * 过渡由 [rememberColorTransition] 的一条进度驱动。首帧直接取目标值。
 */
@Composable
internal fun ColorScheme.animateAsState(): ColorScheme {
    val animated = rememberColorTransition(remember(this) { toColorList() })
    return remember(animated) { animated.toColorScheme() }
}

private fun ColorScheme.toColorList(): List<Color> = listOf(
    primary,
    onPrimary,
    primaryContainer,
    onPrimaryContainer,
    inversePrimary,
    secondary,
    onSecondary,
    secondaryContainer,
    onSecondaryContainer,
    tertiary,
    onTertiary,
    tertiaryContainer,
    onTertiaryContainer,
    background,
    onBackground,
    surface,
    onSurface,
    surfaceVariant,
    onSurfaceVariant,
    surfaceTint,
    inverseSurface,
    inverseOnSurface,
    error,
    onError,
    errorContainer,
    onErrorContainer,
    outline,
    outlineVariant,
    scrim,
    surfaceBright,
    surfaceDim,
    surfaceContainer,
    surfaceContainerHigh,
    surfaceContainerHighest,
    surfaceContainerLow,
    surfaceContainerLowest,
    primaryFixed,
    primaryFixedDim,
    onPrimaryFixed,
    onPrimaryFixedVariant,
    secondaryFixed,
    secondaryFixedDim,
    onSecondaryFixed,
    onSecondaryFixedVariant,
    tertiaryFixed,
    tertiaryFixedDim,
    onTertiaryFixed,
    onTertiaryFixedVariant,
)

// 与 toColorList 的顺序一一对应。
private fun List<Color>.toColorScheme(): ColorScheme {
    var index = 0
    fun next(): Color = this[index++]
    return ColorScheme(
        primary = next(),
        onPrimary = next(),
        primaryContainer = next(),
        onPrimaryContainer = next(),
        inversePrimary = next(),
        secondary = next(),
        onSecondary = next(),
        secondaryContainer = next(),
        onSecondaryContainer = next(),
        tertiary = next(),
        onTertiary = next(),
        tertiaryContainer = next(),
        onTertiaryContainer = next(),
        background = next(),
        onBackground = next(),
        surface = next(),
        onSurface = next(),
        surfaceVariant = next(),
        onSurfaceVariant = next(),
        surfaceTint = next(),
        inverseSurface = next(),
        inverseOnSurface = next(),
        error = next(),
        onError = next(),
        errorContainer = next(),
        onErrorContainer = next(),
        outline = next(),
        outlineVariant = next(),
        scrim = next(),
        surfaceBright = next(),
        surfaceDim = next(),
        surfaceContainer = next(),
        surfaceContainerHigh = next(),
        surfaceContainerHighest = next(),
        surfaceContainerLow = next(),
        surfaceContainerLowest = next(),
        primaryFixed = next(),
        primaryFixedDim = next(),
        onPrimaryFixed = next(),
        onPrimaryFixedVariant = next(),
        secondaryFixed = next(),
        secondaryFixedDim = next(),
        onSecondaryFixed = next(),
        onSecondaryFixedVariant = next(),
        tertiaryFixed = next(),
        tertiaryFixedDim = next(),
        onTertiaryFixed = next(),
        onTertiaryFixedVariant = next(),
    )
}
