package io.github.lingqiqi5211.meowui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.Checkbox as MaterialCheckbox
import androidx.compose.material3.DropdownMenuGroup as MaterialDropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem as MaterialDropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SegmentedListItem as MaterialSegmentedListItem
import androidx.compose.material3.Slider as MaterialSlider
import androidx.compose.material3.SliderDefaults as MaterialSliderDefaults
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Switch as MaterialSwitch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import kotlin.math.abs
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Slider as MiuixSlider
import top.yukonga.miuix.kmp.basic.SliderDefaults as MiuixSliderDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle as MiuixSmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.preference.ArrowPreference as MiuixArrowPreference
import top.yukonga.miuix.kmp.preference.CheckboxLocation
import top.yukonga.miuix.kmp.preference.CheckboxPreference as MiuixCheckboxPreference
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
            // 甩到列表尽头轻响一下，与 Miuix 分支同一处反馈；overscroll 仍是 Material 自己的拉伸。
            PreferenceColumn(
                modifier = modifier
                    .scrollEndHaptic()
                    .then(nestedScrollModifier),
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
    sectionSpacing: Dp,
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
    // 轮次读在这里、也用作 content 的 key：lambda 单独重跑过一趟后轮次 +1，这里重组，
    // content 在新 key 下没有旧组可复用，必然完整重跑一遍（见 MeowPreferenceSectionScope）。
    val epoch = scope.collectionEpoch
    scope.beginCollection()
    key(epoch) { scope.content() }
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
                    val shapes = meowSegmentedItemShapes(index, entries.size)
                    CompositionLocalProvider(
                        LocalMaterialPreferenceItemShapes provides shapes,
                    ) {
                        if (entry.container) {
                            // Material 分支的每个条目各自画容器,所以自定义条目要由分区
                            // 补上一层同色同圆角的卡片,否则它在分组里是一个透明缺口。
                            MaterialSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = shapes.shape,
                                color = MaterialTheme.colorScheme.surfaceBright,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ) {
                                entry.content()
                            }
                        } else {
                            entry.content()
                        }
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
        // 抽屉底色与卡片默认底色同档,贴在一起看不出边界,所以抽屉里的分组抬高一档。
        val cardColors = if (LocalMeowOnSheet.current) {
            MiuixCardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainerHigh)
        } else {
            MiuixCardDefaults.defaultColors()
        }
        MiuixCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            insideMargin = PaddingValues(0.dp),
            colors = cardColors,
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
    /**
     * 行的动作是打开另一个页面时置 true：Material 分支行尾追加 > 箭头。
     * Miuix 分支的 ArrowPreference 本来就带箭头，不受此参数影响。
     */
    navigation: Boolean = false,
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
                navigation = navigation,
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
    /** 整行可点（滑条仍归拖动）；两种风格都不加行尾箭头。 */
    onClick: (() -> Unit)? = null,
    /** 在滑条上标出默认值并吸附；null 不画。 */
    defaultValue: Float? = null,
    /** 分档滑条是否画出每一档的刻度点。 */
    showSteps: Boolean = false,
) {
    val keyPoints = remember(valueRange, steps, showSteps, defaultValue) {
        sliderKeyPoints(valueRange, steps, showSteps, defaultValue)
    }
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
                onClick = onClick,
                defaultValue = defaultValue?.takeIf { it in valueRange },
                showSteps = showSteps,
            )
        },
        miuix = {
            MiuixSliderRow(
                title = title,
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                summary = summary,
                valueRange = valueRange,
                steps = steps,
                enabled = enabled,
                valueText = valueText(value),
                onValueChangeFinished = onValueChangeFinished,
                onClick = onClick,
                keyPoints = keyPoints,
            )
        },
    )
}

/** 滑条上要画、也要吸附的点；没有时返回 null。 */
private fun sliderKeyPoints(
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    showSteps: Boolean,
    defaultValue: Float?,
): List<Float>? {
    val points = mutableListOf<Float>()
    if (showSteps && steps > 0) {
        val span = valueRange.endInclusive - valueRange.start
        for (index in 0..steps + 1) {
            points += valueRange.start + span * index / (steps + 1)
        }
    }
    if (defaultValue != null && defaultValue in valueRange && points.none { it == defaultValue }) {
        points += defaultValue
    }
    return points.takeIf { it.isNotEmpty() }
}

