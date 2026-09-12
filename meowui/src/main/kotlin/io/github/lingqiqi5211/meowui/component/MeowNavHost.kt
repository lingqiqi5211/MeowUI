package io.github.lingqiqi5211.meowui.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.nav.core.NavDisplay as MiuixNavDisplay
import top.yukonga.miuix.kmp.nav.core.NavEntryBuilder as MiuixNavEntryBuilder
import top.yukonga.miuix.kmp.nav.core.NavKey as MiuixNavKey
import top.yukonga.miuix.kmp.nav.core.navBackStackOf as miuixNavBackStackOf
import top.yukonga.miuix.kmp.nav.transition.NavMotion as MiuixNavMotion
import top.yukonga.miuix.kmp.nav.transition.NavSettlePhase as MiuixNavSettlePhase
import top.yukonga.miuix.kmp.nav.transition.NavSettleSpec as MiuixNavSettleSpec
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection as MiuixNavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransition as MiuixNavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope as MiuixNavTransitionScope
import top.yukonga.miuix.kmp.nav.transition.NavTransitions as MiuixNavTransitions
import top.yukonga.miuix.kmp.nav.transition.navDirectionalTransition as miuixNavDirectionalTransition
import top.yukonga.miuix.kmp.nav.transition.navGraphicsTransition as miuixNavGraphicsTransition

/**
 * 页面栈宿主：按当前风格渲染 [backStack] 栈顶的页面，并接管页面间的转场与返回。
 *
 * 返回栈由调用侧持有——一个普通的 `List`，入栈/出栈就是换一个列表传进来。宿主负责的
 * 是下游最难自己做对的三件事：
 *
 * - **转场**：经典 activity 式推入/弹出（KernelSU / InstallerX 的观感）——新页面全宽
 *   滑入，旧页面向左视差约 1/4 宽并轻微压暗；弹出反向。每页自带当前风格的页面底色，
 *   层叠期间不会互相透出。
 * - **预测式返回**：手势直接拖拽弹出转场的进度，松手提交则顺势播完，取消则弹回。
 *   [predictiveBackEnabled] 为 false 时手势不再驱动画面，返回改为定时播完（见
 *   [navNoPredictiveTransition]）。
 * - **方向与层级**：入栈时新页在上层滑入，出栈时旧页在上层滑走，由栈深度决定，
 *   调用侧不用再传 `isForward`。
 *
 * 实现是 miuix-nav 的 `NavDisplay`——两套风格共用同一套导航，只是各带一条
 * `NavTransition`。
 *
 * [onBack] 为 null 时宿主完全不注册返回处理，只做转场——用于返回已由更外层统一处理
 * 的页面（例如编辑器有自己的脏状态确认逻辑），或该宿主当前不在前台（如寄宿在 pager 的
 * 后台页里）。
 *
 * **同一个页面对象不能在栈里出现两次**：`NavDisplay` 用它作为 saveable 状态与
 * reconcile 的身份，重复会在 reconcile 时报错。
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
    val routes = backStack.map { page -> MeowNavRoute(page) }
    // 首帧就带上完整的栈，否则第一次组合会空一帧。
    val navBackStack = remember { miuixNavBackStackOf(*routes.toTypedArray()) }
    if (navBackStack != routes) {
        // 同步放在 SideEffect 里而不是组合期直接写，避免同一趟组合里先写后读的回写；
        // 代价是入栈/出栈的转场晚一帧开始。
        SideEffect {
            navBackStack.clear()
            navBackStack.addAll(routes)
        }
    }
    val pageColor = navPageColor()
    val currentContent by rememberUpdatedState(content)
    val currentPageColor by rememberUpdatedState(pageColor)
    val currentOnBack by rememberUpdatedState(onBack)
    // NavDisplay 会按 content lambda 的身份 remember 出 entryProvider，所以这里必须给一个
    // 稳定的 lambda：页面内容经由上面的 State 读取，不直接捕获本次组合的形参。
    val entries: MiuixNavEntryBuilder.() -> Unit = remember {
        {
            entry<MeowNavRoute> { route ->
                // 层叠期间上层页面必须不透明，否则视差中的下层页会透出来。
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(currentPageColor),
                ) {
                    @Suppress("UNCHECKED_CAST")
                    currentContent(route.value as T)
                }
            }
        }
    }
    val base = when (MeowTheme.style) {
        MeowUiStyle.MaterialExpressive -> MaterialNavTransition
        MeowUiStyle.Miuix -> MiuixNavTransitions.MiuixDefault
    }
    val transition = remember(base, predictiveBackEnabled) {
        if (predictiveBackEnabled) base else navNoPredictiveTransition(base)
    }
    val display = @Composable {
        MiuixNavDisplay(
            backStack = navBackStack,
            modifier = modifier,
            onBack = { currentOnBack?.invoke() },
            transition = transition,
            content = entries,
        )
    }
    val activeOwner = LocalNavigationEventDispatcherOwner.current
    val inert = rememberInertNavigationEventDispatcherOwner()
    // 同一组合位置保留 NavDisplay：后台页重新获得返回处理权时不重建页面与 remember 状态。
    // 无 onBack 时使用孤立分发器，让系统返回事件交给外层处理器。
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides if (onBack == null) {
            inert
        } else {
            checkNotNull(activeOwner) { "MeowNavHost requires a NavigationEventDispatcherOwner" }
        },
    ) {
        display()
    }
}

/**
 * 一条不接任何输入的分发器：宿主不该接管返回时，把它喂给 NavDisplay。
 *
 * 它没有父级，所以既收不到系统返回，也不会把事件从真实分发器那里截走。
 */
