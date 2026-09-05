package io.github.lingqiqi5211.meowui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import top.yukonga.miuix.kmp.basic.BreadcrumbBar as MiuixBreadcrumbBar
import top.yukonga.miuix.kmp.basic.BreadcrumbItem as MiuixBreadcrumbItem

/**
 * 面包屑中的一段。[path] 是路径片段，[text] 是显示文案（为空时显示 [path]）。
 */
@Stable
data class MeowBreadcrumbItem(
    val path: String,
    val text: String? = null,
)

/**
 * 横向的路径导航条：把每一段路径画成胶囊，段与段之间用箭头分隔，超出宽度时横向滚动
 * 而不是折叠——文件管理器在移动端的通行做法。
 *
 * [highlightIndex] 与 [items] 解耦：调用方可以展示完整路径而高亮其中任意一段（当前
 * 目录，或用户回退到的某个父级）。传负数（例如 `-1`）同时关闭高亮与自动滚动。
 *
 * Miuix 分支直接用 miuix 的 `BreadcrumbBar`；Material 分支按同样的行为用 M3 Expressive
 * 的配色与形状画一套，两边的高亮段都会在变化时自动滚到可视区域中间。
 *
 * @param items 要显示的路径段。
 * @param onItemClick 点击某一段时回调其下标。
 * @param highlightIndex 高亮段的下标，默认最后一段。
 * @param enabled 是否可点。置 false 时用禁用配色，但仍可横向滚动。
 * @param itemMaxWidth 单段的最大宽度，超出部分截断。
 * @param contentPadding 条内边距。默认贴合两套风格自己的列表留白；紧贴在顶栏下面时
 *   可以调小纵向值，免得和顶栏之间空出一条。
 * @param edgeFadeWidth 两端渐隐的宽度。想让渐隐正好收在第一段胶囊的左缘，就把它设成与
 *   [contentPadding] 的水平值相同。
 * @param edgeFadeInset 渐隐透明那一端离屏幕边缘的距离。默认贴边（0dp）；给一个值就让内容在
 *   离边缘这么远的地方就已经完全消失，边上留出一条干净的空白。
 *
 * 路径长到要横向滚动时，两侧被截断的那一边会渐隐——上游 miuix 是硬切，这里两套风格都
 * 补上，让「还有内容」这件事看得出来。
 *
 * 层级变化（进下一级 / 回上一级）时整条做一次转场：按方向滑入滑出，免得胶囊在原地凭空
 * 多一段或少一段。
 */
@Composable
fun MeowBreadcrumbBar(
    items: List<MeowBreadcrumbItem>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightIndex: Int = items.lastIndex,
    enabled: Boolean = true,
    itemMaxWidth: Dp = MeowBreadcrumbBarDefaults.ItemMaxWidth,
    contentPadding: PaddingValues = MeowBreadcrumbBarDefaults.ContentPadding,
    edgeFadeWidth: Dp = MeowBreadcrumbBarDefaults.EdgeFadeWidth,
    edgeFadeInset: Dp = 0.dp,
) {
    // 滚动状态由外面持有：渐隐要知道两边还剩多少没露出来，两套风格共用同一份。
    val scrollState = rememberScrollState()
    Box(modifier = modifier.breadcrumbEdgeFade(scrollState, edgeFadeWidth, edgeFadeInset)) {
        MeowStyleContent(
            materialExpressive = {
                MaterialBreadcrumbBar(
                    items = items,
                    onItemClick = onItemClick,
                    scrollState = scrollState,
                    highlightIndex = highlightIndex,
                    enabled = enabled,
                    itemMaxWidth = itemMaxWidth,
                    contentPadding = contentPadding,
                )
            },
            miuix = {
                val miuixItems = remember(items) {
                    items.map { item -> MiuixBreadcrumbItem(item.path, item.text) }
                }
                MiuixBreadcrumbBar(
                    items = miuixItems,
                    onItemClick = onItemClick,
                    highlightIndex = highlightIndex,
                    enabled = enabled,
                    itemMaxWidth = itemMaxWidth,
                    insideMargin = contentPadding,
                    scrollState = scrollState,
                )
            },
        )
    }
}

