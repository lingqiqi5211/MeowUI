package io.github.lingqiqi5211.meowui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.lingqiqi5211.meowui.core.preference.PreferenceKey

@DslMarker
annotation class MeowPreferenceSectionDsl

internal data class MeowPreferenceSectionEntry(
    val key: Any,
    val content: @Composable () -> Unit,
)

/**
 * Collects a complete preference group before it is rendered.
 *
 * This lets each visual style calculate item position from the final item count, so segmented
 * corners are correct on the first frame. Prefer the typed helpers in this scope; use [item] for
 * custom content that still belongs to the group. Typed helpers use the bound key name or the row
 * title as the stable identity; give [item] an explicit [item.key] when rows can appear or
 * disappear conditionally.
 */
@MeowPreferenceSectionDsl
@Suppress("FunctionName")
class MeowPreferenceSectionScope internal constructor() {
    internal val entries = mutableListOf<MeowPreferenceSectionEntry>()

    fun item(
        key: Any? = null,
        visible: Boolean = true,
        content: @Composable () -> Unit,
    ) {
        if (!visible) return
        entries += MeowPreferenceSectionEntry(
            key = key ?: entries.size,
            content = content,
        )
    }

    fun MeowActionPreference(
        title: String,
        modifier: Modifier = Modifier,
        summary: String? = null,
        value: String? = null,
        enabled: Boolean = true,
        leading: (@Composable () -> Unit)? = null,
        trailing: (@Composable () -> Unit)? = null,
        onClick: () -> Unit,
    ) = item(key = title) {
        io.github.lingqiqi5211.meowui.component.MeowActionPreference(
            title = title,
            modifier = modifier,
            summary = summary,
            value = value,
            enabled = enabled,
            leading = leading,
            trailing = trailing,
            onClick = onClick,
        )
    }

    fun MeowSwitchPreference(
        title: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        summary: String? = null,
        enabled: Boolean = true,
        leading: (@Composable () -> Unit)? = null,
    ) = item(key = title) {
        io.github.lingqiqi5211.meowui.component.MeowSwitchPreference(
            title = title,
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            summary = summary,
            enabled = enabled,
            leading = leading,
        )
    }

    fun MeowSwitchPreference(
        key: PreferenceKey<Boolean>,
        title: String,
        modifier: Modifier = Modifier,
        summary: String? = null,
        enabled: Boolean = true,
        leading: (@Composable () -> Unit)? = null,
        onCheckedChange: (Boolean) -> Unit = {},
    ) = item(key = key.name) {
        io.github.lingqiqi5211.meowui.component.MeowSwitchPreference(
            key = key,
            title = title,
            modifier = modifier,
            summary = summary,
            enabled = enabled,
            leading = leading,
            onCheckedChange = onCheckedChange,
        )
    }

    fun MeowCheckboxPreference(
        title: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        summary: String? = null,
        enabled: Boolean = true,
        leading: (@Composable () -> Unit)? = null,
    ) = item(key = title) {
        io.github.lingqiqi5211.meowui.component.MeowCheckboxPreference(
            title = title,
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            summary = summary,
            enabled = enabled,
            leading = leading,
        )
    }

    fun MeowCheckboxPreference(
        key: PreferenceKey<Boolean>,
        title: String,
        modifier: Modifier = Modifier,
        summary: String? = null,
        enabled: Boolean = true,
        leading: (@Composable () -> Unit)? = null,
        onCheckedChange: (Boolean) -> Unit = {},
    ) = item(key = key.name) {
        io.github.lingqiqi5211.meowui.component.MeowCheckboxPreference(
            key = key,
            title = title,
            modifier = modifier,
            summary = summary,
            enabled = enabled,
            leading = leading,
            onCheckedChange = onCheckedChange,
        )
    }

    fun MeowSliderPreference(
        title: String,
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier = Modifier,
        summary: String? = null,
        valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        steps: Int = 0,
        enabled: Boolean = true,
        valueText: (Float) -> String = { it.toString() },
        onValueChangeFinished: (() -> Unit)? = null,
    ) = item(key = title) {
        io.github.lingqiqi5211.meowui.component.MeowSliderPreference(
            title = title,
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            summary = summary,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            valueText = valueText,
            onValueChangeFinished = onValueChangeFinished,
        )
    }

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
    ) = item(key = key.name) {
        io.github.lingqiqi5211.meowui.component.MeowSliderPreference(
            key = key,
            title = title,
            modifier = modifier,
            summary = summary,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled,
            valueText = valueText,
            onValueChange = onValueChange,
        )
    }

    fun <T> MeowPopupPreference(
        title: String,
        value: T,
        options: List<T>,
        onValueChange: (T) -> Unit,
        modifier: Modifier = Modifier,
        summary: String? = null,
        enabled: Boolean = true,
        optionLabel: (T) -> String = { it.toString() },
    ) = item(key = title) {
        io.github.lingqiqi5211.meowui.component.MeowPopupPreference(
            title = title,
            value = value,
            options = options,
            onValueChange = onValueChange,
            modifier = modifier,
            summary = summary,
            enabled = enabled,
            optionLabel = optionLabel,
        )
    }

    fun <T : Any> MeowPopupPreference(
        key: PreferenceKey<T>,
        title: String,
        options: List<T>,
        modifier: Modifier = Modifier,
        summary: String? = null,
        enabled: Boolean = true,
        optionLabel: (T) -> String = { it.toString() },
        onValueChange: (T) -> Unit = {},
    ) = item(key = key.name) {
        io.github.lingqiqi5211.meowui.component.MeowPopupPreference(
            key = key,
            title = title,
            options = options,
            modifier = modifier,
            summary = summary,
            enabled = enabled,
            optionLabel = optionLabel,
            onValueChange = onValueChange,
        )
    }

    fun MeowTextInputPreference(
        title: String,
        value: String,
        onValueChange: (String) -> Unit,
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
    ) = item(key = title) {
        io.github.lingqiqi5211.meowui.component.MeowTextInputPreference(
            title = title,
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            summary = summary,
            enabled = enabled,
            placeholder = placeholder,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            allowBlank = allowBlank,
            blankErrorText = blankErrorText,
            validator = validator,
        )
    }

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
    ) = item(key = key.name) {
        io.github.lingqiqi5211.meowui.component.MeowTextInputPreference(
            key = key,
            title = title,
            modifier = modifier,
            summary = summary,
            enabled = enabled,
            placeholder = placeholder,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            allowBlank = allowBlank,
            blankErrorText = blankErrorText,
            validator = validator,
            onValueChange = onValueChange,
        )
    }
}
