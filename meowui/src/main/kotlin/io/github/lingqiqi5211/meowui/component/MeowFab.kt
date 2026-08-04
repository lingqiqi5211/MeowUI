package io.github.lingqiqi5211.meowui.component

import androidx.compose.material3.FloatingActionButton as MaterialFloatingActionButton
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.basic.FloatingActionButton as MiuixFloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon

/**
 * 主操作悬浮按钮。
 *
 * 只收一枚图标和它的无障碍文案：两套风格的悬浮按钮都是圆的，塞文字要么被截断、要么把按钮撑成
 * 另一种东西。所以叫得出名字的那句话走 [contentDescription]，而不是一段会被丢掉的标签。
 *
 * 容器色与内容色**成对**声明。Material 只在容器保持默认时才自己推导内容色，而这里的容器取的是
 * 强调色——曾经因此画出过深色图标压在深色强调圆上的按钮。
 */
@Composable
fun MeowFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialFloatingActionButton(
                onClick = onClick,
                modifier = modifier,
                containerColor = MeowTheme.colors.primary,
                contentColor = MeowTheme.colors.onPrimary,
            ) {
                MaterialIcon(imageVector = icon, contentDescription = contentDescription)
            }
        },
        miuix = {
            MiuixFloatingActionButton(onClick = onClick, modifier = modifier) {
                MiuixIcon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = MeowTheme.colors.onPrimary,
                )
            }
        },
    )
}