/** 吸附半径，占取值范围的比例；同 miuix Slider 的默认值。 */
private const val SliderMagnetThreshold = 0.02f

/** 不用 miuix SliderPreference：它一给 onClick 就画行尾箭头。这里按它的结构自己拼。 */
@Composable
private fun MiuixSliderRow(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    summary: String?,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    enabled: Boolean,
    valueText: String,
    onValueChangeFinished: (() -> Unit)?,
    onClick: (() -> Unit)?,
    keyPoints: List<Float>?,
) {
    BasicComponent(
        modifier = modifier,
        title = title,
        summary = summary,
        endActions = {
            MiuixText(
                text = valueText,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .align(Alignment.CenterVertically)
                    .weight(1f, fill = false),
                color = if (enabled) {
                    MiuixTheme.colorScheme.onSurfaceVariantActions
                } else {
                    MiuixTheme.colorScheme.disabledOnSecondaryVariant
                },
                style = MeowTheme.typography.value,
            )
        },
        bottomAction = {
            MiuixSlider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                onValueChangeFinished = onValueChangeFinished,
                // 有档或有标记点才响换档；连续滑条只响到端。
                hapticEffect = if (steps > 0 || keyPoints != null) {
                    MiuixSliderDefaults.SliderHapticEffect.Step
                } else {
                    MiuixSliderDefaults.SliderHapticEffect.Edge
                },
                showKeyPoints = keyPoints != null,
                keyPoints = keyPoints,
                magnetThreshold = SliderMagnetThreshold,
            )
        },
        onClick = onClick,
        enabled = enabled,
    )
}

