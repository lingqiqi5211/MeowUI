package io.github.lingqiqi5211.meowui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RecomposeScope
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
 */
@MeowPreferenceSectionDsl
@Suppress("FunctionName")
class MeowPreferenceSectionScope internal constructor() {
    // 注意:本类必须保持 unstable(不要加 @Stable/@Immutable)。
    // pending 路径假设 content lambda 作为整体重跑(receiver 不稳定使其不可被跳过);
    // 若未来变为可跳过,嵌套作用域的局部重跑可能让 pending 只含部分条目并覆盖全量。
    // 收集式 DSL 的陷阱：section 的 content 是 composable lambda，捕获值变化时它会在
    // 自己的重启作用域里单独重跑，而持有 entries 的 section 主体不重组，导致渲染停在
    // 旧条目上。因此收集分两条路：主体重组时正常收集（body）；lambda 单独重跑时先把
    // 结果暂存（pending）并把 section 主体一并失效，让渲染在下一趟重组里取到新条目。
    private var sectionScope: RecomposeScope? = null
    private var collectingInBody = false
    private var bodyContentRan = false
    private var invalidationRequested = false
    private val bodyEntries = mutableListOf<MeowPreferenceSectionEntry>()
    private val pendingEntries = mutableListOf<MeowPreferenceSectionEntry>()
    private var committedEntries: List<MeowPreferenceSectionEntry> = emptyList()

    internal fun beginCollection(scope: RecomposeScope) {
        sectionScope = scope
        collectingInBody = true
        bodyContentRan = false
        bodyEntries.clear()
    }

    internal fun endCollection(): List<MeowPreferenceSectionEntry> {
        collectingInBody = false
        committedEntries = when {
            // 主体这趟真正执行了 content（哪怕条目全部 visible = false）：以本趟收集为准。
            bodyContentRan -> bodyEntries.toList()
            // content（或其中某个嵌套作用域）单独重跑过,而主体这趟里 content 被跳过。
            // 单独重跑可能只执行了 content 的一部分（强跳过下嵌套作用域可以独立重启）,
            // pending 不能当成全量整表替换——那会把没重跑到的条目全部丢掉。按 key 并入:
            // 已有条目原位更新,新条目追加。结构性删除要等 content 真正在主体里重跑。
            invalidationRequested -> {
                val pendingByKey = pendingEntries.associateBy { it.key }
                val knownKeys = committedEntries.mapTo(mutableSetOf()) { it.key }
                buildList {
                    committedEntries.forEach { add(pendingByKey[it.key] ?: it) }
                    pendingEntries.forEach { if (it.key !in knownKeys) add(it) }
                }
            }
            // 主体因无关原因重组且 content 被跳过：沿用上一次的条目。
            else -> committedEntries
        }
        invalidationRequested = false
        pendingEntries.clear()
        return committedEntries
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
            // 在 visible 判断之前记录“content 确实执行过”，
            // 让全部隐藏时也能提交空列表，而不是沿用旧条目。
            bodyContentRan = true
            if (!visible) return
            bodyEntries += MeowPreferenceSectionEntry(
                key = key ?: bodyEntries.size,
                container = container,
                content = content,
            )
        } else {
            // 同理：即使条目全部隐藏也要请求主体重组，保证结构变化被渲染。
            if (!invalidationRequested) {
                invalidationRequested = true
                pendingEntries.clear()
                sectionScope?.invalidate()
            }
            if (!visible) return
            pendingEntries += MeowPreferenceSectionEntry(
                key = key ?: pendingEntries.size,
                container = container,
                content = content,
            )
        }
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
