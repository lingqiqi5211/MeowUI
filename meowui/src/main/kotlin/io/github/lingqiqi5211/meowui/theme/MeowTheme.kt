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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemeColorSpec as MiuixColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle as MiuixPaletteStyle

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
    val onError: Color,
    // 成对的容器色。两套设计体系各自都有这一组，只是取值风格不同:Material 的容器带明显
    // 色调,Miuix 的更灰更平。调用侧自建的小色块(状态标签、图标底、状态卡)需要它们,
    // 而在这里各分支映射自己的调色板,就不会出现「Miuix 皮肤下画着 Material 容器色」。
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    /** 比 [surfaceVariant] 再高一档的中性底,用于卡片里还要再分一层的小色块。 */
    val surfaceContainerHighest: Color,
    val onSurfaceContainerHighest: Color,
    // 「成功」「警告」两档语义色。两套设计体系都没有定义它们:Material 只有 primary/
    // secondary/tertiary/error 四组,Miuix 连 tertiary 都只是一档蓝。而「正常 / 已降级」
    // 这种状态是应用普遍需要表达的,把它硬塞进 tertiary 会和「信息」撞色,塞进 error 又
    // 会把降级说成故障。因此这两档由库按深浅色给定,两种风格下取值一致 —— 状态色本身就
    // 该跨皮肤保持稳定,绿色是好、琥珀色是要注意。
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

/**
 * 语义状态色，按深浅色给出。
 *
 * 与调色板无关:它们表达的是「好 / 要注意」，不是主题强调色，跟着 seed 变会让绿色变成
 * 紫色,状态也就读不出来了。
 */
internal fun meowStatusColors(darkTheme: Boolean): MeowStatusColors = if (darkTheme) {
    MeowStatusColors(
        successContainer = Color(0xFF1E3B2A),
        onSuccessContainer = Color(0xFF8FD9A8),
        warningContainer = Color(0xFF3B2F14),
        onWarningContainer = Color(0xFFF0C069),
    )
} else {
    MeowStatusColors(
        successContainer = Color(0xFFDCF0E2),
        onSuccessContainer = Color(0xFF1B5E38),
        warningContainer = Color(0xFFFBEBD2),
        onWarningContainer = Color(0xFF7A4E07),
    )
}

@Immutable
internal data class MeowStatusColors(
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
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

// 配色与深浅色随主题设置频繁变化，用可追踪的 compositionLocalOf，
// 只重组真正读取它们的节点；static 版本会让整棵子树整体闪刷一次。
private val LocalMeowColors = compositionLocalOf<MeowColorScheme> {
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
internal val LocalMeowDarkTheme = compositionLocalOf { false }

/** 悬浮控件是否启用背景模糊,由统一外观入口写入。 */
internal val LocalMeowBlurEnabled = staticCompositionLocalOf { true }

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
     * @param dynamicColor Android 12+ 上跟随系统主色（跟随壁纸取色）；
     * 低版本或关闭时两个分支都回退到 [seedColor]。
     * @param seedColor 关闭动态取色时的种子色。Material 分支展开为完整的
     * MD3 tonal palette；Miuix 分支经 Miuix Monet 引擎生成同源配色。
     * @param paletteStyle 种子色展开为配色方案的调色板风格，两个分支共用。
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
        MeowThemeContent(
            style = style,
            darkTheme = darkTheme,
            amoledDark = false,
            dynamicColor = dynamicColor,
            seedColor = seedColor,
            paletteStyle = paletteStyle,
            colorSpec = null,
            miuixMonetEnabled = true,
            dimensions = dimensions,
            content = content,
        )
    }

    /**
     * 使用统一的 [MeowAppearance] 应用主题、色彩标准与界面缩放。
     *
     * 界面缩放限制在 80% 到 110%，并保留系统字体缩放比例。
     */
    @Composable
    operator fun invoke(
        appearance: MeowAppearance,
        dimensions: MeowDimensions = MeowDimensions(),
        content: @Composable () -> Unit,
    ) {
        val darkTheme = when (appearance.themeMode) {
            MeowThemeMode.System -> isSystemInDarkTheme()
            MeowThemeMode.Light -> false
            MeowThemeMode.Dark -> true
        }
        val baseDensity = LocalDensity.current
        val interfaceScale = appearance.interfaceScale
            .takeIf(Float::isFinite)
            ?.coerceIn(
                MeowAppearanceDefaults.MinInterfaceScale,
                MeowAppearanceDefaults.MaxInterfaceScale,
            ) ?: 1f
        val scaledDensity = remember(baseDensity, interfaceScale) {
            Density(
                density = baseDensity.density * interfaceScale,
                fontScale = baseDensity.fontScale,
            )
        }

        CompositionLocalProvider(
            LocalDensity provides scaledDensity,
            LocalMeowBlurEnabled provides appearance.blurEnabled,
        ) {
            MeowThemeContent(
                style = appearance.style,
                darkTheme = darkTheme,
                amoledDark = appearance.amoledDarkEnabled && darkTheme,
                dynamicColor = appearance.dynamicColor,
                seedColor = appearance.seedColor,
                paletteStyle = appearance.paletteStyle,
                colorSpec = appearance.colorSpec,
                miuixMonetEnabled = appearance.miuixMonetEnabled,
                dimensions = dimensions,
                content = content,
            )
        }
    }
}

