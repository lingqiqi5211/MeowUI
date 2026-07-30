package io.github.lingqiqi5211.meowui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.Colors

/**
 * 对 Miuix [Colors] 的全部色 role 做 spring 过渡。
 *
 * Miuix 原生主题在 ThemeController 切换（如开关 Monet）时颜色硬切，
 * 这里仿照 Material 分支的做法逐色动画，让配色平滑变化。
 * 首帧直接取目标值，不会从错误颜色开始播放动画。
 */
@Composable
internal fun Colors.animateAsState(): Colors {
    @Composable
    fun animateColor(color: Color): Color = animateColorAsState(
        targetValue = color,
        animationSpec = spring(),
        label = "meow_miuix_color",
    ).value

    return Colors(
        primary = animateColor(primary),
        onPrimary = animateColor(onPrimary),
        primaryVariant = animateColor(primaryVariant),
        onPrimaryVariant = animateColor(onPrimaryVariant),
        error = animateColor(error),
        onError = animateColor(onError),
        errorContainer = animateColor(errorContainer),
        onErrorContainer = animateColor(onErrorContainer),
        disabledPrimary = animateColor(disabledPrimary),
        disabledOnPrimary = animateColor(disabledOnPrimary),
        disabledPrimaryButton = animateColor(disabledPrimaryButton),
        disabledOnPrimaryButton = animateColor(disabledOnPrimaryButton),
        disabledPrimarySlider = animateColor(disabledPrimarySlider),
        primaryContainer = animateColor(primaryContainer),
        onPrimaryContainer = animateColor(onPrimaryContainer),
        secondary = animateColor(secondary),
        onSecondary = animateColor(onSecondary),
        secondaryVariant = animateColor(secondaryVariant),
        onSecondaryVariant = animateColor(onSecondaryVariant),
        disabledSecondary = animateColor(disabledSecondary),
        disabledOnSecondary = animateColor(disabledOnSecondary),
        disabledSecondaryVariant = animateColor(disabledSecondaryVariant),
        disabledOnSecondaryVariant = animateColor(disabledOnSecondaryVariant),
        secondaryContainer = animateColor(secondaryContainer),
        onSecondaryContainer = animateColor(onSecondaryContainer),
        secondaryContainerVariant = animateColor(secondaryContainerVariant),
        onSecondaryContainerVariant = animateColor(onSecondaryContainerVariant),
        tertiaryContainer = animateColor(tertiaryContainer),
        onTertiaryContainer = animateColor(onTertiaryContainer),
        tertiaryContainerVariant = animateColor(tertiaryContainerVariant),
        background = animateColor(background),
        onBackground = animateColor(onBackground),
        onBackgroundVariant = animateColor(onBackgroundVariant),
        surface = animateColor(surface),
        onSurface = animateColor(onSurface),
        surfaceVariant = animateColor(surfaceVariant),
        onSurfaceSecondary = animateColor(onSurfaceSecondary),
        onSurfaceVariantSummary = animateColor(onSurfaceVariantSummary),
        onSurfaceVariantActions = animateColor(onSurfaceVariantActions),
        disabledOnSurface = animateColor(disabledOnSurface),
        surfaceContainer = animateColor(surfaceContainer),
        onSurfaceContainer = animateColor(onSurfaceContainer),
        onSurfaceContainerVariant = animateColor(onSurfaceContainerVariant),
        surfaceContainerHigh = animateColor(surfaceContainerHigh),
        onSurfaceContainerHigh = animateColor(onSurfaceContainerHigh),
        surfaceContainerHighest = animateColor(surfaceContainerHighest),
        onSurfaceContainerHighest = animateColor(onSurfaceContainerHighest),
        outline = animateColor(outline),
        dividerLine = animateColor(dividerLine),
        windowDimming = animateColor(windowDimming),
        sliderKeyPoint = animateColor(sliderKeyPoint),
        sliderKeyPointForeground = animateColor(sliderKeyPointForeground),
        sliderBackground = animateColor(sliderBackground),
    )
}
