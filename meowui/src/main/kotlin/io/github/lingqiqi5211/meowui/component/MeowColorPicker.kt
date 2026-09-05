package io.github.lingqiqi5211.meowui.component

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.LocalMeowDarkTheme
import io.github.lingqiqi5211.meowui.theme.MeowColorSpec
import io.github.lingqiqi5211.meowui.theme.MeowPaletteStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import io.github.lingqiqi5211.meowui.theme.meowMaterialColorScheme
import io.github.lingqiqi5211.meowui.theme.supportsSpec2025
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.window.WindowDialog

/** [MeowColorPickerDialog] 的默认值。 */
object MeowColorPickerDefaults {
    /** 默认的自定义种子色列表。 */
    val PresetColors: List<Color> = listOf(
        Color(0xFF7B4DFF),
        Color(0xFFB94073),
        Color(0xFFBA1A1A),
        Color(0xFF944A00),
        Color(0xFF795900),
        Color(0xFF006D39),
        Color(0xFF006A64),
        Color(0xFF00639B),
        Color(0xFF335BBC),
        Color(0xFF6750A4),
        Color(0xFF575D7E),
        Color(0xFF5F6162),
    )
}

/**
 * 统一取色窗口，参考 KernelSU 的取色页面：第一个色块表示跟随壁纸（系统动态取色，
 * Android 12+ 显示），其余为自定义种子色；每个色块用该种子展开后的配色画双色圆弧预览。
 *
 * 选择立即通过回调生效（与 KernelSU 一致的即时预览语义），窗口用确认按钮关闭。
 *
 * @param show 是否显示窗口。
 * @param dynamicColor 当前是否跟随壁纸取色。
 * @param seedColor 当前自定义种子色。
 * @param onDynamicColorChange 切换跟随壁纸/自定义时回调。
 * @param onSeedColorChange 选中某个自定义种子色时回调（同时会回调 `onDynamicColorChange(false)`）。
 * @param onDismissRequest 请求关闭窗口。
 * @param presetColors 自定义色列表。
 */
@Composable
fun MeowColorPickerDialog(
    show: Boolean,
    dynamicColor: Boolean,
    seedColor: Color,
    onDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    title: String = "Theme color",
    confirmText: String = "Done",
    presetColors: List<Color> = MeowColorPickerDefaults.PresetColors,
    paletteStyle: MeowPaletteStyle = MeowPaletteStyle.TonalSpot,
    colorSpec: MeowColorSpec? = null,
) {
    MeowStyleContent(
        materialExpressive = {
            if (show) {
                AlertDialog(
                    onDismissRequest = onDismissRequest,
                    title = { MaterialText(title) },
                    text = {
                        ColorPickerContent(
                            dynamicColor = dynamicColor,
                            seedColor = seedColor,
                            presetColors = presetColors,
                            paletteStyle = paletteStyle,
                            colorSpec = colorSpec,
                            onDynamicColorChange = onDynamicColorChange,
                            onSeedColorChange = onSeedColorChange,
                        )
                    },
                    confirmButton = {
                        MaterialTextButton(onClick = onDismissRequest) {
                            MaterialText(confirmText)
                        }
                    },
                )
            }
        },
        miuix = {
            WindowDialog(
                show = show,
                title = title,
                onDismissRequest = onDismissRequest,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ColorPickerContent(
                        dynamicColor = dynamicColor,
                        seedColor = seedColor,
                        presetColors = presetColors,
                        paletteStyle = paletteStyle,
                        colorSpec = colorSpec,
                        onDynamicColorChange = onDynamicColorChange,
                        onSeedColorChange = onSeedColorChange,
                    )
                    Spacer(Modifier.height(20.dp))
                    MiuixDialogButtons(
                        confirmText = confirmText,
                        cancelText = null,
                        onConfirm = onDismissRequest,
                        onCancel = onDismissRequest,
                    )
                }
            }
        },
    )
}

/**
 * 可直接放进页面的横向主题色选择器。
 *
 * 色票会使用当前 [paletteStyle] 与 [colorSpec] 生成预览；Android 12 以上额外显示
 * 跟随壁纸选项。选择自定义色时会先回调 `onDynamicColorChange(false)`，再回调
 * [onSeedColorChange]。
 */
@Composable
fun MeowColorPicker(
    dynamicColor: Boolean,
    seedColor: Color,
    onDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    presetColors: List<Color> = MeowColorPickerDefaults.PresetColors,
    paletteStyle: MeowPaletteStyle = MeowPaletteStyle.TonalSpot,
    colorSpec: MeowColorSpec? = null,
    customColorTitle: String = "Custom color",
    confirmText: String = "Done",
    cancelText: String = "Cancel",
) {
    ColorPickerContent(
        dynamicColor = dynamicColor,
        seedColor = seedColor,
        presetColors = presetColors,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
        onDynamicColorChange = onDynamicColorChange,
        onSeedColorChange = onSeedColorChange,
        modifier = modifier,
        customColorTitle = customColorTitle,
        confirmText = confirmText,
        cancelText = cancelText,
    )
}

