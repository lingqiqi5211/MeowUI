package io.github.lingqiqi5211.meowui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.lingqiqi5211.meowui.core.preference.PreferenceKey

@DslMarker
annotation class MeowPreferenceSectionDsl

internal data class MeowPreferenceSectionEntry(
    val key: Any,
    val container: Boolean,
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
 *
 * A row that comes and goes should be declared with `item(visible = …)` rather than omitted:
 * declaring it is how [content] announces that it re-ran. A [content] that ends up declaring no
 * items at all says nothing, so a group cannot notice on its own that it has just become empty —
 * something else about the section (its title, or the caller recomposing) has to change with it.
 */
@MeowPreferenceSectionDsl
@Suppress("FunctionName")
class MeowPreferenceSectionScope internal constructor() {
    // 注意:本类必须保持 unstable(不要加 @Stable/@Immutable)。
    //
    // 收集式 DSL 的陷阱：section 的 content 是 composable lambda，捕获值变化时它会在自己的
    // 重启作用域里单独重跑，而持有 entries 的 section 主体不重组——渲染就停在旧条目上。
    //
    // 单独重跑的结果一律不提交：它可能只跑了 content 的一部分（强跳过下嵌套作用域可以独立
    // 重启），既不能整表替换，按 key 并入更糟——旧行留在原位、新行追加在后面，一个分组里
    // 同时挂着两套内容。改为让主体重收一趟：收集轮次是状态，单独重跑把它 +1，主体读到新值
    // 后在 [collectionEpoch] 这个新 key 下重组 content。新 key 下没有旧组可复用，content
    // 必然完整重跑，于是提交的一定是当前该有的那一整份。
    private var collectingInBody = false
    private var pendingRecollect = false
    private val bodyEntries = mutableListOf<MeowPreferenceSectionEntry>()
    private var committedEntries: List<MeowPreferenceSectionEntry> = emptyList()

    /** 收集轮次。主体必须在组合里读它（用作 content 的 key），否则重收不会发生。 */
    internal var collectionEpoch by mutableIntStateOf(0)
        private set

    internal fun beginCollection() {
        collectingInBody = true
        bodyEntries.clear()
    }

    internal fun endCollection(): List<MeowPreferenceSectionEntry> {
        collectingInBody = false
        val collected = bodyEntries.toList()
        // 条目只多不少：正常的一趟，直接换上。
        //
        // 变少了有两种可能，在这里分不出来：主体复用了 content 整组、只重跑了其中一个嵌套
        // 作用域（拿到的是残缺的一份），或者真有行被隐藏了。先按前者处理——留着上一份、换个
        // key 重收一趟；重收那趟没有旧组可复用，content 必然完整跑，那时仍然变少就是真删了。
        if (collected.size >= committedEntries.size || pendingRecollect) {
            committedEntries = collected
            pendingRecollect = false
        } else {
            requestRecollect()
        }
        return committedEntries
    }

    /** 让主体在新的轮次下把 content 整趟重跑一遍。 */
    private fun requestRecollect() {
        if (pendingRecollect) return
        pendingRecollect = true
        collectionEpoch++
    }

    /**
     * 自定义条目。
     *
     * [container] 为 true（默认）时由分区负责绘制该条目的容器：Material 分支给它一层
     * 与相邻条目一致的分组卡片（分段圆角 + 卡片色），Miuix 分支不需要额外处理——整个
     * 分区本来就是一张卡片。库内的 MeowXxxPreference 自己就是带容器的 ListItem，
     * 因此都传 false。自绘容器的内容（例如自带选中态色块的选项卡）也应传 false。
     */
    fun item(
        key: Any? = null,
        visible: Boolean = true,
        container: Boolean = true,
        content: @Composable () -> Unit,
    ) {
        if (collectingInBody) {
            if (!visible) return
            bodyEntries += MeowPreferenceSectionEntry(
                key = key ?: bodyEntries.size,
                container = container,
                content = content,
            )
            return
        }
        // content 在主体之外单独重跑了。这一趟的条目一概不要（可能只是其中一部分），
        // 只记下“要重收”：主体读到新的轮次后会在新 key 下把 content 整趟跑一遍。
        requestRecollect()
    }

    fun MeowActionPreference(
        title: String,
        modifier: Modifier = Modifier,
        summary: String? = null,
        value: String? = null,
        enabled: Boolean = true,
        navigation: Boolean = false,
        leading: (@Composable () -> Unit)? = null,
        trailing: (@Composable () -> Unit)? = null,
        onClick: () -> Unit,
    ) = item(key = title, container = false) {
        io.github.lingqiqi5211.meowui.component.MeowActionPreference(
            title = title,
            modifier = modifier,
            summary = summary,
            value = value,
            enabled = enabled,
            navigation = navigation,
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
    ) = item(key = title, container = false) {
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
    ) = item(key = key.name, container = false) {
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
    ) = item(key = title, container = false) {
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
    ) = item(key = key.name, container = false) {
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
        onClick: (() -> Unit)? = null,
        defaultValue: Float? = null,
        snapToDefault: Boolean = true,
        showSteps: Boolean = false,
        defaultText: String? = "Default",
    ) = item(key = title, container = false) {
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
            onClick = onClick,
            defaultValue = defaultValue,
            snapToDefault = snapToDefault,
            showSteps = showSteps,
            defaultText = defaultText,
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
        onClick: (() -> Unit)? = null,
        showDefaultValue: Boolean = false,
        snapToDefault: Boolean = true,
        showSteps: Boolean = false,
        defaultText: String? = "Default",
    ) = item(key = key.name, container = false) {
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
            onClick = onClick,
            showDefaultValue = showDefaultValue,
            snapToDefault = snapToDefault,
            showSteps = showSteps,
            defaultText = defaultText,
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
    ) = item(key = title, container = false) {
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
    ) = item(key = key.name, container = false) {
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
    ) = item(key = title, container = false) {
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
    ) = item(key = key.name, container = false) {
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
