package io.github.lingqiqi5211.meowui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.Colors

/**
 * 对 Miuix [Colors] 的全部色 role 做 spring 过渡。
 *
 * Miuix 原生主题在 ThemeController 切换（如开关 Monet）时颜色硬切，这里让配色平滑变化。
 * 过渡由 [rememberColorTransition] 的一条进度驱动。首帧直接取目标值。
 *
 * [Colors] 的每个 role 都是快照状态，这里逐个读取即订阅了它们的变化。
 */
@Composable
internal fun Colors.animateAsState(): Colors {
    val animated = rememberColorTransition(toColorList())
    return remember(animated) { animated.toMiuixColors() }
}

private fun Colors.toColorList(): List<Color> = listOf(
    primary,
    onPrimary,
    primaryVariant,
    onPrimaryVariant,
    error,
    onError,
    errorContainer,
    onErrorContainer,
    disabledPrimary,
    disabledOnPrimary,
    disabledPrimaryButton,
    disabledOnPrimaryButton,
    disabledPrimarySlider,
    primaryContainer,
    onPrimaryContainer,
    secondary,
    onSecondary,
    secondaryVariant,
    onSecondaryVariant,
    disabledSecondary,
    disabledOnSecondary,
    disabledSecondaryVariant,
    disabledOnSecondaryVariant,
    secondaryContainer,
    onSecondaryContainer,
    secondaryContainerVariant,
    onSecondaryContainerVariant,
    tertiaryContainer,
    onTertiaryContainer,
    tertiaryContainerVariant,
    background,
    onBackground,
    onBackgroundVariant,
    surface,
    onSurface,
    surfaceVariant,
    onSurfaceSecondary,
    onSurfaceVariantSummary,
    onSurfaceVariantActions,
    disabledOnSurface,
    surfaceContainer,
    onSurfaceContainer,
    onSurfaceContainerVariant,
    surfaceContainerHigh,
    onSurfaceContainerHigh,
    surfaceContainerHighest,
    onSurfaceContainerHighest,
    outline,
    dividerLine,
    windowDimming,
    sliderKeyPoint,
    sliderKeyPointForeground,
    sliderBackground,
)

// 与 toColorList 的顺序一一对应。
private fun List<Color>.toMiuixColors(): Colors {
    var index = 0
    fun next(): Color = this[index++]
    return Colors(
        primary = next(),
        onPrimary = next(),
        primaryVariant = next(),
        onPrimaryVariant = next(),
        error = next(),
        onError = next(),
        errorContainer = next(),
        onErrorContainer = next(),
        disabledPrimary = next(),
        disabledOnPrimary = next(),
        disabledPrimaryButton = next(),
        disabledOnPrimaryButton = next(),
        disabledPrimarySlider = next(),
        primaryContainer = next(),
        onPrimaryContainer = next(),
        secondary = next(),
        onSecondary = next(),
        secondaryVariant = next(),
        onSecondaryVariant = next(),
        disabledSecondary = next(),
        disabledOnSecondary = next(),
        disabledSecondaryVariant = next(),
        disabledOnSecondaryVariant = next(),
        secondaryContainer = next(),
        onSecondaryContainer = next(),
        secondaryContainerVariant = next(),
        onSecondaryContainerVariant = next(),
        tertiaryContainer = next(),
        onTertiaryContainer = next(),
        tertiaryContainerVariant = next(),
        background = next(),
        onBackground = next(),
        onBackgroundVariant = next(),
        surface = next(),
        onSurface = next(),
        surfaceVariant = next(),
        onSurfaceSecondary = next(),
        onSurfaceVariantSummary = next(),
        onSurfaceVariantActions = next(),
        disabledOnSurface = next(),
        surfaceContainer = next(),
        onSurfaceContainer = next(),
        onSurfaceContainerVariant = next(),
        surfaceContainerHigh = next(),
        onSurfaceContainerHigh = next(),
        surfaceContainerHighest = next(),
        onSurfaceContainerHighest = next(),
        outline = next(),
        dividerLine = next(),
        windowDimming = next(),
        sliderKeyPoint = next(),
        sliderKeyPointForeground = next(),
        sliderBackground = next(),
    )
}
