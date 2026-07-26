package io.github.lingqiqi5211.meowui.component

import androidx.compose.material3.SnackbarHost as MaterialSnackbarHost
import androidx.compose.material3.SnackbarHostState as MaterialSnackbarHostState
import androidx.compose.material3.SnackbarResult as MaterialSnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
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
 * 传给 [MeowScaffold] 或 [MeowPreferencePage] 的 `snackbarState` 后即可在任意协程中 [show]。
 */
@Stable
class MeowSnackbarState internal constructor() {
    internal val materialHostState = MaterialSnackbarHostState()
    internal val miuixHostState = MiuixSnackbarHostState()
    internal var currentStyle: MeowUiStyle = MeowUiStyle.MaterialExpressive

    /**
     * 显示一条 snackbar 并挂起到它消失。
     *
     * @param message 文本内容。
     * @param actionLabel 可选的 action 文案；用户点击时返回 [MeowSnackbarResult.ActionPerformed]。
     */
    suspend fun show(
        message: String,
        actionLabel: String? = null,
    ): MeowSnackbarResult = when (currentStyle) {
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
            val result = miuixHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
            )
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

/** 由 MeowScaffold 安装到各风格 Scaffold 的 snackbarHost 槽位。 */
@Composable
internal fun MeowSnackbarHost(state: MeowSnackbarState) {
    MeowStyleContent(
        materialExpressive = {
            MaterialSnackbarHost(hostState = state.materialHostState)
        },
        miuix = {
            MiuixSnackbarHost(state = state.miuixHostState)
        },
    )
}
