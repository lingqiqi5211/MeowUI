package io.github.lingqiqi5211.meowui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.Checkbox as MaterialCheckbox
import androidx.compose.material3.DropdownMenuGroup as MaterialDropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem as MaterialDropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup as MaterialDropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SegmentedListItem as MaterialSegmentedListItem
import androidx.compose.material3.Slider as MaterialSlider
import androidx.compose.material3.Switch as MaterialSwitch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.SmallTitle as MiuixSmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.preference.ArrowPreference as MiuixArrowPreference
import top.yukonga.miuix.kmp.preference.CheckboxLocation
import top.yukonga.miuix.kmp.preference.CheckboxPreference as MiuixCheckboxPreference
import top.yukonga.miuix.kmp.preference.SliderPreference as MiuixSliderPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference as MiuixSwitchPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference as MiuixWindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

private val LocalMaterialPreferenceItemShapes = staticCompositionLocalOf<ListItemShapes?> { null }

/** 文本输入类组件"不允许为空"的统一默认错误文案。 */
internal const val MeowDefaultBlankErrorText = "Value cannot be empty"

@Composable
fun MeowPreferenceScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = MeowTheme.dimensions.pageHorizontalPadding,
        vertical = MeowTheme.dimensions.pageVerticalPadding,
    ),
    scaffoldPadding: PaddingValues = LocalMeowScaffoldContentPadding.current,
    content: @Composable ColumnScope.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val scrollConnection = LocalMeowScrollContext.current.nestedScrollConnection
    val mergedPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDirection) +
            scaffoldPadding.calculateStartPadding(layoutDirection),
        top = contentPadding.calculateTopPadding() + scaffoldPadding.calculateTopPadding(),
        end = contentPadding.calculateEndPadding(layoutDirection) +
            scaffoldPadding.calculateEndPadding(layoutDirection),
        bottom = contentPadding.calculateBottomPadding() + scaffoldPadding.calculateBottomPadding(),
    )
    val nestedScrollModifier = scrollConnection?.let { Modifier.nestedScroll(it) } ?: Modifier

    MeowStyleContent(
        materialExpressive = {
            PreferenceColumn(
                modifier = modifier.then(nestedScrollModifier),
                contentPadding = mergedPadding,
                sectionSpacing = 13.dp,
                content = content,
            )
        },
        miuix = {
            PreferenceColumn(
                modifier = modifier
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .then(nestedScrollModifier),
                contentPadding = mergedPadding,
                sectionSpacing = 12.dp,
                content = content,
            )
        },
    )
}

@Composable
private fun PreferenceColumn(
    modifier: Modifier,
    contentPadding: PaddingValues,
    sectionSpacing: androidx.compose.ui.unit.Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
        content = content,
    )
}

/**
 * A style-native preference group whose items are collected before rendering.
 *
 * Material uses the final item index and count for segmented corners, while Miuix renders the same
 * entries in its native card container. Business code declares the group only once.
 *
 * [content] is composable, so row parameters may use `stringResource`, `remember`, or other
 * composable calls directly. Rows must still be declared through the scope helpers or [MeowPreferenceSectionScope.item];
 * composables emitted directly inside [content] are not part of the group.
 */
