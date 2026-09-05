package io.github.lingqiqi5211.meowui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 触觉反馈的语义入口。取值照 miuix 的用法，两种风格下同一交互给同一种震感：
 *
 * | 交互 | 类型 | miuix 出处 |
 * | --- | --- | --- |
 * | 开关、复选、单选切换 | ToggleOn / ToggleOff | Switch、Checkbox、RadioButton |
 * | 弹出菜单展开 | ContextClick | DropdownMenu、SpinnerPreference |
 * | 在弹窗里选中一项 | Confirm | DropdownPopup |
 * | 手势过阈值：滑条到端、下拉到位 | GestureThresholdActivate | Slider、PullToRefresh |
 * | 跨过一格：滑条走到下一档、列表甩到尽头 | TextHandleMove | Slider、scrollEndHaptic |
 *
 * Miuix 分支的原生组件自带这些反馈，库里不重复触发；这里补的是 Material 分支和库内
 * 自绘的列表（对话框里的单选 / 多选行）。
 */
@Stable
internal class MeowHaptics(private val feedback: State<HapticFeedback>) {
    fun toggled(on: Boolean) {
        perform(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
    }

    fun menuOpened() = perform(HapticFeedbackType.ContextClick)

    fun picked() = perform(HapticFeedbackType.Confirm)

    fun thresholdReached() = perform(HapticFeedbackType.GestureThresholdActivate)

    fun tick() = perform(HapticFeedbackType.TextHandleMove)

    private fun perform(type: HapticFeedbackType) = feedback.value.performHapticFeedback(type)
}

/** 实例稳定，可被 `remember` 住的 lambda 捕获；内部始终读最新的 [LocalHapticFeedback]。 */
@Composable
internal fun rememberMeowHaptics(): MeowHaptics {
    val feedback = rememberUpdatedState(LocalHapticFeedback.current)
    return remember { MeowHaptics(feedback) }
}

/**
 * 滑条的反馈，照 miuix `SliderHapticState`：
 *
 * - 到端：值进入 0% 或 100% 时响一次，离开端点后再回来才再响；一开始就停在端点上不响。
 * - 分档（`steps > 0`）：换到另一档时轻响一下，端点那一下已经由到端反馈负责，不重复。
 */
@Stable
internal class MeowSliderHaptic {
    private var atEdge = false
    private var lastStep = Float.NaN

    fun onValueChange(
        value: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        steps: Int,
        haptics: MeowHaptics,
    ) {
        val isAtEdge = value == valueRange.start || value == valueRange.endInclusive
        if (isAtEdge && !atEdge) haptics.thresholdReached()
        atEdge = isAtEdge
        if (steps <= 0) return
        if (!isAtEdge && value != lastStep) haptics.tick()
        lastStep = value
    }

    /** 与实际显示的值对齐：外部改值不经过 [onValueChange]，不同步的话下一次拖动会误响。 */
    fun settle(value: Float, valueRange: ClosedFloatingPointRange<Float>) {
        atEdge = value == valueRange.start || value == valueRange.endInclusive
        lastStep = value
    }
}

@Composable
internal fun rememberSliderHaptic(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
): MeowSliderHaptic {
    val state = remember { MeowSliderHaptic() }
    SideEffect { state.settle(value, valueRange) }
    return state
}