@Composable
private fun MeowThemeContent(
    style: MeowUiStyle,
    darkTheme: Boolean,
    amoledDark: Boolean,
    dynamicColor: Boolean,
    seedColor: Color,
    paletteStyle: MeowPaletteStyle,
    colorSpec: MeowColorSpec?,
    miuixMonetEnabled: Boolean,
    dimensions: MeowDimensions,
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
                amoledDark = amoledDark,
                dynamicColor = dynamicColor,
                seedColor = seedColor,
                paletteStyle = paletteStyle,
                colorSpec = colorSpec,
                dimensions = dimensions,
                content = movableContent,
            )

            MeowUiStyle.Miuix -> MiuixContent(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor,
                seedColor = seedColor,
                paletteStyle = paletteStyle,
                colorSpec = colorSpec,
                monetEnabled = miuixMonetEnabled,
                dimensions = dimensions,
                content = movableContent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MaterialExpressiveContent(
    darkTheme: Boolean,
    amoledDark: Boolean,
    dynamicColor: Boolean,
    seedColor: Color,
    paletteStyle: MeowPaletteStyle,
    colorSpec: MeowColorSpec?,
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
    val baseColorScheme = remember(keyColor, darkTheme, amoledDark, paletteStyle, colorSpec) {
        meowMaterialColorScheme(
            seedColor = keyColor,
            isDark = darkTheme,
            paletteStyle = paletteStyle,
            colorSpec = colorSpec,
            isAmoled = amoledDark,
        )
    }
    val colorScheme = baseColorScheme.animateAsState()

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
    ) {
        val colors = MaterialTheme.colorScheme
        val status = meowStatusColors(darkTheme)
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
                onError = colors.onError,
                primaryContainer = colors.primaryContainer,
                onPrimaryContainer = colors.onPrimaryContainer,
                secondaryContainer = colors.secondaryContainer,
                onSecondaryContainer = colors.onSecondaryContainer,
                tertiaryContainer = colors.tertiaryContainer,
                onTertiaryContainer = colors.onTertiaryContainer,
                errorContainer = colors.errorContainer,
                onErrorContainer = colors.onErrorContainer,
                surfaceContainerHighest = colors.surfaceContainerHighest,
                onSurfaceContainerHighest = colors.onSurface,
                successContainer = status.successContainer,
                onSuccessContainer = status.onSuccessContainer,
                warningContainer = status.warningContainer,
                onWarningContainer = status.onWarningContainer,
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
    seedColor: Color,
    paletteStyle: MeowPaletteStyle,
    colorSpec: MeowColorSpec?,
    monetEnabled: Boolean,
    dimensions: MeowDimensions,
    content: @Composable () -> Unit,
) {
    // 启用 Monet 时从系统主色或调用侧种子色生成配色；关闭时使用 Miuix 原生配色。
    val keyColor = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        colorResource(id = android.R.color.system_accent1_500)
    } else {
        seedColor
    }
    val colorSchemeMode = when {
        !monetEnabled && darkTheme -> ColorSchemeMode.Dark
        !monetEnabled -> ColorSchemeMode.Light
        darkTheme -> ColorSchemeMode.MonetDark
        else -> ColorSchemeMode.MonetLight
    }
    val miuixPaletteStyle = paletteStyle.toMiuixPaletteStyle()
    val miuixColorSpec = when (colorSpec) {
        MeowColorSpec.Spec2021 -> MiuixColorSpec.Spec2021
        MeowColorSpec.Spec2025 -> if (paletteStyle.supportsSpec2025) {
            MiuixColorSpec.Spec2025
        } else {
            MiuixColorSpec.Spec2021
        }
        null -> if (paletteStyle.supportsSpec2025) {
            MiuixColorSpec.Spec2025
        } else {
            MiuixColorSpec.Spec2021
        }
    }
    val controller = remember(colorSchemeMode, keyColor, miuixPaletteStyle, miuixColorSpec, darkTheme) {
        ThemeController(
            colorSchemeMode = colorSchemeMode,
            keyColor = keyColor,
            paletteStyle = miuixPaletteStyle,
            colorSpec = miuixColorSpec,
            isDark = darkTheme,
        )
    }

    // 外层提供 colorSchemeMode 等控制器信息；内层用逐色动画后的配色覆盖颜色，
    // 让 Monet 开关、种子色切换时 Miuix 原生组件的颜色平滑过渡而不是硬切。
    MiuixTheme(controller = controller) {
        val animatedColors = MiuixTheme.colorScheme.animateAsState()
        MiuixContentInner(
            animatedColors = animatedColors,
            dimensions = dimensions,
            content = content,
        )
    }
}