@Composable
fun MeowPreferenceSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable MeowPreferenceSectionScope.() -> Unit,
) {
    val scope = remember { MeowPreferenceSectionScope() }
    scope.beginCollection(currentRecomposeScope)
    scope.content()
    val entries = scope.endCollection()
    if (entries.isEmpty()) return

    // 直接按风格分发而不是经过 MeowStyleContent 的 lambda 槽位：
    // entries 每趟收集都会重建，经 lambda 捕获后会被跳过更新，
    // 导致条目内容（如受控开关的 checked）停留在旧值，直到页面整体重建。
    when (MeowTheme.style) {
        MeowUiStyle.MaterialExpressive -> MaterialPreferenceSection(
            modifier = modifier,
            title = title,
            entries = entries,
        )

        MeowUiStyle.Miuix -> MiuixPreferenceSection(
            modifier = modifier,
            title = title,
            entries = entries,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MaterialPreferenceSection(
    modifier: Modifier,
    title: String?,
    entries: List<MeowPreferenceSectionEntry>,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        title?.takeIf(String::isNotBlank)?.let {
            MaterialText(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
        Column(
            // 分区内条目增减（如外观页隐藏调色板选项）时高度平滑过渡。
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            entries.forEachIndexed { index, entry ->
                key(entry.key) {
                    CompositionLocalProvider(
                        LocalMaterialPreferenceItemShapes provides
                            meowSegmentedItemShapes(index, entries.size),
                    ) {
                        entry.content()
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixPreferenceSection(
    modifier: Modifier,
    title: String?,
    entries: List<MeowPreferenceSectionEntry>,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        title?.takeIf(String::isNotBlank)?.let {
            MiuixSmallTitle(
                text = it,
                insideMargin = PaddingValues(
                    start = 12.dp,
                    top = 8.dp,
                    end = 12.dp,
                    bottom = 12.dp,
                ),
            )
        }
        MiuixCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            insideMargin = PaddingValues(0.dp),
        ) {
            entries.forEach { entry ->
                key(entry.key) { entry.content() }
            }
        }
    }
}

@Composable
fun MeowActionPreference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    value: String? = null,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialPreferenceRow(
                title = title,
                modifier = modifier,
                summary = summary,
                value = value,
                enabled = enabled,
                leading = leading,
                trailing = trailing,
                onClick = onClick,
            )
        },
        miuix = {
            MiuixArrowPreference(
                title = title,
                modifier = modifier,
                summary = summary,
                startAction = miuixStartAction(leading),
                endActions = {
                    value?.takeIf(String::isNotBlank)?.let {
                        MiuixText(
                            text = it,
                            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            style = MeowTheme.typography.value,
                            maxLines = 1,
                        )
                    }
                    trailing?.invoke()
                },
                onClick = onClick,
                enabled = enabled,
            )
        },
    )
}

@Composable
fun MeowSwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialSwitchPreference(
                title = title,
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = modifier,
                summary = summary,
                enabled = enabled,
                leading = leading,
            )
        },
        miuix = {
            MiuixSwitchPreference(
                checked = checked,
                onCheckedChange = onCheckedChange,
                title = title,
                modifier = modifier,
                summary = summary,
                startAction = miuixStartAction(leading),
                enabled = enabled,
            )
        },
    )
}

@Composable
fun MeowCheckboxPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialCheckboxPreference(
                title = title,
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = modifier,
                summary = summary,
                enabled = enabled,
                leading = leading,
            )
        },
        miuix = {
            MiuixCheckboxPreference(
                title = title,
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = modifier,
                summary = summary,
                startAction = miuixStartAction(leading),
                checkboxLocation = CheckboxLocation.End,
                enabled = enabled,
            )
        },
    )
}

@Composable
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
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialSliderPreference(
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
        },
        miuix = {
            MiuixSliderPreference(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                title = title,
                summary = summary,
                valueText = valueText(value),
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                onValueChangeFinished = onValueChangeFinished,
            )
        },
    )
}

/** Style-native popup preference for choosing one value. */
@Composable
fun <T> MeowPopupPreference(
    title: String,
    value: T,
    options: List<T>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    optionLabel: (T) -> String = { it.toString() },
) {
    require(options.isNotEmpty()) { "options must not be empty" }

    MeowStyleContent(
        materialExpressive = {
            MaterialChoicePreference(
                title = title,
                value = value,
                options = options,
                onValueChange = onValueChange,
                modifier = modifier,
                summary = summary,
                enabled = enabled,
                optionLabel = optionLabel,
            )
        },
        miuix = {
            MiuixChoicePreference(
                title = title,
                value = value,
                options = options,
                onValueChange = onValueChange,
                modifier = modifier,
                summary = summary,
                enabled = enabled,
                optionLabel = optionLabel,
            )
        },
    )
}

@Composable
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
) {
    var showDialog by remember { mutableStateOf(false) }
    MeowActionPreference(
        title = title,
        modifier = modifier,
        summary = summary,
        value = value,
        enabled = enabled,
        onClick = { showDialog = true },
    )
    MeowTextInputDialog(
        show = showDialog,
        title = title,
        initialValue = value,
        placeholder = placeholder,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        allowBlank = allowBlank,
        blankErrorText = blankErrorText,
        validator = validator,
        onConfirm = {
            onValueChange(it)
            showDialog = false
        },
        onDismissRequest = { showDialog = false },
    )
}