@Composable
private fun ColorPickerContent(
    dynamicColor: Boolean,
    seedColor: Color,
    presetColors: List<Color>,
    paletteStyle: MeowPaletteStyle,
    colorSpec: MeowColorSpec?,
    onDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    customColorTitle: String = "Custom color",
    confirmText: String = "Done",
    cancelText: String = "Cancel",
) {
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val distinctPresets = presetColors.distinct()
    val cells = buildList {
        if (supportsDynamic) add(ColorPickerCell.FollowWallpaper)
        distinctPresets.forEach { add(ColorPickerCell.Preset(it)) }
        // 调色盘:自选任意种子色(参考 miuix 官方示例的 ColorPicker)。
        add(ColorPickerCell.Custom)
    }
    val customSelected = !dynamicColor && distinctPresets.none { it == seedColor }
    var showCustomDialog by remember { mutableStateOf(false) }
    // 选中一个色票即时生效，等同于在弹窗里选中一项：响 Confirm。
    val haptics = rememberMeowHaptics()

    MeowColorPaletteDialog(
        show = showCustomDialog,
        title = customColorTitle,
        confirmText = confirmText,
        cancelText = cancelText,
        initialColor = seedColor,
        onConfirm = { color ->
            showCustomDialog = false
            onDynamicColorChange(false)
            onSeedColorChange(color)
        },
        onDismissRequest = { showCustomDialog = false },
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // 色块尺寸按可用宽度反推：恰好显示 4 个半，露出的半个提示可以左右滑动。
        val swatchSize = (
            (maxWidth - SwatchContentPadding - SwatchSpacing * 4) / 4.5f
            ).coerceIn(44.dp, 72.dp)

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            contentPadding = PaddingValues(horizontal = SwatchContentPadding),
            horizontalArrangement = Arrangement.spacedBy(SwatchSpacing),
        ) {
            items(
                items = cells,
                key = { cell ->
                    when (cell) {
                        ColorPickerCell.FollowWallpaper -> "wallpaper"
                        is ColorPickerCell.Preset -> "preset-${cell.color.value}"
                        ColorPickerCell.Custom -> "custom"
                    }
                },
            ) { cell ->
                when (cell) {
                    ColorPickerCell.Custom -> CustomColorSwatch(
                        selected = customSelected,
                        currentColor = seedColor,
                        size = swatchSize,
                        onClick = { showCustomDialog = true },
                    )

                    ColorPickerCell.FollowWallpaper -> {
                        val systemSeed = colorResource(id = android.R.color.system_accent1_500)
                        SchemeSwatch(
                            seed = systemSeed,
                            selected = dynamicColor,
                            followWallpaper = true,
                            paletteStyle = paletteStyle,
                            colorSpec = colorSpec,
                            size = swatchSize,
                            contentDescription = "Follow wallpaper colors",
                            onClick = {
                                haptics.picked()
                                onDynamicColorChange(true)
                            },
                        )
                    }

                    is ColorPickerCell.Preset -> SchemeSwatch(
                        seed = cell.color,
                        selected = !dynamicColor && cell.color == seedColor,
                        followWallpaper = false,
                        paletteStyle = paletteStyle,
                        colorSpec = colorSpec,
                        size = swatchSize,
                        contentDescription = remember(cell.color) {
                            "Theme color #%06X".format(cell.color.toArgb() and 0xFFFFFF)
                        },
                        onClick = {
                            haptics.picked()
                            onDynamicColorChange(false)
                            onSeedColorChange(cell.color)
                        },
                    )
                }
            }
        }
    }
}

private val SwatchContentPadding = 16.dp
private val SwatchSpacing = 14.dp

private sealed interface ColorPickerCell {
    data object FollowWallpaper : ColorPickerCell

    data class Preset(val color: Color) : ColorPickerCell

    data object Custom : ColorPickerCell
}

/** KernelSU 式配色色块，外层卡片与选中反馈均参与动画；内部图形随 [size] 等比缩放。 */
@Composable
private fun SchemeSwatch(
    seed: Color,
    selected: Boolean,
    followWallpaper: Boolean,
    paletteStyle: MeowPaletteStyle,
    colorSpec: MeowColorSpec?,
    size: Dp,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val isDark = LocalMeowDarkTheme.current
    val scheme = rememberSwatchScheme(
        seed = seed,
        isDark = isDark,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )

    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        label = "meow_color_swatch_scale",
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(scheme.surfaceContainer)
            .semantics { this.contentDescription = contentDescription }
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size * 2f / 3f)) {
            drawArc(
                color = scheme.primaryContainer,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
            )
            drawArc(
                color = scheme.tertiaryContainer,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
            )
        }

        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = selectionScale
                scaleY = selectionScale
            },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
            ) {
                Box(
                    modifier = Modifier
                        .size(size * 0.78f)
                        .border(2.dp, scheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(size / 3f)
                            .clip(CircleShape)
                            .background(scheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(size * 0.22f),
                            colorFilter = ColorFilter.tint(scheme.onPrimary),
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = !selected,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
            ) {
                if (followWallpaper) {
                    Image(
                        imageVector = Icons.Rounded.Wallpaper,
                        contentDescription = null,
                        modifier = Modifier.size(size * 0.28f),
                        colorFilter = ColorFilter.tint(scheme.primary),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(size * 0.28f)
                            .clip(CircleShape)
                            .background(scheme.primary),
                    )
                }
            }
        }
    }
}

