package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.SnackbarHost as MaterialSnackbarHost
import androidx.compose.material3.SnackbarHostState as MaterialSnackbarHostState
import androidx.compose.material3.SnackbarResult as MaterialSnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.SnackbarHost as MiuixSnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState as MiuixSnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarResult as MiuixSnackbarResult

/** [MeowSnackbarState.show] 的结果。 */
enum class MeowSnackbarResult {
    /** Snackbar 超时或被用户滑走/关闭。 */
    Dismissed,

    /** 用户点击了 action。 */
    ActionPerformed,
}

/**
 * 跨风格的 snackbar 状态。通过 [rememberMeowSnackbarState] 创建，
 * 传给 [MeowScaffold] 或 [MeowPreferencePage] 后，在与宿主同生命周期的协程中调用 [show]。
 */
@Stable
class MeowSnackbarState internal constructor() {
    internal val materialHostState = MaterialSnackbarHostState()
    internal var miuixHostState by mutableStateOf(MiuixSnackbarHostState())
        private set
    private class StyleSession(val style: MeowUiStyle)
    private val styles = MutableStateFlow(StyleSession(MeowUiStyle.MaterialExpressive))
    private val queue = Mutex()
    internal var currentStyle: MeowUiStyle
        get() = styles.value.style
        set(value) {
            if (currentStyle == value) return
            // Miuix 的退场条目由宿主动画清理；风格切换销毁宿主后不再复用其队列。
            if (currentStyle == MeowUiStyle.Miuix) miuixHostState = MiuixSnackbarHostState()
            styles.value = StyleSession(value)
        }

    /**
     * 显示一条 snackbar 并挂起到它消失。
     * 切换风格时当前消息返回 [MeowSnackbarResult.Dismissed]，排队消息使用新风格。
     *
     * @param message 文本内容。
     * @param actionLabel 可选的 action 文案；用户点击时返回 [MeowSnackbarResult.ActionPerformed]。
     */
    suspend fun show(
        message: String,
        actionLabel: String? = null,
    ): MeowSnackbarResult = queue.withLock {
        val session = styles.value
        coroutineScope {
            val displayed = async { showInStyle(session.style, message, actionLabel) }
            val styleChanged = async {
                // 即使同一帧切走再切回，旧宿主上的消息也必须结束。
                styles.first { it !== session }
                MeowSnackbarResult.Dismissed
            }
            try {
                select {
                    displayed.onAwait { it }
                    styleChanged.onAwait { it }
                }
            } finally {
                displayed.cancel()
                styleChanged.cancel()
            }
        }
    }

    private suspend fun showInStyle(
        style: MeowUiStyle,
        message: String,
        actionLabel: String?,
    ): MeowSnackbarResult = when (style) {
        MeowUiStyle.MaterialExpressive -> {
            val result = materialHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
            )
            if (result == MaterialSnackbarResult.ActionPerformed) {
                MeowSnackbarResult.ActionPerformed
            } else {
                MeowSnackbarResult.Dismissed
            }
        }

        MeowUiStyle.Miuix -> {
            val host = miuixHostState
            val result = try {
                host.showSnackbar(message = message, actionLabel = actionLabel)
            } finally {
                // 上游 showSnackbar 的 await 取消不会关闭已插入的条目。
                withContext(NonCancellable) { host.newestSnackbarData()?.dismiss() }
            }
            if (result == MiuixSnackbarResult.ActionPerformed) {
                MeowSnackbarResult.ActionPerformed
            } else {
                MeowSnackbarResult.Dismissed
            }
        }
    }
}

/** 创建并记住一个跨风格 snackbar 状态，show 时自动投递到当前风格的宿主。 */
@Composable
fun rememberMeowSnackbarState(): MeowSnackbarState {
    val state = remember { MeowSnackbarState() }
    val style = MeowTheme.style
    SideEffect { state.currentStyle = style }
    return state
}

/**
 * 由 MeowScaffold 安装到各风格 Scaffold 的 snackbarHost 槽位。
 *
 * 往下让回底栏插槽顶部的那段留白（见 [LocalMeowBottomBarInset]），让 snackbar 贴着
 * 悬浮胶囊弹出，而不是浮在它上方三十多 dp 的半空里。用 offset 而不是负 padding：
 * 位移不参与测量，snackbar 自身的尺寸和换行都不受影响。
 */
@Composable
internal fun MeowSnackbarHost(state: MeowSnackbarState) {
    val deadSpace = LocalMeowBottomBarInset.current?.deadSpaceTop ?: 0.dp
    val shift = (deadSpace - MeowSnackbarGap).coerceAtLeast(0.dp)

    Box(modifier = Modifier.offset { IntOffset(x = 0, y = shift.roundToPx()) }) {
        MeowStyleContent(
            materialExpressive = {
                MaterialSnackbarHost(hostState = state.materialHostState)
            },
            miuix = {
                MiuixSnackbarHost(state = state.miuixHostState)
            },
        )
    }
}
