package io.github.lingqiqi5211.meowui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * 与导航栏配套的分页容器：左右滑动切 tab。
 *
 * 与直接用 `HorizontalPager` 的区别在手势规范（见 [MeowNavigationPagerDefaults]）和跨页跳转的
 * 淡入淡出。页码与选中项都归 [selection]，别自己读 [PagerState.currentPage]。
 *
 * [keepPagesAlive] 默认把所有页都留在组合里：tab 之间来回切是常事，每次重建列表既慢又丢滚动位置。
 * 页很多或者每页都很重时关掉它。
 */
@Composable
fun MeowNavigationPager(
    selection: MeowNavigationSelection,
    modifier: Modifier = Modifier,
    keepPagesAlive: Boolean = true,
    content: @Composable (Int) -> Unit,
) {
    val state = selection.pagerState
    val stateHolder = rememberSaveableStateHolder()
    HorizontalPager(
        state = state,
        // 跨页跳转的淡入淡出。在绘制期读，动画帧不触发重组。
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = selection.jumpAlpha.value },
        beyondViewportPageCount = if (keepPagesAlive) (state.pageCount - 1).coerceAtLeast(0) else 0,
        flingBehavior = MeowNavigationPagerDefaults.flingBehavior(state),
    ) { page ->
        stateHolder.SaveableStateProvider(page) { content(page) }
    }
}

/** [MeowNavigationPager] 的手势规范。 */
object MeowNavigationPagerDefaults {
    /**
     * 手势要划过这么多页宽才翻页，慢速拖不到就弹回原页。
     *
     * Compose 的默认值是半页。列表页上手指稍微带点横向就换了 tab，所以往上提。
     */
    const val SnapThreshold = 0.65f

    /**
     * 一次甩动最多翻一页，与 Compose 默认的 PagerSnapDistance.atMost(1) 一致。
     *
     * 管不到手指按着一路拖过好几页：那是直接操纵，中间页本来就该跟着手走。
     */
    @Composable
    fun flingBehavior(state: PagerState): TargetedFlingBehavior = PagerDefaults.flingBehavior(
        state = state,
        pagerSnapDistance = SnapDistance,
        snapAnimationSpec = SnapSpec,
        snapPositionalThreshold = SnapThreshold,
    )

    // 两个都是无状态的规格对象，提到外面按常量存，省得每趟重组各新建一个。
    private val SnapDistance = PagerSnapDistance.atMost(1)

    private val SnapSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
}

/**
 * 导航栏该高亮第几项，以及点导航栏怎么切页。
 *
 * 不要把 [PagerState.currentPage] 直接喂给导航栏：跨页时它会在中间那页停一下，
 * 胶囊会被拉回去再弹出来。这里点的时候先把选中项挪到目标，滚动停下来之后才回读。
 */
@Stable
class MeowNavigationSelection internal constructor(
    internal val pagerState: PagerState,
    private val scope: CoroutineScope,
) {
    var index: Int by mutableIntStateOf(pagerState.currentPage)
        internal set

    /** 跨页跳转期间的整页不透明度，[MeowNavigationPager] 在绘制期读它。 */
    internal val jumpAlpha = Animatable(1f)

    internal var selecting by mutableStateOf(false)
        private set
    private var selectionJob: Job? = null
    private var selectionVersion = 0

    /**
     * 切到第 [target] 页。导航栏立刻显示为选中。
     *
     * 相邻的一页滑过去。隔着页的不滑：一次动画里把中间几页全播一遍，既晃眼又看不清自己到了
     * 哪。改成淡出、瞬移、淡入——中间页始终不露面，又不是硬切。
     */
    fun select(target: Int) {
        if (target !in 0 until pagerState.pageCount) return
        val version = ++selectionVersion
        val previousJob = selectionJob
        previousJob?.cancel()
        selecting = true
        index = target
        selectionJob = scope.launch {
            try {
                previousJob?.join()
                jumpAlpha.snapTo(1f)
                if (abs(target - pagerState.currentPage) <= 1) {
                    pagerState.slideToAdjacent(target)
                } else {
                    jumpAlpha.animateTo(0f, tween(JumpFadeOutMillis, easing = EaseInOut))
                    pagerState.scrollToPage(target)
                    jumpAlpha.animateTo(1f, tween(JumpFadeInMillis, easing = EaseInOut))
                }
            } finally {
                if (version == selectionVersion) {
                    withContext(NonCancellable) { jumpAlpha.snapTo(1f) }
                    selecting = false
                }
            }
        }
    }
}

/**
 * 记住一份 [MeowNavigationSelection]。
 *
 * [onSettled] 在页面真正停在新的一页时回调一次，手滑过去和点导航栏都算；停在原页不回调。
 */
@Composable
fun rememberMeowNavigationSelection(
    state: PagerState,
    onSettled: (Int) -> Unit = {},
): MeowNavigationSelection {
    val scope = rememberCoroutineScope()
    val selection = remember(state, scope) { MeowNavigationSelection(state, scope) }
    val currentOnSettled by rememberUpdatedState(onSettled)
    LaunchedEffect(state, selection) {
        var settled = state.currentPage
        snapshotFlow { (selection.selecting || state.isScrollInProgress) to state.currentPage }
            .collect { (scrolling, page) ->
                if (scrolling) return@collect
                selection.index = page
                if (page != settled) {
                    settled = page
                    currentOnSettled(page)
                }
            }
    }
    return selection
}

/**
 * 滑到相邻的一页。
 *
 * 按像素滚而不是 animateScrollToPage：手指划过去和点导航栏走的是同一段距离，观感一致。
 */
private suspend fun PagerState.slideToAdjacent(target: Int) {
    val pageSize = layoutInfo.pageSize
    if (pageSize <= 0 || target == currentPage) {
        scrollToPage(target)
        return
    }
    val from = currentPage + currentPageOffsetFraction
    animateScrollBy(
        value = (target - from) * pageSize,
        animationSpec = tween(SlideMillis, easing = EaseInOut),
    )
    scrollToPage(target)
}

private const val SlideMillis = 200

private const val JumpFadeOutMillis = 110

private const val JumpFadeInMillis = 170