/**
 * 被截断的那一侧渐隐。
 *
 * 只在该侧确实还有没露出来的内容时才画，所以路径短到不用滚动时两边都是干净的。用
 * `DstIn` 抠掉 alpha 而不是盖一层背景色——面包屑底下是什么颜色由调用侧决定，盖色会穿帮。
 */
private fun Modifier.breadcrumbEdgeFade(
    scrollState: ScrollState,
    fadeWidth: Dp,
    inset: Dp,
): Modifier = this
    // DstIn 要在离屏层上做，否则会把底下已经画好的东西一起抠掉。
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val fade = fadeWidth.toPx().coerceAtMost(size.width / 2f)
        if (fade <= 0f) return@drawWithContent
        val gap = inset.toPx().coerceAtMost(size.width / 2f - fade).coerceAtLeast(0f)
        if (scrollState.value > 0) {
            // 先把最边上那条整段抹掉：渐隐的透明端因此落在离屏幕边缘 [inset] 的位置，
            // 而不是贴着边。
            if (gap > 0f) {
                drawRect(
                    color = Color.Transparent,
                    size = Size(gap, size.height),
                    blendMode = BlendMode.DstIn,
                )
            }
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = gap,
                    endX = gap + fade,
                ),
                topLeft = Offset(gap, 0f),
                size = Size(fade, size.height),
                blendMode = BlendMode.DstIn,
            )
        }
        if (scrollState.value < scrollState.maxValue) {
            if (gap > 0f) {
                drawRect(
                    color = Color.Transparent,
                    topLeft = Offset(size.width - gap, 0f),
                    size = Size(gap, size.height),
                    blendMode = BlendMode.DstIn,
                )
            }
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = size.width - gap - fade,
                    endX = size.width - gap,
                ),
                topLeft = Offset(size.width - gap - fade, 0f),
                size = Size(fade, size.height),
                blendMode = BlendMode.DstIn,
            )
        }
    }

object MeowBreadcrumbBarDefaults {

    /** 单段胶囊的最大宽度。 */
    val ItemMaxWidth: Dp = 160.dp

    /** 单段胶囊的高度。 */
    val ItemHeight: Dp = 32.dp

    /** 条内边距，与 miuix `BreadcrumbBarDefaults.InsideMargin` 一致。 */
    val ContentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp)

    /**
     * 可滚动时两端渐隐的宽度。比调用侧的页边距略窄：胶囊静止时的边缘与正文左缘对齐，渐隐
     * 在它左边几 dp 就收完，滚动时那一段是淡出而不是硬切。
     */
    val EdgeFadeWidth: Dp = 1.dp

    /**
     * 单段胶囊自己的内边距，与 miuix `BreadcrumbBarDefaults.ItemHorizontalPadding` 一致。
     * 只作用于 Material 分支——Miuix 分支的这一档写在上游，改不动。
     *
     * 调用侧要让第一段的**文字**与正文左缘对齐时，把条的 `contentPadding` 减掉这里的水平值。
     */
    val ItemPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp)

}

