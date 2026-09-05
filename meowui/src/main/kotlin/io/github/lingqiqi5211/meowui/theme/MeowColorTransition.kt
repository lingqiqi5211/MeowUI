package io.github.lingqiqi5211.meowui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * 让整套配色在切换时平滑过渡，而不是硬切。
 *
 * 只动一条进度：目标变了就从当前显示的那套插值到新目标。以前每个色 role 各自一条
 * `animateColorAsState`，Material 五十条、Miuix 五十多条，过渡期每帧要写几十个状态、
 * 跑几十个协程才凑出一帧；现在每帧一次状态写入、一次插值。
 *
 * 首帧直接取目标值，不会从错误颜色开始播放动画。[target] 的元素顺序必须固定，两次调用
 * 之间按下标一一对应。
 */
@Composable
internal fun rememberColorTransition(target: List<Color>): List<Color> {
    val transition = remember { ColorListTransition(target) }
    LaunchedEffect(target) { transition.animateTo(target) }
    return transition.current
}

@Stable
private class ColorListTransition(initial: List<Color>) {
    private var from: List<Color> = initial
    private var to: List<Color> = initial
    private val progress = Animatable(1f)

    val current: List<Color>
        get() {
            val fraction = progress.value
            if (fraction >= 1f) return to
            return List(to.size) { index -> lerp(from[index], to[index], fraction) }
        }

    suspend fun animateTo(target: List<Color>) {
        if (target == to) return
        // 从当前显示的颜色出发：切换途中再切一次也不会跳回旧起点。
        from = current
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, spring())
    }
}