@Composable
private fun rememberInertNavigationEventDispatcherOwner(): NavigationEventDispatcherOwner {
    val owner = remember {
        object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher = NavigationEventDispatcher()
        }
    }
    DisposableEffect(owner) {
        onDispose {
            try {
                owner.navigationEventDispatcher.dispose()
            } catch (_: IllegalStateException) {
                // 子分发器的级联释放可能已经带走了它。
            }
        }
    }
    return owner
}

/** 把调用侧的任意页面对象包成 `NavDisplay` 认得的路由。 */
private data class MeowNavRoute(val value: Any) : MiuixNavKey

/**
 * 页面底色：取当前风格的 scaffold 页面色，与 MeowScaffold/MeowPreferenceScreen 的背景
 * 无缝衔接。
 */
@Composable
private fun navPageColor(): Color = MeowTheme.colors.page

// 快出极缓入：前段快速让位、尾段长收敛，是各家 activity 转场共用的曲线形状。
private val NavEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

// 视差中被盖住的页面压暗到的透明度。
private const val CoveredAlpha = 0.75f

// 被盖住的页面向前视差的宽度比例。
private const val CoveredParallaxFraction = 0.25f

private const val MaterialNavDurationMillis = 350

// 关掉预测式返回后，提交/取消各自定时播完的时长。
private const val NoPredictiveMillis = 350

/**
 * Material 分支的转场：几何上和 miuix 那条一致（新页全宽滑入、被盖住的页向前视差 1/4 宽），
 * 差别在压暗幅度和时长——沿用本组件原来自绘时的 [CoveredAlpha] 与 [NavEasing]，换实现不换观感。
 *
 * 手势提交/取消走 [MiuixNavMotion] 的默认弹簧：那两段要跟手，续速度的弹簧比定时曲线合适。
 */
private val MaterialNavTransition: MiuixNavTransition = miuixNavGraphicsTransition(
    motion = MiuixNavMotion(
        programmatic = MiuixNavSettleSpec.Tween(
            durationMillis = MaterialNavDurationMillis,
            easing = NavEasing,
        ),
    ),
) { scope ->
    val width = scope.layoutSize.width.toFloat()
    val depth = scope.relativeDepth
    val rtl = scope.layoutDirection == LayoutDirection.Rtl
    if (depth <= 0f) {
        translationX = (if (rtl) -1f else 1f) * (-depth).coerceIn(0f, 1f) * width
    } else {
        val covered = depth.coerceIn(0f, 1f)
        translationX = (if (rtl) 1f else -1f) * covered * width * CoveredParallaxFraction
        alpha = 1f - (1f - CoveredAlpha) * covered
    }
}

/**
 * 关掉预测式返回：手势不再驱动画面。
 *
 * `NavDisplay` 的返回处理没有开关，手势会话照常发生；能改的是**画面怎么响应**。所以这里
 * 包一层：手势按住期间把深度冻结在抓取那一刻（画面不动），松手提交/取消时再从冻结处定时
 * 播到终点。观感等同于普通返回键，同时保留了 NavDisplay 的层级、状态与生命周期管理。
 *
 * 思路参考 InstallerX-Revived 的做法（它同样用 `navDirectionalTransition` 的 `predictivePop`
 * 分支接管手势期的画面），实现为独立编写。
 */
private fun navNoPredictiveTransition(base: MiuixNavTransition): MiuixNavTransition =
    miuixNavDirectionalTransition(
        push = base,
        pop = base,
        predictivePop = object : MiuixNavTransition {
            override val opaqueDepth: Float = base.opaqueDepth

            // 手势不驱动画面，就没有可跟手的滑动关闭。
            override val dismissDirection: MiuixNavSwipeDirection = MiuixNavSwipeDirection.None

            override val motion: MiuixNavMotion = MiuixNavMotion(
                commit = MiuixNavSettleSpec.Tween(NoPredictiveMillis, NavEasing),
                cancel = MiuixNavSettleSpec.Tween(NoPredictiveMillis, NavEasing),
            )

            override fun scrimFraction(scope: MiuixNavTransitionScope): Float =
                base.scrimFraction(scope.withFrozenGestureDepth())

            override fun Modifier.transformEntry(scope: MiuixNavTransitionScope): Modifier {
                val outer = this
                return with(base) { outer.transformEntry(scope.withFrozenGestureDepth()) }
            }
        },
    )

/**
 * 把手势带来的那部分深度变化抵消掉。
 *
 * 手指拖动时 `relativeDepth` 会随 `gesture.progress` 线性偏移，冻结即把它加回去。松手后
 * settle 阶段再按已播时长从冻结值插值到真实深度——起点等于冻结值，所以提交那一刻不会跳。
 */
private fun MiuixNavTransitionScope.withFrozenGestureDepth(): MiuixNavTransitionScope {
    val gesture = gesture ?: return this
    val frozen = relativeDepth + gesture.progress
    val settle = settle
    val depth = if (settle == null || settle.phase == MiuixNavSettlePhase.Programmatic) {
        frozen
    } else {
        val played = (settle.elapsedMillis / NoPredictiveMillis).coerceIn(0f, 1f)
        val eased = NavEasing.transform(played)
        frozen + (relativeDepth - frozen) * eased
    }
    return FrozenDepthScope(this, depth)
}

private class FrozenDepthScope(
    delegate: MiuixNavTransitionScope,
    override val relativeDepth: Float,
) : MiuixNavTransitionScope by delegate