@Composable
private fun MaterialBreadcrumbBar(
    items: List<MeowBreadcrumbItem>,
    onItemClick: (Int) -> Unit,
    scrollState: ScrollState,
    highlightIndex: Int,
    enabled: Boolean,
    itemMaxWidth: Dp,
    contentPadding: PaddingValues,
) {
    val hasHighlight = highlightIndex >= 0
    // The highlighted segment is centred on first layout and whenever it moves. Its
    // position is captured relative to the scrolling row, so it stays valid however
    // far the row has already been scrolled.
    var highlightX by remember { mutableFloatStateOf(0f) }
    var highlightWidth by remember { mutableFloatStateOf(0f) }
    var positioned by remember { mutableStateOf(false) }

    LaunchedEffect(highlightIndex, positioned) {
        if (!hasHighlight || !positioned || highlightWidth <= 0f) return@LaunchedEffect
        val viewport = scrollState.viewportSize.toFloat()
        if (viewport <= 0f) return@LaunchedEffect
        val target = (highlightX - (viewport - highlightWidth) / 2f)
            .coerceIn(0f, scrollState.maxValue.toFloat())
        scrollState.animateScrollTo(target.toInt())
    }

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            // 宽度跟着层级变化平滑收放。整条滑动的转场看着别扭：列表在手势里已经跟着
            // 手指走，而路径只在提交那一刻才换，一滑就晚一拍，还和「高亮段自动滚到中间」
            // 抢同一个方向。只让受影响的那一段自己动，其余原地不动，才是文件管理器的样子。
            .animateContentSize(tween(LevelChangeMillis, easing = LevelChangeEasing))
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            key(item.path, index) {
                if (index > 0) {
                    MaterialIcon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 2.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (enabled) 1f else DisabledAlpha,
                        ),
                    )
                }
                val highlighted = hasHighlight && index == highlightIndex
                // 新加入的那一段自己展开淡入，已有的段不重播（visibleState 只在首次组合时
                // 从 false 走到 true）。回上一级时末段直接消失，整条的宽度由
                // animateContentSize 收回——试过给末段做收缩淡出，观感反而更别扭。
                val appearance = remember { MutableTransitionState(false) }
                appearance.targetState = true
                AnimatedVisibility(
                    visibleState = appearance,
                    enter = fadeIn(tween(LevelChangeMillis, easing = LevelChangeEasing)) +
                        expandHorizontally(
                            animationSpec = tween(LevelChangeMillis, easing = LevelChangeEasing),
                            expandFrom = Alignment.Start,
                        ),
                    exit = ExitTransition.None,
                ) {
                    MaterialBreadcrumbSegment(
                        text = item.text ?: item.path,
                        highlighted = highlighted,
                        enabled = enabled,
                        itemMaxWidth = itemMaxWidth,
                        onPositioned = if (highlighted) {
                            { x, width ->
                                highlightX = x
                                highlightWidth = width
                                if (!positioned) positioned = true
                            }
                        } else {
                            null
                        },
                        onClick = { onItemClick(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialBreadcrumbSegment(
    text: String,
    highlighted: Boolean,
    enabled: Boolean,
    itemMaxWidth: Dp,
    onPositioned: ((x: Float, width: Float) -> Unit)?,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = when {
        !enabled -> colorScheme.surfaceContainerHighest.copy(alpha = DisabledAlpha)
        highlighted -> colorScheme.secondaryContainer
        else -> colorScheme.surfaceContainerHighest
    }
    val content = when {
        !enabled -> colorScheme.onSurfaceVariant.copy(alpha = DisabledAlpha)
        highlighted -> colorScheme.onSecondaryContainer
        else -> colorScheme.onSurfaceVariant
    }
    MaterialText(
        text = text,
        modifier = Modifier
            .then(
                if (onPositioned == null) {
                    Modifier
                } else {
                    Modifier.onGloballyPositioned { coordinates ->
                        onPositioned(
                            coordinates.positionInParent().x,
                            coordinates.size.width.toFloat(),
                        )
                    }
                },
            )
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = MeowBreadcrumbBarDefaults.ItemHeight)
            .widthIn(max = itemMaxWidth)
            .padding(MeowBreadcrumbBarDefaults.ItemPadding),
        color = content,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (highlighted) FontWeight.Medium else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Matches the alpha Material uses for disabled content. */
private const val DisabledAlpha = 0.38f

/** 把面包屑重新拼成一条路径。 */
fun List<MeowBreadcrumbItem>.joinToPath(separator: String = "/"): String =
    joinToString(separator = separator) { item -> item.path }

// 层级变化的转场：比列表内容的更替短，这只是一条导航条在换内容。
private const val LevelChangeMillis = 200
private val LevelChangeEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
