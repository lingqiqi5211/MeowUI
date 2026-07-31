package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.theme.LocalContentColor as MiuixLocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 内容卡片，KernelSU 首页的状态卡与信息卡即为此形态。
 *
 * Material 分支是 `surfaceBright` 圆角 Surface，Miuix 分支是原生 Card。
 *
 * [containerColor] 与 [contentColor] 用于容器色由运行状态决定的卡片（正常/警告/错误
 * 三种色调的状态卡）：内容色会作为 `LocalContentColor` 提供，卡内文字与图标默认就是
 * 可读的对比色。
 *
 * [index] 与 [count] 让同一组相邻的卡片在 Material 分支下拼成一张分组卡片——组内第一
 * 张上圆角大、中间的圆角小、最后一张下圆角大，也就是 Expressive 的分段列表形状，并自动
 * 在项间留出标准缝隙。数据列表（条目数由数据决定、还要放进 `LazyColumn`）走不了
 * [MeowPreferenceSection] 那条编译期收集的 DSL，用这两个参数即可；默认的 `0 to 1` 就是
 * 一张独立卡片。Miuix 分支不做拼接，每项仍是一张独立卡片，那是 miuix 列表本来的样子。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MeowCard(
    modifier: Modifier = Modifier,
    index: Int = 0,
    count: Int = 1,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    // 组内非末项在下方留缝，调用侧因此不需要知道两种风格各自的间距。
    val spacing = if (index < count - 1) {
        if (MeowTheme.style == MeowUiStyle.MaterialExpressive) {
            ListItemDefaults.SegmentedGap
        } else {
            MiuixListGap
        }
    } else {
        0.dp
    }
    val outerModifier = modifier
        .fillMaxWidth()
        .padding(bottom = spacing)

    MeowStyleContent(
        materialExpressive = {
            val color = containerColor.takeOrElse { MaterialTheme.colorScheme.surfaceBright }
            val resolvedContentColor =
                contentColor.takeOrElse { MaterialTheme.colorScheme.onSurface }
            val shape = if (count == 1) {
                MaterialTheme.shapes.extraLarge
            } else {
                ListItemDefaults.segmentedShapes(index, count).shape
            }
            if (onClick != null) {
                MaterialSurface(
                    onClick = onClick,
                    modifier = outerModifier,
                    shape = shape,
                    color = color,
                    contentColor = resolvedContentColor,
                ) {
                    Column(modifier = Modifier.padding(contentPadding), content = content)
                }
            } else {
                MaterialSurface(
                    modifier = outerModifier,
                    shape = shape,
                    color = color,
                    contentColor = resolvedContentColor,
                ) {
                    Column(modifier = Modifier.padding(contentPadding), content = content)
                }
            }
        },
        miuix = {
            val colors = if (containerColor == Color.Unspecified) {
                MiuixCardDefaults.defaultColors()
            } else {
                MiuixCardDefaults.defaultColors(color = containerColor)
            }
            val resolvedContentColor =
                contentColor.takeOrElse { MiuixTheme.colorScheme.onSurface }
            MiuixCard(
                modifier = outerModifier,
                insideMargin = contentPadding,
                colors = colors,
                onClick = onClick,
            ) {
                // 给定容器色时 miuix Card 不会跟着换内容色,状态卡的错误/警告色调下
                // 文字会不可读,所以内容色一并提供下去。
                CompositionLocalProvider(
                    MiuixLocalContentColor provides resolvedContentColor,
                    MaterialLocalContentColor provides resolvedContentColor,
                ) {
                    Column(content = content)
                }
            }
        },
    )
}

private val MiuixListGap = 8.dp