/** 调色盘色块：彩虹渐变圆表示“自选任意颜色”，选中时用当前种子色描边确认。 */
@Composable
private fun CustomColorSwatch(
    selected: Boolean,
    currentColor: Color,
    size: Dp,
    onClick: () -> Unit,
) {
    val selectionScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        label = "meow_color_swatch_scale",
    )
    val rainbow = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFFE57373),
                Color(0xFFFFD54F),
                Color(0xFF81C784),
                Color(0xFF4DD0E1),
                Color(0xFF7986CB),
                Color(0xFFBA68C8),
                Color(0xFFE57373),
            ),
        )
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(MeowTheme.colors.surfaceVariant)
            .semantics { this.contentDescription = "Custom color" }
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size * 2f / 3f)
                .clip(CircleShape)
                .background(rainbow),
        )
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = selectionScale
                scaleY = selectionScale
            },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
            ) {
                Box(
                    modifier = Modifier
                        .size(size * 0.78f)
                        .border(2.dp, MeowTheme.colors.onSurface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(size / 3f)
                            .clip(CircleShape)
                            .background(currentColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(size * 0.22f),
                            colorFilter = ColorFilter.tint(
                                if (currentColor.luminance() > 0.5f) Color.Black else Color.White,
                            ),
                        )
                    }
                }
            }
        }
    }
}

/** 色块预览需要的 5 个色 role，避免为每个色块保留完整 ColorScheme。 */
@Immutable
private data class SwatchScheme(
    val surfaceContainer: Color,
    val primaryContainer: Color,
    val tertiaryContainer: Color,
    val primary: Color,
    val onPrimary: Color,
)

private data class SwatchSchemeKey(
    val seed: Color,
    val isDark: Boolean,
    val paletteStyle: MeowPaletteStyle,
    val colorSpec: MeowColorSpec?,
)

/** 进程级缓存：同一种子只展开一次，重进页面时同步命中，不再掉帧。 */
private val swatchSchemeCache = ConcurrentHashMap<SwatchSchemeKey, SwatchScheme>()

/**
 * 色块配色的生成放到后台线程：tonal palette 全量展开较贵，13 个色块在首帧
 * 同步生成会让进入页面明显卡顿。未生成完成前先用种子色混出近似占位色。
 */
@Composable
private fun rememberSwatchScheme(
    seed: Color,
    isDark: Boolean,
    paletteStyle: MeowPaletteStyle,
    colorSpec: MeowColorSpec?,
): SwatchScheme {
    // null 与不支持 2025 的组合在生成侧等价于对应的有效 spec，
    // 归一化后作为缓存键，避免同一配色存多份。
    val effectiveSpec = when {
        colorSpec == MeowColorSpec.Spec2021 -> MeowColorSpec.Spec2021
        paletteStyle.supportsSpec2025 -> MeowColorSpec.Spec2025
        else -> MeowColorSpec.Spec2021
    }
    val key = SwatchSchemeKey(seed, isDark, paletteStyle, effectiveSpec)
    swatchSchemeCache[key]?.let { return it }

    // 以 key 为 remember 键：key 变化且缓存未命中时立即回到占位色，而不是闪现旧配色。
    var generated by remember(key) { mutableStateOf(swatchSchemeCache[key]) }
    LaunchedEffect(key) {
        generated = withContext(Dispatchers.Default) {
            swatchSchemeCache.getOrPut(key) {
                val scheme = meowMaterialColorScheme(
                    seedColor = key.seed,
                    isDark = key.isDark,
                    paletteStyle = key.paletteStyle,
                    colorSpec = key.colorSpec,
                )
                SwatchScheme(
                    surfaceContainer = scheme.surfaceContainer,
                    primaryContainer = scheme.primaryContainer,
                    tertiaryContainer = scheme.tertiaryContainer,
                    primary = scheme.primary,
                    onPrimary = scheme.onPrimary,
                )
            }
        }
    }
    return generated ?: placeholderSwatchScheme(seed, isDark)
}

private fun placeholderSwatchScheme(seed: Color, isDark: Boolean): SwatchScheme {
    val base = if (isDark) Color.Black else Color.White
    val container = lerp(seed, base, 0.6f)
    return SwatchScheme(
        surfaceContainer = lerp(seed, base, 0.85f),
        primaryContainer = container,
        tertiaryContainer = container,
        primary = seed,
        onPrimary = base,
    )
}
