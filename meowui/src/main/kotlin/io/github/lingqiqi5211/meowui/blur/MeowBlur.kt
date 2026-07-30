package io.github.lingqiqi5211.meowui.blur

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.component.MeowScaffoldEffect
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRuntimeShaderSupported

@Stable
class MeowBlurBackdrop internal constructor(
    internal val delegate: LayerBackdrop,
)

object MeowBlurDefaults {
    const val Radius = 25f
    const val SurfaceAlpha = 0.8f
    const val FloatingSurfaceAlpha = 0.65f
}

private val FloatingBottomBarCornerRadius = 28.dp

fun isMeowBlurSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        isRuntimeShaderSupported()

@Composable
fun rememberMeowBlurBackdrop(
    enabled: Boolean = true,
): MeowBlurBackdrop? {
    if (!enabled || !isMeowBlurSupported()) {
        return null
    }

    val surfaceColor = meowBlurSurfaceColor()
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    return remember(backdrop) { MeowBlurBackdrop(backdrop) }
}

fun Modifier.meowBlurSource(backdrop: MeowBlurBackdrop?): Modifier =
    if (backdrop == null) this else layerBackdrop(backdrop.delegate)

@Composable
fun Modifier.meowBlurSurface(
    backdrop: MeowBlurBackdrop?,
    shape: Shape = RectangleShape,
    blurRadius: Float = MeowBlurDefaults.Radius,
    surfaceAlpha: Float = MeowBlurDefaults.SurfaceAlpha,
    surfaceColor: Color = meowBlurSurfaceColor(),
): Modifier {
    if (backdrop == null) return background(surfaceColor, shape)

    val blendColor = surfaceColor.copy(alpha = surfaceAlpha.coerceIn(0f, 1f))
    return textureBlur(
        backdrop = backdrop.delegate,
        shape = shape,
        blurRadius = blurRadius.coerceAtLeast(0f),
        colors = BlurColors(
            blendColors = listOf(BlendColorEntry(blendColor)),
        ),
    )
}

/**
 * Creates a scaffold effect that blurs the top bar and bottom bar backgrounds.
 * Unsupported Android versions automatically use opaque themed surfaces.
 */
@Composable
fun rememberMeowBlurScaffoldEffect(
    enabled: Boolean = true,
    blurRadius: Float = MeowBlurDefaults.Radius,
    surfaceAlpha: Float = MeowBlurDefaults.SurfaceAlpha,
    floatingSurfaceAlpha: Float = MeowBlurDefaults.FloatingSurfaceAlpha,
): MeowScaffoldEffect {
    val backdrop = rememberMeowBlurBackdrop(enabled)
    val barSurfaceColor = meowBlurSurfaceColor()
    val floatingBarSurfaceColor = MeowTheme.colors.surfaceVariant
    val floatingBarShape = when (MeowTheme.style) {
        MeowUiStyle.MaterialExpressive -> CircleShape
        MeowUiStyle.Miuix -> RoundedCornerShape(FloatingBottomBarCornerRadius)
    }
    val containerColor = if (backdrop == null) barSurfaceColor else Color.Transparent
    val floatingContainerColor = if (backdrop == null) {
        floatingBarSurfaceColor
    } else {
        Color.Transparent
    }

    return MeowScaffoldEffect(
        contentModifier = Modifier.meowBlurSource(backdrop),
        topBarModifier = Modifier.meowBlurSurface(
            backdrop = backdrop,
            blurRadius = blurRadius,
            surfaceAlpha = surfaceAlpha,
            surfaceColor = barSurfaceColor,
        ),
        topBarContainerColor = containerColor,
        bottomBarModifier = Modifier.meowBlurSurface(
            backdrop = backdrop,
            blurRadius = blurRadius,
            surfaceAlpha = surfaceAlpha,
            surfaceColor = barSurfaceColor,
        ),
        bottomBarContainerColor = containerColor,
        floatingBottomBarModifier = Modifier.meowBlurSurface(
            backdrop = backdrop,
            shape = floatingBarShape,
            blurRadius = blurRadius,
            surfaceAlpha = floatingSurfaceAlpha,
            surfaceColor = floatingBarSurfaceColor,
        ),
        floatingBottomBarContainerColor = floatingContainerColor,
    )
}

@Composable
private fun meowBlurSurfaceColor(): Color = when (MeowTheme.style) {
    MeowUiStyle.MaterialExpressive -> MeowTheme.colors.surfaceVariant
    MeowUiStyle.Miuix -> MeowTheme.colors.surface
}
