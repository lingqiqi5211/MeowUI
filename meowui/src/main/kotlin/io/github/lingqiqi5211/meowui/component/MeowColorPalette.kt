package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import top.yukonga.miuix.kmp.basic.ColorPalette as MiuixColorPalette
import top.yukonga.miuix.kmp.basic.ColorPicker as MiuixColorPicker
import top.yukonga.miuix.kmp.basic.ColorSpace as MiuixColorSpace
import top.yukonga.miuix.kmp.window.WindowDialog

/** [MeowColorPalette] 的呈现形态。 */
enum class MeowColorPaletteMode {
    /** 滑条式取色（色相/饱和度/明度等各一条滑条，带触觉反馈）。 */
    Sliders,

    /** 网格式取色盘（HSV 色块网格）。 */
    Grid,
}

/** [MeowColorPaletteMode.Sliders] 使用的色彩空间。 */
enum class MeowColorPaletteSpace {
    Hsv,
    OkHsv,
    OkLab,
    OkLch,
}

/**
 * 调色盘控件：自选任意颜色，两种风格共用同一实现。
 *
 * 底层是 miuix 的取色组件（纯 Canvas 绘制、无风格专属视觉），因此 Material 3
 * Expressive 与 Miuix 页面都可以直接内嵌；需要弹窗形态时用 [MeowColorPaletteDialog]。
 *
 * @param color 当前颜色。
 * @param onColorChanged 颜色变化回调（拖动过程中持续回调）。
 * @param mode 滑条式或网格式，见 [MeowColorPaletteMode]。
 * @param colorSpace 滑条式使用的色彩空间，网格式忽略。
 * @param showPreview 是否显示当前颜色预览条。
 */
@Composable
fun MeowColorPalette(
    color: Color,
    onColorChanged: (Color) -> Unit,
    modifier: Modifier = Modifier,
    mode: MeowColorPaletteMode = MeowColorPaletteMode.Sliders,
    colorSpace: MeowColorPaletteSpace = MeowColorPaletteSpace.Hsv,
    showPreview: Boolean = true,
) {
    when (mode) {
        MeowColorPaletteMode.Sliders -> MiuixColorPicker(
            color = color,
            onColorChanged = onColorChanged,
            modifier = modifier,
            showPreview = showPreview,
            colorSpace = when (colorSpace) {
                MeowColorPaletteSpace.Hsv -> MiuixColorSpace.HSV
                MeowColorPaletteSpace.OkHsv -> MiuixColorSpace.OKHSV
                MeowColorPaletteSpace.OkLab -> MiuixColorSpace.OKLAB
                MeowColorPaletteSpace.OkLch -> MiuixColorSpace.OKLCH
            },
        )

        MeowColorPaletteMode.Grid -> MiuixColorPalette(
            color = color,
            onColorChanged = onColorChanged,
            modifier = modifier,
            showPreview = showPreview,
        )
    }
}

/**
 * 调色盘弹窗：以风格原生的窗口承载 [MeowColorPalette]，确认后一次性提交。
 *
 * @param show 是否显示窗口。
 * @param initialColor 打开时的初始颜色。
 * @param onConfirm 点击确认时回调选中的颜色；[keepAlpha] 为 false 时透明度强制为 1。
 * @param onDismissRequest 请求关闭窗口（取消或点击外部）。
 * @param keepAlpha 是否保留透明度分量；作种子色等用途时应保持默认 false。
 */
@Composable
fun MeowColorPaletteDialog(
    show: Boolean,
    initialColor: Color,
    onConfirm: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    title: String = "Custom color",
    confirmText: String = "Done",
    cancelText: String = "Cancel",
    mode: MeowColorPaletteMode = MeowColorPaletteMode.Sliders,
    colorSpace: MeowColorPaletteSpace = MeowColorPaletteSpace.Hsv,
    keepAlpha: Boolean = false,
) {
    var draft by remember(show) { mutableStateOf(initialColor) }
    val confirm = {
        onConfirm(if (keepAlpha) draft else draft.copy(alpha = 1f))
    }

    MeowStyleContent(
        materialExpressive = {
            if (show) {
                AlertDialog(
                    onDismissRequest = onDismissRequest,
                    title = { MaterialText(title) },
                    text = {
                        MeowColorPalette(
                            color = draft,
                            onColorChanged = { draft = it },
                            mode = mode,
                            colorSpace = colorSpace,
                        )
                    },
                    confirmButton = {
                        MaterialTextButton(onClick = confirm) {
                            MaterialText(confirmText)
                        }
                    },
                    dismissButton = {
                        MaterialTextButton(onClick = onDismissRequest) {
                            MaterialText(cancelText)
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
                    MeowColorPalette(
                        color = draft,
                        onColorChanged = { draft = it },
                        mode = mode,
                        colorSpace = colorSpace,
                    )
                    Spacer(Modifier.height(20.dp))
                    MiuixDialogButtons(
                        confirmText = confirmText,
                        cancelText = cancelText,
                        onConfirm = confirm,
                        onCancel = onDismissRequest,
                    )
                }
            }
        },
    )
}