/**
 * Style-native popup preference for choosing one value.
 *
 * 选项可附图标（[optionLeading]）、副文本（[optionSummary]）与单项禁用
 * （[optionEnabled]）；[groups] 非空时每个子列表为一组（组间分隔，
 * [options] 被忽略）；[collapseOnSelection] 为 false 时选择后弹窗不收起。
 */
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
    optionSummary: ((T) -> String?)? = null,
    optionEnabled: ((T) -> Boolean)? = null,
    optionLeading: (@Composable (T) -> Unit)? = null,
    groups: List<List<T>> = emptyList(),
    collapseOnSelection: Boolean = true,
) {
    val resolvedGroups = groups.filter { it.isNotEmpty() }.ifEmpty { listOf(options) }
    require(resolvedGroups.any { it.isNotEmpty() }) { "options must not be empty" }

    MeowStyleContent(
        materialExpressive = {
            MaterialChoicePreference(
                title = title,
                value = value,
                groups = resolvedGroups,
                onValueChange = onValueChange,
                modifier = modifier,
                summary = summary,
                enabled = enabled,
                optionLabel = optionLabel,
                optionSummary = optionSummary,
                optionEnabled = optionEnabled,
                optionLeading = optionLeading,
                collapseOnSelection = collapseOnSelection,
            )
        },
        miuix = {
            MiuixChoicePreference(
                title = title,
                value = value,
                groups = resolvedGroups,
                onValueChange = onValueChange,
                modifier = modifier,
                summary = summary,
                enabled = enabled,
                optionLabel = optionLabel,
                optionSummary = optionSummary,
                optionEnabled = optionEnabled,
                optionLeading = optionLeading,
                collapseOnSelection = collapseOnSelection,
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
    confirmText: String = "OK",
    cancelText: String = "Cancel",
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
        confirmText = confirmText,
        cancelText = cancelText,
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
    groups: List<List<T>>,
    onValueChange: (T) -> Unit,
    modifier: Modifier,
    summary: String?,
    enabled: Boolean,
    optionLabel: (T) -> String,
    optionSummary: ((T) -> String?)?,
    optionEnabled: ((T) -> Boolean)?,
    optionLeading: (@Composable (T) -> Unit)?,
    collapseOnSelection: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val haptics = rememberMeowHaptics()

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
                MeowMaterialMenuPopup(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    // 每组一个大圆角容器，组间用官方间距，与 miuix 的 DropdownEntry 分组语义对齐。
                    groups.forEachIndexed { groupIndex, groupOptions ->
                        if (groupIndex > 0) {
                            Spacer(Modifier.height(MenuDefaults.GroupSpacing))
                        }
                        MaterialDropdownMenuGroup(
                            shapes = MenuDefaults.groupShapes(),
                        ) {
                            groupOptions.forEachIndexed { index, option ->
                                key(index, option) {
                                    // 分段项间隙：相邻选中 pill 不再贴成一块。
                                    if (index > 0) {
                                        Spacer(Modifier.height(ListItemDefaults.SegmentedGap))
                                    }
                                    MaterialDropdownMenuItem(
                                        // 选中态交给 selected 的容器样式，不自己画 ✓。
                                        text = { MaterialText(optionLabel(option)) },
                                        supportingText = optionSummary?.invoke(option)
                                            ?.takeIf(String::isNotBlank)
                                            ?.let { text -> { MaterialText(text) } },
                                        onClick = {
                                            haptics.picked()
                                            onValueChange(option)
                                            if (collapseOnSelection) expanded = false
                                        },
                                        enabled = enabled &&
                                            (optionEnabled?.invoke(option) ?: true),
                                        selected = option == value,
                                        leadingIcon = optionLeading?.let { leading ->
                                            { leading(option) }
                                        },
                                        shapes = MenuDefaults.itemShape(
                                            index = index,
                                            count = groupOptions.size,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        onClick = {
            haptics.menuOpened()
            expanded = true
        },
    )
}

@Composable
private fun <T> MiuixChoicePreference(
    title: String,
    value: T,
    groups: List<List<T>>,
    onValueChange: (T) -> Unit,
    modifier: Modifier,
    summary: String?,
    enabled: Boolean,
    optionLabel: (T) -> String,
    optionSummary: ((T) -> String?)?,
    optionEnabled: ((T) -> Boolean)?,
    optionLeading: (@Composable (T) -> Unit)?,
    collapseOnSelection: Boolean,
) {
    fun item(option: T): DropdownItem = DropdownItem(
        text = optionLabel(option),
        summary = optionSummary?.invoke(option),
        enabled = optionEnabled?.invoke(option) ?: true,
        selected = option == value,
        onClick = { onValueChange(option) },
        icon = optionLeading?.let { leading ->
            { iconModifier -> Box(modifier = iconModifier) { leading(option) } }
        },
    )

    if (groups.size == 1 && collapseOnSelection) {
        // 单组且选完即收：经典 spinner 形态，行尾显示当前值。
        val options = groups.first()
        val selectedIndex = options.indexOf(value)
        MiuixWindowSpinnerPreference(
            items = options.map(::item),
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
        return
    }

    // 分组 / 选完不收的形态：选中态由每项的 selected + onClick 自行表达。
    MiuixWindowSpinnerPreference(
        entries = groups.map { group -> DropdownEntry(items = group.map(::item)) },
        title = title,
        modifier = modifier,
        summary = summary,
        enabled = enabled,
        collapseOnSelection = collapseOnSelection,
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
    navigation: Boolean = false,
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
        trailingContent = materialTrailingContent(value, trailing, navigation),
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
    val haptics = rememberMeowHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val updateChecked: (Boolean) -> Unit = { newValue ->
        haptics.toggled(newValue)
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
    val haptics = rememberMeowHaptics()
    MaterialSegmentedListItem(
        checked = checked,
        onCheckedChange = { newValue ->
            haptics.toggled(newValue)
            onCheckedChange(newValue)
        },
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
    onClick: (() -> Unit)?,
    defaultValue: Float?,
    showSteps: Boolean,
) {
    val haptics = rememberMeowHaptics()
    val sliderHaptic = rememberSliderHaptic(value, valueRange)
    // 分档滑条已落在档上，只有连续滑条需要吸附。
    val snapToDefault: (Float) -> Float = { newValue ->
        val span = valueRange.endInclusive - valueRange.start
        if (defaultValue != null && steps == 0 && abs(newValue - defaultValue) <= span * SliderMagnetThreshold) {
            defaultValue
        } else {
            newValue
        }
    }
    val defaultColors = MaterialSliderDefaults.colors()
    val sliderColors = if (showSteps) {
        defaultColors
    } else {
        defaultColors.copy(
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
            disabledActiveTickColor = Color.Transparent,
            disabledInactiveTickColor = Color.Transparent,
        )
    }
    val supportingContent: @Composable () -> Unit = {
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
                onValueChange = { rawValue ->
                    val newValue = snapToDefault(rawValue)
                    sliderHaptic.onValueChange(newValue, valueRange, steps, defaultValue, haptics)
                    onValueChange(newValue)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                onValueChangeFinished = onValueChangeFinished,
                colors = sliderColors,
                steps = steps,
                track = { state ->
                    MaterialSliderDefaults.Track(
                        sliderState = state,
                        modifier = if (defaultValue != null) {
                            Modifier.drawDefaultValueMarker(
                                fraction = (defaultValue - valueRange.start) /
                                    (valueRange.endInclusive - valueRange.start),
                                valueFraction = { state.coercedValueAsFraction },
                                activeColor = if (enabled) {
                                    defaultColors.activeTickColor
                                } else {
                                    defaultColors.disabledActiveTickColor
                                },
                                inactiveColor = if (enabled) {
                                    defaultColors.inactiveTickColor
                                } else {
                                    defaultColors.disabledInactiveTickColor
                                },
                            )
                        } else {
                            Modifier
                        },
                        enabled = enabled,
                        colors = sliderColors,
                    )
                },
                valueRange = valueRange,
            )
        }
    }
    val headline: @Composable () -> Unit = {
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
    if (onClick != null) {
        MaterialSegmentedListItem(
            onClick = onClick,
            shapes = materialPreferenceItemShapes(),
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            supportingContent = supportingContent,
            colors = materialPreferenceItemColors(),
            content = headline,
        )
    } else {
        MaterialSegmentedListItem(
            shapes = materialPreferenceItemShapes(),
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            supportingContent = supportingContent,
            colors = materialPreferenceItemColors(),
            content = headline,
        )
    }
}

/** 在轨道上画默认值标记，大小同 M3 刻度点；已滑过的一侧用活动刻度色。 */
private fun Modifier.drawDefaultValueMarker(
    fraction: Float,
    valueFraction: () -> Float,
    activeColor: Color,
    inactiveColor: Color,
): Modifier = drawWithContent {
    drawContent()
    val rtl = layoutDirection == LayoutDirection.Rtl
    val x = (if (rtl) 1f - fraction else fraction) * size.width
    val covered = fraction <= valueFraction()
    drawCircle(
        color = if (covered) activeColor else inactiveColor,
        radius = 2.dp.toPx(),
        center = Offset(x, size.height / 2f),
    )
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
    navigation: Boolean = false,
): (@Composable () -> Unit)? {
    if (value.isNullOrBlank() && trailing == null && !navigation) return null

    return {
        Row(verticalAlignment = Alignment.CenterVertically) {
            value?.takeIf(String::isNotBlank)?.let {
                MaterialText(
                    text = it,
                    modifier = Modifier.widthAtMostFraction(MaterialValueMaxWidthFraction),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.End,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!value.isNullOrBlank() && trailing != null) {
                Spacer(Modifier.width(12.dp))
            }
            trailing?.invoke()
            if (navigation) {
                // 导航行的 > 箭头：与 miuix ArrowPreference 的右侧箭头对齐。
                MaterialIcon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** 行尾值文本最多占可用宽度的比例，取自 miuix BasicComponent 对 end 区域的上限。 */
private const val MaterialValueMaxWidthFraction = 0.6f

/** 宽度上限取可用宽度的一部分，本身只占内容宽度：长值换行，不挤掉标题。 */
private fun Modifier.widthAtMostFraction(fraction: Float): Modifier = layout { measurable, constraints ->
    val cap = if (constraints.hasBoundedWidth) {
        (constraints.maxWidth * fraction).roundToInt()
    } else {
        constraints.maxWidth
    }
    val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = cap))
    layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
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
    style: TextStyle,
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
