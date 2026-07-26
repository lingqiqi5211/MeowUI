package io.github.lingqiqi5211.meowui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Immutable
data class MeowColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val divider: Color,
    val error: Color,
)

@Immutable
data class MeowTypography(
    val pageTitle: TextStyle,
    val sectionTitle: TextStyle,
    val title: TextStyle,
    val summary: TextStyle,
    val value: TextStyle,
    val button: TextStyle,
)

@Immutable
data class MeowShapes(
    val section: Shape,
    val item: Shape,
    val dialog: Shape,
)

@Immutable
data class MeowDimensions(
    val pageHorizontalPadding: Dp = 16.dp,
    val pageVerticalPadding: Dp = 12.dp,
    val sectionSpacing: Dp = 18.dp,
    val itemHorizontalPadding: Dp = 18.dp,
    val itemVerticalPadding: Dp = 14.dp,
)

private val LocalMeowUiStyle = staticCompositionLocalOf { MeowUiStyle.MaterialExpressive }
private val LocalMeowColors = staticCompositionLocalOf<MeowColorScheme> {
    error("MeowTheme is missing")
}
private val LocalMeowTypography = staticCompositionLocalOf<MeowTypography> {
    error("MeowTheme is missing")
}
private val LocalMeowShapes = staticCompositionLocalOf<MeowShapes> {
    error("MeowTheme is missing")
}
private val LocalMeowDimensions = staticCompositionLocalOf { MeowDimensions() }

/** 当前主题是否为深色，供库内组件做深浅色差异化（如阴影浓度）。 */
internal val LocalMeowDarkTheme = staticCompositionLocalOf { false }

object MeowTheme {
    val style: MeowUiStyle
        @Composable
        @ReadOnlyComposable
        get() = LocalMeowUiStyle.current

    val colors: MeowColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMeowColors.current

    val typography: MeowTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalMeowTypography.current

    val shapes: MeowShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalMeowShapes.current

    val dimensions: MeowDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalMeowDimensions.current

