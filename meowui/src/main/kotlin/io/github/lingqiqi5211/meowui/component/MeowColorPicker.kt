package io.github.lingqiqi5211.meowui.component

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.LocalMeowDarkTheme
import io.github.lingqiqi5211.meowui.theme.MeowPaletteStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.meowMaterialColorScheme
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

@Composable
private fun ColorPickerContent(
    dynamicColor: Boolean,
    seedColor: Color,
    presetColors: List<Color>,
    onDynamicColorChange: (Boolean) -> Unit,
    onSeedColorChange: (Color) -> Unit,
) {
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val cells = buildList {
            if (supportsDynamic) add(ColorPickerCell.FollowWallpaper)
            presetColors.forEach { add(ColorPickerCell.Preset(it)) }
        }
        cells.chunked(SwatchesPerRow).forEach { rowCells ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowCells.forEach { cell ->
                    when (cell) {
                        ColorPickerCell.FollowWallpaper -> {
                            val systemSeed = colorResource(id = android.R.color.system_accent1_500)
                            SchemeSwatch(
                                seed = systemSeed,
                                selected = dynamicColor,
                                followWallpaper = true,
                                onClick = { onDynamicColorChange(true) },
                            )
                        }

                        is ColorPickerCell.Preset -> SchemeSwatch(
                            seed = cell.color,
                            selected = !dynamicColor && cell.color == seedColor,
                            followWallpaper = false,
                            onClick = {
                                onDynamicColorChange(false)
                                onSeedColorChange(cell.color)
                            },
                        )
                    }
                }
            }
        }
    }
}

private sealed interface ColorPickerCell {
    data object FollowWallpaper : ColorPickerCell

    data class Preset(val color: Color) : ColorPickerCell
}

private const val SwatchesPerRow = 5

/**
 * KernelSU 式配色色块：用种子展开后的 primaryContainer / tertiaryContainer 画上下双色圆弧，
 * 选中显示描边与对勾，未选中显示 primary 圆点；跟随壁纸的色块中央显示壁纸图标。
 */
@Composable
private fun SchemeSwatch(
    seed: Color,
    selected: Boolean,
    followWallpaper: Boolean,
    onClick: () -> Unit,
) {
    val isDark = LocalMeowDarkTheme.current
    val scheme = remember(seed, isDark) {
        meowMaterialColorScheme(
            seedColor = seed,
            isDark = isDark,
            paletteStyle = MeowPaletteStyle.TonalSpot,
        )
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(scheme.surfaceContainerHighest)
            .then(
                if (selected) {
                    Modifier.border(2.dp, scheme.primary, CircleShape)
                } else {
                    Modifier
                },
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
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

        when {
            selected -> Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(scheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(scheme.onPrimary),
                )
            }

            followWallpaper -> Image(
                imageVector = Icons.Rounded.Wallpaper,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(scheme.primary),
            )

            else -> Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(scheme.primary),
            )
        }
    }
}
