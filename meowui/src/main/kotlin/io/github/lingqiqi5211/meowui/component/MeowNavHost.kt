package io.github.lingqiqi5211.meowui.component

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import androidx.compose.runtime.snapshotFlow
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 页面栈宿主：按当前风格渲染 [backStack] 栈顶的页面，并接管页面间的转场与返回。
 *
 * 返回栈由调用侧持有——一个普通的 `List`，入栈/出栈就是换一个列表传进来。宿主负责的
 * 是下游最难自己做对的三件事：
 *
 * - **转场**：经典 activity 式推入/弹出（KernelSU / InstallerX 的观感）——新页面全宽
 *   滑入，旧页面向左视差约 1/4 宽并轻微压暗；弹出反向。没有尺寸动画，两页高度不同
 *   也不会斜着拉。每页自带当前风格的页面底色，层叠期间不会互相透出。
 * - **预测式返回**：手势直接拖拽弹出转场的进度（不是另做一套缩放预览），松手提交则
 *   顺势播完，取消则弹回。[predictiveBackEnabled] 为 false 时退化为普通返回键。
 * - **方向与层级**：入栈时新页在上层滑入，出栈时旧页在上层滑走，由栈深度决定，
 *   调用侧不用再传 `isForward`。
 *
 * [onBack] 为 null 时宿主完全不注册返回处理，只做转场——用于返回已由更外层统一处理
 * 的页面（例如编辑器有自己的脏状态确认逻辑），或该宿主当前不在前台（如寄宿在
 * pager 的后台页里）。
 */
@Composable
fun <T : Any> MeowNavHost(
    backStack: List<T>,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    predictiveBackEnabled: Boolean = true,
    content: @Composable (T) -> Unit,
) {
    require(backStack.isNotEmpty()) { "backStack must not be empty" }
    val top = backStack.last()

    // 方向：栈变短是弹出，其余（含同深度换栈顶）按推入处理。写在 remember 计算块里，
    // 保证 transitionSpec 在同一趟组合里就能读到新方向。
    var lastStack by remember { mutableStateOf(backStack) }
    var popDirection by remember { mutableStateOf(false) }
    if (lastStack != backStack) {
        popDirection = backStack.size < lastStack.size
        lastStack = backStack
    }

    val transitionState = remember { SeekableTransitionState(top) }
    LaunchedEffect(top) {
        if (transitionState.currentState != top || transitionState.targetState != top) {
            transitionState.animateTo(top)
        }
    }

    val currentOnBack by rememberUpdatedState(onBack)
    val currentStack by rememberUpdatedState(backStack)
    val previous = backStack.getOrNull(backStack.lastIndex - 1)
    val backAvailable = onBack != null && previous != null
    if (predictiveBackEnabled) {
        PredictiveBackHandler(enabled = backAvailable) { events ->
            val target = previous ?: return@PredictiveBackHandler
            try {
                popDirection = true
                events.collect { event ->
                    transitionState.seekTo(event.progress.coerceIn(0f, 1f), target)
                }
                val origin = currentStack.last()
                currentOnBack?.invoke()
                // 是否真正出栈由调用侧决定,而且出栈往往经由 ViewModel 异步落地。
                // 在已拖满的位置稍候栈的变化——变了自然由上面的 effect 播完;超时
                // 仍没变就视为返回被拦截（比如脏编辑器改弹确认框）,把页面弹回去。
                // 不能提交后立刻核对:异步出栈的状态还没落地,会被误判成拦截,
                // 页面先弹回再弹出,来回抽动。
                val popped = withTimeoutOrNull(VetoSettleTimeoutMillis) {
                    snapshotFlow { currentStack.lastOrNull() }.first { it != origin }
                } != null
                if (!popped) {
                    transitionState.animateTo(transitionState.currentState)
                }
            } catch (error: CancellationException) {
                // 手势取消：弹回当前页。集合抛出的取消并不终止本协程，动画完成后再抛。
                transitionState.animateTo(transitionState.currentState)
                throw error
            }
        }
    } else {
        BackHandler(enabled = backAvailable) { currentOnBack?.invoke() }
    }

    // 层叠期间上层页面必须不透明，否则视差中的下层页会透出来。页面底色取当前风格
    // 的 scaffold 页面色，与 MeowScaffold/MeowPreferenceScreen 的背景无缝衔接。
    val pageColor = when (MeowTheme.style) {
        MeowUiStyle.MaterialExpressive -> MaterialTheme.colorScheme.surfaceContainer
        // MiuixScaffold 的 containerColor 默认值;background 在浅色下更白,会跳色。
        MeowUiStyle.Miuix -> MiuixTheme.colorScheme.surface
    }
    val durationMillis = when (MeowTheme.style) {
        MeowUiStyle.MaterialExpressive -> 350
        MeowUiStyle.Miuix -> 380
    }

    // 被盖住的页面在转场结束后会离开组合;rememberSaveable 状态经由这里的 holder
    // 存续,弹回时滚动位置、pager 页等都还在原处。
    val stateHolder = rememberSaveableStateHolder()
    // 但保留只给"仍在栈里"的页面:已经弹出的页面把保存的状态一并丢弃,下次再进
    // 是一张新页(滚动回到顶部),而不是接着上次的位置。清理放在转场落定之后,
    // 退出中的页面此时才真正离开组合。
    val retainedKeys = remember { mutableSetOf<String>() }
    val transition = rememberTransition(transitionState, label = "MeowNavHost")
    if (transitionState.currentState == transitionState.targetState) {
        val liveKeys = backStack.map { it.toString() }
        SideEffect {
            val iterator = retainedKeys.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                if (key !in liveKeys) {
                    stateHolder.removeState(key)
                    iterator.remove()
                }
            }
        }
    }
    transition.AnimatedContent(
        modifier = modifier,
        transitionSpec = {
            val slide = tween<IntOffset>(durationMillis, easing = NavEasing)
            val fade = tween<Float>(durationMillis, easing = NavEasing)
            val transform = if (popDirection) {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(slide) { -it / 4 } +
                        fadeIn(fade, initialAlpha = CoveredAlpha),
                    initialContentExit = slideOutHorizontally(slide) { it },
                    sizeTransform = null,
                )
            } else {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(slide) { it },
                    initialContentExit = slideOutHorizontally(slide) { -it / 4 } +
                        fadeOut(fade, targetAlpha = CoveredAlpha),
                    sizeTransform = null,
                )
            }
            // 目标页的层级取它在栈里的深度：推入时新页更深所以在上层滑入，弹出时目标
            // 更浅所以旧页保持在上层滑走。
            transform.targetContentZIndex = currentStack.indexOf(targetState).toFloat()
            transform
        },
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageColor),
        ) {
            // 键用字符串而不是页面对象本身:SaveableStateProvider 要求键能存进
            // Bundle,而页面完全可以是普通的 data object/data class。枚举、data 类的
            // toString 稳定且互不相同,跨进程恢复也一致。
            val key = page.toString()
            SideEffect { retainedKeys += key }
            stateHolder.SaveableStateProvider(key = key) {
                content(page)
            }
        }
    }
}

// 快出极缓入：前段快速让位、尾段长收敛，是各家 activity 转场共用的曲线形状。
private val NavEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

// 视差中被盖住的页面压暗到的透明度。
private const val CoveredAlpha = 0.75f

// 提交预测式返回后等待调用侧真正出栈的时限,超时视为返回被拦截。
private const val VetoSettleTimeoutMillis = 350L
