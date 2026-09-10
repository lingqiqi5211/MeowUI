package io.github.lingqiqi5211.meowui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.lingqiqi5211.meowui.core.preference.PreferenceConnectionState
import io.github.lingqiqi5211.meowui.core.preference.PreferenceKey
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceConnectionState
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceValue
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceWriter
import io.github.lingqiqi5211.meowui.preference.rememberMeowPreferenceSuspendingWriter
import kotlinx.coroutines.launch

/** A switch row that reads and writes [key] through the nearest preference provider. */
@Composable
fun MeowSwitchPreference(
    key: PreferenceKey<Boolean>,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    val checked by rememberMeowPreferenceValue(key)
    val connectionState by rememberMeowPreferenceConnectionState()
    val write = rememberMeowPreferenceWriter(key)

    MeowSwitchPreference(
        title = title,
        checked = checked,
        onCheckedChange = { value ->
            write(value)
            onCheckedChange(value)
        },
        modifier = modifier,
        summary = summary,
        enabled = enabled && connectionState !is PreferenceConnectionState.Disconnected,
        leading = leading,
    )
}

/** A checkbox row that reads and writes [key] through the nearest preference provider. */
@Composable
fun MeowCheckboxPreference(
    key: PreferenceKey<Boolean>,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    val checked by rememberMeowPreferenceValue(key)
    val connectionState by rememberMeowPreferenceConnectionState()
    val write = rememberMeowPreferenceWriter(key)

    MeowCheckboxPreference(
        title = title,
        checked = checked,
        onCheckedChange = { value ->
            write(value)
            onCheckedChange(value)
        },
        modifier = modifier,
        summary = summary,
        enabled = enabled && connectionState !is PreferenceConnectionState.Disconnected,
        leading = leading,
    )
}

/** A slider row that reads and writes [key] through the nearest preference provider. */
@Composable
fun MeowSliderPreference(
    key: PreferenceKey<Float>,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    valueText: (Float) -> String = { it.toString() },
    onValueChange: (Float) -> Unit = {},
    onClick: (() -> Unit)? = null,
    /** 在滑条上标出 [key] 的默认值。 */
    showDefaultValue: Boolean = false,
    /** 标出默认值时，拖到附近吸上去。 */
    snapToDefault: Boolean = true,
    showSteps: Boolean = false,
    defaultText: String? = "Default",
) {
    val storedValue by rememberMeowPreferenceValue(key)
    val connectionState by rememberMeowPreferenceConnectionState()
    val write = rememberMeowPreferenceSuspendingWriter(key)
    val scope = rememberCoroutineScope()

    // 拖动期间用本地值回显，抬手才提交一次，避免每帧写入远程存储并等待值回环。
    var draftValue by remember(write) { mutableStateOf<Float?>(null) }
    val editVersion = remember(write) { longArrayOf(0L) }

    MeowSliderPreference(
        title = title,
        value = draftValue ?: storedValue,
        onValueChange = { newValue ->
            editVersion[0]++
            draftValue = newValue
            onValueChange(newValue)
        },
        modifier = modifier,
        summary = summary,
        valueRange = valueRange,
        steps = steps,
        enabled = enabled && connectionState !is PreferenceConnectionState.Disconnected,
        valueText = valueText,
        onValueChangeFinished = {
            draftValue?.let { value ->
                val version = editVersion[0]
                scope.launch {
                    try {
                        write(value)
                    } finally {
                        // 失败或没有产生新值的写入也要退出草稿；旧写入不覆盖新一轮拖动。
                        if (version == editVersion[0]) draftValue = null
                    }
                }
            }
        },
        onClick = onClick,
        defaultValue = if (showDefaultValue) key.defaultValue else null,
        snapToDefault = snapToDefault,
        showSteps = showSteps,
        defaultText = defaultText,
    )
}

/** A popup preference that only needs a title, key and options. */
@Composable
fun <T : Any> MeowPopupPreference(
    key: PreferenceKey<T>,
    title: String,
    options: List<T>,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    optionLabel: (T) -> String = { it.toString() },
    onValueChange: (T) -> Unit = {},
) {
    val value by rememberMeowPreferenceValue(key)
    val connectionState by rememberMeowPreferenceConnectionState()
    val write = rememberMeowPreferenceWriter(key)

    MeowPopupPreference(
        title = title,
        value = value,
        options = options,
        onValueChange = { newValue ->
            write(newValue)
            onValueChange(newValue)
        },
        modifier = modifier,
        summary = summary,
        enabled = enabled && connectionState !is PreferenceConnectionState.Disconnected,
        optionLabel = optionLabel,
    )
}

/** A text input row that reads and writes [key] through the nearest preference provider. */
@Composable
fun MeowTextInputPreference(
    key: PreferenceKey<String>,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = if (singleLine) 1 else 3,
    maxLines: Int = if (singleLine) 1 else 6,
    allowBlank: Boolean = true,
    blankErrorText: String = MeowDefaultBlankErrorText,
    validator: (String) -> String? = { null },
    onValueChange: (String) -> Unit = {},
) {
    val value by rememberMeowPreferenceValue(key)
    val connectionState by rememberMeowPreferenceConnectionState()
    val write = rememberMeowPreferenceWriter(key)

    MeowTextInputPreference(
        title = title,
        value = value,
        onValueChange = { newValue ->
            write(newValue)
            onValueChange(newValue)
        },
        modifier = modifier,
        summary = summary,
        enabled = enabled && connectionState !is PreferenceConnectionState.Disconnected,
        placeholder = placeholder,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        allowBlank = allowBlank,
        blankErrorText = blankErrorText,
        validator = validator,
    )
}