@Composable
private fun MiuixContentInner(
    animatedColors: top.yukonga.miuix.kmp.theme.Colors,
    dimensions: MeowDimensions,
    content: @Composable () -> Unit,
) {
    MiuixTheme(colors = animatedColors) {
        val colors = MiuixTheme.colorScheme
        // 这层拿不到 darkTheme 参数，读上游已经写入的 local，避免为一个布尔再穿一层参数。
        val status = meowStatusColors(LocalMeowDarkTheme.current)
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
                onError = colors.onError,
                primaryContainer = colors.primaryContainer,
                onPrimaryContainer = colors.onPrimaryContainer,
                secondaryContainer = colors.secondaryContainer,
                onSecondaryContainer = colors.onSecondaryContainer,
                tertiaryContainer = colors.tertiaryContainer,
                onTertiaryContainer = colors.onTertiaryContainer,
                errorContainer = colors.errorContainer,
                onErrorContainer = colors.onErrorContainer,
                surfaceContainerHighest = colors.surfaceContainerHighest,
                onSurfaceContainerHighest = colors.onSurfaceContainerHighest,
                successContainer = status.successContainer,
                onSuccessContainer = status.onSuccessContainer,
                warningContainer = status.warningContainer,
                onWarningContainer = status.onWarningContainer,
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

private fun MeowPaletteStyle.toMiuixPaletteStyle(): MiuixPaletteStyle = when (this) {
    MeowPaletteStyle.TonalSpot -> MiuixPaletteStyle.TonalSpot
    MeowPaletteStyle.Neutral -> MiuixPaletteStyle.Neutral
    MeowPaletteStyle.Vibrant -> MiuixPaletteStyle.Vibrant
    MeowPaletteStyle.Expressive -> MiuixPaletteStyle.Expressive
    MeowPaletteStyle.Rainbow -> MiuixPaletteStyle.Rainbow
    MeowPaletteStyle.FruitSalad -> MiuixPaletteStyle.FruitSalad
    MeowPaletteStyle.Monochrome -> MiuixPaletteStyle.Monochrome
    MeowPaletteStyle.Fidelity -> MiuixPaletteStyle.Fidelity
    MeowPaletteStyle.Content -> MiuixPaletteStyle.Content
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
