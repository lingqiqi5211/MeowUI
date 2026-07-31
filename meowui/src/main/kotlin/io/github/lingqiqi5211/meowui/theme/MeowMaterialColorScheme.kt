package io.github.lingqiqi5211.meowui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.PaletteStyle as MaterialKolorPaletteStyle
import com.materialkolor.dynamicColorScheme as materialKolorDynamicColorScheme

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
 * 首帧直接取目标值，不会从错误颜色开始播放动画。
 */
@Composable
internal fun ColorScheme.animateAsState(): ColorScheme {
    @Composable
    fun animateColor(color: Color): Color = animateColorAsState(
        targetValue = color,
        animationSpec = spring(),
        label = "meow_theme_color",
    ).value

    return ColorScheme(
        primary = animateColor(primary),
        onPrimary = animateColor(onPrimary),
        primaryContainer = animateColor(primaryContainer),
        onPrimaryContainer = animateColor(onPrimaryContainer),
        inversePrimary = animateColor(inversePrimary),
        secondary = animateColor(secondary),
        onSecondary = animateColor(onSecondary),
        secondaryContainer = animateColor(secondaryContainer),
        onSecondaryContainer = animateColor(onSecondaryContainer),
        tertiary = animateColor(tertiary),
        onTertiary = animateColor(onTertiary),
        tertiaryContainer = animateColor(tertiaryContainer),
        onTertiaryContainer = animateColor(onTertiaryContainer),
        background = animateColor(background),
        onBackground = animateColor(onBackground),
        surface = animateColor(surface),
        onSurface = animateColor(onSurface),
        surfaceVariant = animateColor(surfaceVariant),
        onSurfaceVariant = animateColor(onSurfaceVariant),
        surfaceTint = animateColor(surfaceTint),
        inverseSurface = animateColor(inverseSurface),
        inverseOnSurface = animateColor(inverseOnSurface),
        error = animateColor(error),
        onError = animateColor(onError),
        errorContainer = animateColor(errorContainer),
        onErrorContainer = animateColor(onErrorContainer),
        outline = animateColor(outline),
        outlineVariant = animateColor(outlineVariant),
        scrim = animateColor(scrim),
        surfaceBright = animateColor(surfaceBright),
        surfaceDim = animateColor(surfaceDim),
        surfaceContainer = animateColor(surfaceContainer),
        surfaceContainerHigh = animateColor(surfaceContainerHigh),
        surfaceContainerHighest = animateColor(surfaceContainerHighest),
        surfaceContainerLow = animateColor(surfaceContainerLow),
        surfaceContainerLowest = animateColor(surfaceContainerLowest),
        primaryFixed = animateColor(primaryFixed),
        primaryFixedDim = animateColor(primaryFixedDim),
        onPrimaryFixed = animateColor(onPrimaryFixed),
        onPrimaryFixedVariant = animateColor(onPrimaryFixedVariant),
        secondaryFixed = animateColor(secondaryFixed),
        secondaryFixedDim = animateColor(secondaryFixedDim),
        onSecondaryFixed = animateColor(onSecondaryFixed),
        onSecondaryFixedVariant = animateColor(onSecondaryFixedVariant),
        tertiaryFixed = animateColor(tertiaryFixed),
        tertiaryFixedDim = animateColor(tertiaryFixedDim),
        onTertiaryFixed = animateColor(onTertiaryFixed),
        onTertiaryFixedVariant = animateColor(onTertiaryFixedVariant),
    )
}