    /**
     * MeowUI 主题入口。
     *
     * @param style 当前 UI 风格，决定走 Material 3 Expressive 还是 Miuix 分支。
     * @param darkTheme 是否使用深色主题，同时驱动状态栏与导航栏图标亮暗。
     * @param dynamicColor Android 12+ 上跟随系统取色。Material 分支以系统主色为种子
     * 展开调色板；Miuix 分支使用 Monet 配色。低版本或关闭时回退 [seedColor] 与
     * Miuix 自身配色。
     * @param seedColor 关闭动态取色时 Material 分支的种子色，会展开为完整的
     * MD3 tonal palette，而不是仅替换 primary。不影响 Miuix 分支。
     * @param paletteStyle 种子色展开为配色方案的调色板风格，仅影响 Material 分支。
     * @param dimensions 页面级间距 token。
     */
    @Composable
    operator fun invoke(
        style: MeowUiStyle,
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = true,
        seedColor: Color = Color(0xFF6750A4),
        paletteStyle: MeowPaletteStyle = MeowPaletteStyle.TonalSpot,
        dimensions: MeowDimensions = MeowDimensions(),
        content: @Composable () -> Unit,
    ) {
        val latestContent by rememberUpdatedState(content)
        val movableContent = remember {
            movableContentOf { latestContent() }
        }

        SystemBarAppearanceEffect(darkTheme = darkTheme)

        CompositionLocalProvider(LocalMeowDarkTheme provides darkTheme) {
            when (style) {
                MeowUiStyle.MaterialExpressive -> MaterialExpressiveContent(
                    darkTheme = darkTheme,
                    dynamicColor = dynamicColor,
                    seedColor = seedColor,
                    paletteStyle = paletteStyle,
                    dimensions = dimensions,
                    content = movableContent,
                )

                MeowUiStyle.Miuix -> MiuixContent(
                    darkTheme = darkTheme,
                    dynamicColor = dynamicColor,
                    dimensions = dimensions,
                    content = movableContent,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MaterialExpressiveContent(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    seedColor: Color,
    paletteStyle: MeowPaletteStyle,
    dimensions: MeowDimensions,
    content: @Composable () -> Unit,
) {
    // 动态取色时以系统主色为种子，其余情况用调用侧种子色；
    // 两条路径都展开为完整 tonal palette，保证所有色 role 与种子一致。
    val keyColor = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        colorResource(id = android.R.color.system_accent1_500)
    } else {
        seedColor
    }
    val baseColorScheme = remember(keyColor, darkTheme, paletteStyle) {
        meowMaterialColorScheme(
            seedColor = keyColor,
            isDark = darkTheme,
            paletteStyle = paletteStyle,
        )
    }
    val colorScheme = baseColorScheme.animateAsState()

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
    ) {
        val colors = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography
        CompositionLocalProvider(
            LocalMeowUiStyle provides MeowUiStyle.MaterialExpressive,
            LocalMeowColors provides MeowColorScheme(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                background = colors.background,
                onBackground = colors.onBackground,
                surface = colors.surface,
                onSurface = colors.onSurface,
                surfaceVariant = colors.surfaceContainer,
                onSurfaceVariant = colors.onSurfaceVariant,
                outline = colors.outline,
                divider = colors.outlineVariant,
                error = colors.error,
            ),
            LocalMeowTypography provides MeowTypography(
                pageTitle = typography.headlineMedium,
                sectionTitle = typography.titleSmall,
                title = typography.bodyLarge,
                summary = typography.bodyMedium,
                value = typography.labelLarge,
                button = typography.labelLarge,
            ),
            LocalMeowShapes provides MeowShapes(
                section = MaterialTheme.shapes.extraLarge,
                item = MaterialTheme.shapes.large,
                dialog = MaterialTheme.shapes.extraLarge,
            ),
            LocalMeowDimensions provides dimensions,
            content = content,
        )
    }
}

@Composable
private fun MiuixContent(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    dimensions: MeowDimensions,
    content: @Composable () -> Unit,
) {
    val colorSchemeMode = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight
        }

        darkTheme -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.Light
    }
    val controller = remember(colorSchemeMode) {
        ThemeController(colorSchemeMode = colorSchemeMode)
    }

    MiuixTheme(controller = controller) {
        val colors = MiuixTheme.colorScheme
        val typography = MiuixTheme.textStyles
        CompositionLocalProvider(
            LocalMeowUiStyle provides MeowUiStyle.Miuix,
            LocalMeowColors provides MeowColorScheme(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                background = colors.background,
                onBackground = colors.onBackground,
                surface = colors.surface,
                onSurface = colors.onSurface,
                surfaceVariant = colors.surfaceContainer,
                onSurfaceVariant = colors.onSurfaceVariantSummary,
                outline = colors.outline,
                divider = colors.dividerLine,
                error = colors.error,
            ),
            LocalMeowTypography provides MeowTypography(
                pageTitle = typography.title2,
                sectionTitle = typography.subtitle,
                title = typography.main,
                summary = typography.body2,
                value = typography.body2,
                button = typography.button,
            ),
            LocalMeowShapes provides MeowShapes(
                section = RoundedCornerShape(18.dp),
                item = RoundedCornerShape(14.dp),
                dialog = RoundedCornerShape(24.dp),
            ),
            LocalMeowDimensions provides dimensions,
            content = content,
        )
    }
}

/**
 * 让状态栏与导航栏前景亮暗跟随主题，而不是跟随系统深色模式，
 * 并关闭系统对导航栏强制加的对比度遮罩，保证边到边内容连续。
 */
@Composable
private fun SystemBarAppearanceEffect(darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
internal fun MeowStyleContent(
    materialExpressive: @Composable () -> Unit,
    miuix: @Composable () -> Unit,
) {
    when (MeowTheme.style) {
        MeowUiStyle.MaterialExpressive -> materialExpressive()
        MeowUiStyle.Miuix -> miuix()
    }
}
