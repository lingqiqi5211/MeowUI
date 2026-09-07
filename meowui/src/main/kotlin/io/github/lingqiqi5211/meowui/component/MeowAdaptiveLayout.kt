package io.github.lingqiqi5211.meowui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.MeowTheme

enum class MeowCompactPane {
    List,
    Detail,
}

/**
 * 窗口宽度档位，取自 window size class。
 *
 * 两档不是同一件事：[Medium] 起宽度够放一列侧边导航栏，[Expanded] 起才够分成两栏。
 * 竖屏平板与展开的折叠机落在两者之间：底栏该换成侧栏，分栏则会把每栏都挤得太窄。
 */
object MeowWindowWidth {
    val Medium = 600.dp
    val Expanded = 840.dp
}

/**
 * 窗口高度档位。
 *
 * 分栏只看宽度是不够的：手机横过来也有八九百 dp 宽，但只有四百 dp 高，分两栏之后每栏
 * 都只剩几行。够高才分栏，横屏手机因此停在「侧栏、不分栏」这一档。
 */
object MeowWindowHeight {
    val Medium = 480.dp
}

@Composable
fun MeowAdaptiveLayout(
    compactPane: MeowCompactPane,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    expandedBreakpoint: Dp = MeowWindowWidth.Expanded,
    /** 分栏还要够高，见 [MeowWindowHeight]。 */
    expandedMinHeight: Dp = MeowWindowHeight.Medium,
    listPaneWidth: Dp = 360.dp,
    /**
     * 详情栏至少要留多宽。左栏放不下 [listPaneWidth] 时先让这一段，剩下的才归左栏。
     * 用下限而不是按比例封顶：左栏里除了列表还可能有侧边导航栏，那一列展开时按比例算会把列表挤扁。
     */
    detailPaneMinWidth: Dp = 360.dp,
    /**
     * 窄屏自己接管时用它，此时 [compactPane] 不参与。
     * 给调用侧留着带转场的导航容器：只切换 [listPane] 与 [detailPane] 是硬切，没有推入退出动画。
     *
     * 参数是当前可用宽度，窄屏这一侧还要再分档时读它。调用侧不要为此再套一层子组合：
     * 跨档那一帧新旧两份会同时活着，共用 key 的 SaveableStateProvider 会直接抛异常。
     */
    compactContent: (@Composable (Dp) -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        if (availableWidth >= expandedBreakpoint && maxHeight >= expandedMinHeight) {
            // 左栏宽度会变（侧边导航栏展开），所以走动画；值在布局期读，见 [animatedWidth]。
            val paneWidth = animateDpAsState(
                targetValue = listPaneWidth.coerceAtMost(
                    (availableWidth - detailPaneMinWidth).coerceAtLeast(0.dp),
                ),
                label = "meowListPaneWidth",
            )
            Row(modifier = Modifier.fillMaxSize()) {
                // 两栏都要裁：页面转场把上一页往旁边推并压暗，不裁的话那层压暗会画到另一栏上。
                Box(
                    modifier = Modifier
                        .animatedWidth(paneWidth)
                        .fillMaxHeight()
                        .clipToBounds(),
                ) {
                    listPane()
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MeowTheme.colors.divider),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clipToBounds(),
                ) {
                    detailPane()
                }
            }
        } else if (compactContent != null) {
            compactContent(availableWidth)
        } else {
            when (compactPane) {
                MeowCompactPane.List -> listPane()
                MeowCompactPane.Detail -> detailPane()
            }
        }
    }
}

/**
 * 宽度取自 [width]，且在布局期才读。
 *
 * 宽度动画不要在组合期读值：弹簧每一帧都会把读它的那棵子树重组一遍，分栏这种位置重组一次
 * 就是整页。这样写只有测量重跑。
 */
internal fun Modifier.animatedWidth(width: State<Dp>): Modifier = layout { measurable, constraints ->
    val target = width.value.roundToPx().coerceIn(constraints.minWidth, constraints.maxWidth)
    val placeable = measurable.measure(constraints.copy(minWidth = target, maxWidth = target))
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}