@Composable
fun MeowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
            ) {
                MaterialText(text)
            }
        },
        miuix = {
            MiuixButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
            ) {
                MeowText(
                    text = text,
                    color = Color.Unspecified,
                    style = MeowTheme.typography.button,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <T> MaterialChoicePreference(
    title: String,
    value: T,
    options: List<T>,
    onValueChange: (T) -> Unit,
    modifier: Modifier,
    summary: String?,
    enabled: Boolean,
    optionLabel: (T) -> String,
) {
    var expanded by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    MaterialPreferenceRow(
        title = title,
        modifier = modifier,
        summary = summary,
        value = optionLabel(value),
        enabled = enabled,
        // Popup 锚定在行尾的箭头处弹出，靠右展开，与 Miuix spinner 的弹出位置语义一致。
        trailing = {
            Box {
                MaterialIcon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MaterialDropdownMenuPopup(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    MaterialDropdownMenuGroup(
                        shapes = MenuDefaults.groupShapes(),
                    ) {
                        options.forEachIndexed { index, option ->
                            key(index, option) {
                                MaterialDropdownMenuItem(
                                    text = { MaterialText(optionLabel(option)) },
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(
                                            HapticFeedbackType.VirtualKey,
                                        )
                                        onValueChange(option)
                                        expanded = false
                                    },
                                    enabled = enabled,
                                    selected = option == value,
                                    shapes = MenuDefaults.itemShape(
                                        index = index,
                                        count = options.size,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        },
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
            expanded = true
        },
    )
}

@Composable
private fun <T> MiuixChoicePreference(
    title: String,
    value: T,
    options: List<T>,
    onValueChange: (T) -> Unit,
    modifier: Modifier,
    summary: String?,
    enabled: Boolean,
    optionLabel: (T) -> String,
) {
    val selectedIndex = options.indexOf(value)

    MiuixWindowSpinnerPreference(
        items = options.map { option ->
            DropdownItem(text = optionLabel(option))
        },
        selectedIndex = selectedIndex,
        title = title,
        modifier = modifier,
        summary = summary,
        enabled = enabled,
        showValue = selectedIndex >= 0,
        onSelectedIndexChange = { index ->
            options.getOrNull(index)?.let(onValueChange)
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MaterialPreferenceRow(
    title: String,
    modifier: Modifier,
    summary: String?,
    value: String? = null,
    enabled: Boolean,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    MaterialSegmentedListItem(
        onClick = onClick,
        shapes = materialPreferenceItemShapes(),
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        leadingContent = leading,
        trailingContent = materialTrailingContent(value, trailing),
        supportingContent = summary?.takeIf(String::isNotBlank)?.let { text ->
            { MaterialPreferenceSupportingText(text) }
        },
        verticalAlignment = Alignment.CenterVertically,
        colors = materialPreferenceItemColors(),
    ) {
        MaterialPreferenceHeadline(
            text = title,
            hasSupportingContent = !summary.isNullOrBlank(),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MaterialSwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier,
    summary: String?,
    enabled: Boolean,
    leading: (@Composable () -> Unit)?,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val updateChecked: (Boolean) -> Unit = { newValue ->
        hapticFeedback.performHapticFeedback(
            if (newValue) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
        )
        onCheckedChange(newValue)
    }

    MaterialSegmentedListItem(
        onClick = { updateChecked(!checked) },
        shapes = materialPreferenceItemShapes(),
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Switch
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            },
        enabled = enabled,
        leadingContent = leading,
        interactionSource = interactionSource,
        trailingContent = {
            MaterialSwitch(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.clearAndSetSemantics {},
                enabled = enabled,
                interactionSource = interactionSource,
                colors = SwitchDefaults.colors(
                    checkedIconColor = MaterialTheme.colorScheme.primary,
                    uncheckedIconColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledCheckedThumbColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
                    disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledCheckedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledUncheckedThumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                    disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.12f),
                    disabledUncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledUncheckedIconColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
                thumbContent = if (checked || enabled) {
                    {
                        MaterialIcon(
                            imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
        },
        supportingContent = summary?.takeIf(String::isNotBlank)?.let { text ->
            { MaterialPreferenceSupportingText(text) }
        },
        verticalAlignment = Alignment.CenterVertically,
        colors = materialPreferenceItemColors(),
    ) {
        MaterialPreferenceHeadline(
            text = title,
            hasSupportingContent = !summary.isNullOrBlank(),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MaterialCheckboxPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier,
    summary: String?,
    enabled: Boolean,
    leading: (@Composable () -> Unit)?,
) {
    MaterialSegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = materialPreferenceItemShapes(),
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        leadingContent = leading,
        trailingContent = {
            MaterialCheckbox(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
        supportingContent = summary?.takeIf(String::isNotBlank)?.let { text ->
            { MaterialPreferenceSupportingText(text) }
        },
        verticalAlignment = Alignment.CenterVertically,
        colors = materialPreferenceItemColors(),
    ) {
        MaterialPreferenceHeadline(
            text = title,
            hasSupportingContent = !summary.isNullOrBlank(),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MaterialSliderPreference(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    summary: String?,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    enabled: Boolean,
    valueText: (Float) -> String,
    onValueChangeFinished: (() -> Unit)?,
) {
    MaterialSegmentedListItem(
        shapes = materialPreferenceItemShapes(),
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        supportingContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = materialPreferenceInternalPadding()),
            ) {
                summary?.takeIf(String::isNotBlank)?.let {
                    MaterialPreferenceSupportingText(it, bottomPadding = false)
                }
                Spacer(Modifier.height(12.dp))
                MaterialSlider(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    valueRange = valueRange,
                    steps = steps,
                    onValueChangeFinished = onValueChangeFinished,
                )
            }
        },
        colors = materialPreferenceItemColors(),
    ) {
        // 数值文本与标题同行显示，而不是放 trailing 槽位：
        // trailing 会占满整行右侧，把下方的滑条挤得不足全宽。
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MaterialPreferenceHeadline(
                    text = title,
                    hasSupportingContent = true,
                )
            }
            MaterialText(
                text = valueText(value),
                modifier = Modifier.padding(top = materialPreferenceInternalPadding()),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun MaterialPreferenceHeadline(
    text: String,
    hasSupportingContent: Boolean,
) {
    val padding = materialPreferenceInternalPadding()
    MaterialText(
        text = text,
        modifier = Modifier.padding(
            top = padding,
            bottom = if (hasSupportingContent) 0.dp else padding,
        ),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun MaterialPreferenceSupportingText(
    text: String,
    bottomPadding: Boolean = true,
) {
    MaterialText(
        text = text,
        modifier = Modifier.padding(
            bottom = if (bottomPadding) materialPreferenceInternalPadding() else 0.dp,
        ),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun materialPreferenceInternalPadding() =
    (4 * LocalDensity.current.fontScale).dp

/**
 * Miuix 分支的 leading 图标与标题间距。
 * Miuix 原生 startAction 与文字几乎贴着，参考 KernelSU 的做法补 6dp 尾距；
 * Material 分支的间距由 SegmentedListItem 自身提供，不需要处理。
 */
private fun miuixStartAction(
    leading: (@Composable () -> Unit)?,
): (@Composable () -> Unit)? = leading?.let { icon ->
    {
        Box(modifier = Modifier.padding(end = 6.dp)) {
            icon()
        }
    }
}

@Composable
private fun materialTrailingContent(
    value: String?,
    trailing: (@Composable () -> Unit)?,
): (@Composable () -> Unit)? {
    if (value.isNullOrBlank() && trailing == null) return null

    return {
        Row(verticalAlignment = Alignment.CenterVertically) {
            value?.takeIf(String::isNotBlank)?.let {
                MaterialText(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
            }
            if (!value.isNullOrBlank() && trailing != null) {
                Spacer(Modifier.width(12.dp))
            }
            trailing?.invoke()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun materialPreferenceItemShapes(): ListItemShapes =
    LocalMaterialPreferenceItemShapes.current ?: meowSegmentedItemShapes(0, 1)

/**
 * material3 的 `segmentedShapes(count = 1)` 会回退到普通 item 的 4dp 小圆角，
 * 与多项分组 16dp 的外圈圆角不一致；单项分组时统一改用外圈大圆角。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun meowSegmentedItemShapes(index: Int, count: Int): ListItemShapes {
    val shapes = ListItemDefaults.segmentedShapes(index, count)
    if (count != 1) return shapes
    return shapes.copy(shape = MaterialTheme.shapes.large)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun materialPreferenceItemColors(): ListItemColors = ListItemDefaults.segmentedColors(
    containerColor = MaterialTheme.colorScheme.surfaceBright,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceBright,
    supportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
internal fun MeowText(
    text: String,
    color: Color,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    MaterialText(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
