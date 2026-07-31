package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.material3.ListItemDefaults
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.scaleOut
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.DropdownMenuGroup as MaterialDropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem as MaterialDropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup as MaterialDropdownMenuPopup
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.LargeFlexibleTopAppBar as MaterialLargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import io.github.lingqiqi5211.meowui.theme.LocalMeowBlurEnabled
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayCascadingListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * A top bar action.
 *
 * The bar renders these itself in the active style, so [modifier] is how the call
 * site reaches the resulting control — to tag it for tests, or to constrain it.
 * It is applied to the button each style builds, not to a wrapper, so semantics
 * merge onto the same node that carries the click.
 */
sealed interface MeowTopBarAction {
    val enabled: Boolean

    /** The modifier applied to the control this action renders as. */
    val modifier: Modifier

    data class Icon(
        val icon: ImageVector,
        val contentDescription: String,
        override val modifier: Modifier = Modifier,
        override val enabled: Boolean = true,
        val onClick: () -> Unit,
    ) : MeowTopBarAction

    data class Text(
        val text: String,
        override val modifier: Modifier = Modifier,
        override val enabled: Boolean = true,
        val onClick: () -> Unit,
    ) : MeowTopBarAction

    /**
     * 图标 + 弹窗菜单。
     *
     * [groups] 非空时每个子列表是一个视觉分组（组间有分隔），[items] 被忽略；
     * 否则 [items] 即单组。[collapseOnSelection] 为 false 时点选条目不收起菜单，
     * 用于单选/多选式菜单（条目带 [MeowMenuItem.selected]）；子菜单入口不受影响。
     */
    data class Menu(
        val icon: ImageVector,
        val contentDescription: String,
        val items: List<MeowMenuItem> = emptyList(),
        override val modifier: Modifier = Modifier,
        override val enabled: Boolean = true,
        val groups: List<List<MeowMenuItem>> = emptyList(),
        val collapseOnSelection: Boolean = true,
    ) : MeowTopBarAction
}

/** 菜单的分组视图：[Menu.groups] 优先，否则 [Menu.items] 作为单组。 */
private val MeowTopBarAction.Menu.resolvedGroups: List<List<MeowMenuItem>>
    get() = groups.filter { it.isNotEmpty() }.ifEmpty {
        if (items.isEmpty()) emptyList() else listOf(items)
    }

/**
 * 菜单项。[children] 非空时该项成为子菜单入口（点击展开下一级，[onClick] 被忽略）：
 * Miuix 使用原生级联弹窗堆叠展开，Material 在同一弹窗内下钻并提供返回上级的行。
 */
data class MeowMenuItem(
    val text: String,
    val icon: ImageVector? = null,
    val summary: String? = null,
    val enabled: Boolean = true,
    /** null = 普通动作项；非 null = 可选中项，true 时行尾显示选中标记。 */
    val selected: Boolean? = null,
    val onClick: () -> Unit = {},
    val children: List<MeowMenuItem> = emptyList(),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MeowTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    onBackClick: (() -> Unit)? = null,
    navigationModifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actionItems: List<MeowTopBarAction> = emptyList(),
) {
    val effect = LocalMeowScaffoldEffect.current
    val scrollContext = LocalMeowScrollContext.current
    // 主题开启模糊且顶栏在 MeowScaffold 里(拿得到内容图层快照)时,顶栏变成磨砂:
    // 自身底色透明,由背景模糊 + 半透明底色承担容器色。内容需要滚到顶栏下方才看
    // 得出效果,由页面把 scaffold 顶部内边距交给滚动容器实现。
    val backdrop = LocalMeowBackdrop.current
    val useBlur = LocalMeowBlurEnabled.current &&
        backdrop != null &&
        isRuntimeShaderSupported()
    val effectModifier = modifier.then(effect.topBarModifier)
    // navigationIcon 优先；只给 onBackClick 时渲染风格原生的返回按钮。
    // navigationModifier 只作用于内置返回按钮；自定义 navigationIcon 由调用侧自己带 modifier。
    val resolvedNavigationIcon: (@Composable () -> Unit)? = navigationIcon
        ?: onBackClick?.let { back ->
            { MeowBackIconButton(onClick = back, modifier = navigationModifier) }
        }

    MeowStyleContent(
        materialExpressive = {
            val baseColor = effect.topBarContainerColor
                ?: MaterialTheme.colorScheme.surfaceContainer
            val containerColor = if (useBlur) Color.Transparent else baseColor
            val colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                scrolledContainerColor = containerColor,
            )
            val barModifier = if (useBlur && backdrop != null) {
                effectModifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { RectangleShape },
                    effects = { blur(MeowBlurRadius.toPx(), MeowBlurRadius.toPx()) },
                    onDrawSurface = { drawRect(baseColor.copy(alpha = MeowBlurSurfaceAlpha)) },
                )
            } else {
                effectModifier
            }

            MaterialLargeFlexibleTopAppBar(
                title = { MaterialText(title) },
                modifier = barModifier,
                subtitle = subtitle.takeIf(String::isNotBlank)?.let { text ->
                    { MaterialText(text) }
                },
                navigationIcon = { resolvedNavigationIcon?.invoke() },
                actions = {
                    actionItems.forEach { MaterialTopBarAction(it) }
                },
                colors = colors,
                scrollBehavior = scrollContext.materialTopBar,
            )
        },
        miuix = {
            val baseColor = effect.topBarContainerColor ?: MiuixTheme.colorScheme.surface
            val barModifier = if (useBlur && backdrop != null) {
                effectModifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { RectangleShape },
                    effects = { blur(MeowBlurRadius.toPx(), MeowBlurRadius.toPx()) },
                    onDrawSurface = { drawRect(baseColor.copy(alpha = MeowBlurSurfaceAlpha)) },
                )
            } else {
                effectModifier
            }
            MiuixTopAppBar(
                title = title,
                modifier = barModifier,
                color = if (useBlur) Color.Transparent else baseColor,
                subtitle = subtitle,
                navigationIcon = { resolvedNavigationIcon?.invoke() },
                actions = {
                    actionItems.forEach { MiuixTopBarAction(it) }
                },
                scrollBehavior = scrollContext.miuixTopBar,
            )
        },
    )
}

@Composable
internal fun MeowBackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialIconButton(onClick = onClick, modifier = modifier) {
                MaterialIcon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        miuix = {
            MiuixIconButton(onClick = onClick, modifier = modifier) {
                MiuixIcon(
                    imageVector = MiuixIcons.Back,
                    contentDescription = "Back",
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
        },
    )
}

@Composable
private fun MaterialTopBarAction(action: MeowTopBarAction) {
    when (action) {
        is MeowTopBarAction.Icon -> {
            MaterialIconButton(
                onClick = action.onClick,
                modifier = action.modifier,
                enabled = action.enabled,
            ) {
                MaterialIcon(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                )
            }
        }

        is MeowTopBarAction.Text -> {
            MaterialTextButton(
                onClick = action.onClick,
                modifier = action.modifier,
                enabled = action.enabled,
            ) {
                MaterialText(action.text)
            }
        }

        is MeowTopBarAction.Menu -> MaterialTopBarMenu(action)
    }
}

/**
 * MD3E 菜单弹窗的共享外壳：收起动画自己做。
 *
 * M3 的 DropdownMenuPopup 不暴露动画参数，退场只有百来毫秒，观感像闪断。
 * 这里让弹窗在收起时先播放内容层的缩放淡出（时长自定），播完才把 expanded
 * 交还给 M3——它那段快速退场作用在已经透明的内容上，等于无感。展开沿用
 * M3 自己的入场，内容层不再叠一次。
 */
@Composable
internal fun MeowMaterialMenuPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val exitState = remember { MutableTransitionState(false) }
    exitState.targetState = expanded
    val popupVisible = expanded || exitState.currentState || !exitState.isIdle
    MaterialDropdownMenuPopup(
        expanded = popupVisible,
        onDismissRequest = onDismissRequest,
    ) {
        AnimatedVisibility(
            visibleState = exitState,
            enter = EnterTransition.None,
            exit = fadeOut(tween(MenuDismissMillis)) +
                scaleOut(targetScale = 0.92f, animationSpec = tween(MenuDismissMillis)),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun MaterialTopBarMenu(action: MeowTopBarAction.Menu) {
    var expanded by remember { mutableStateOf(false) }
    // Material 无原生级联弹窗:子菜单在同一弹窗内下钻,顶部提供返回上级的行。
    // 下钻状态存的是稳定键（组序:项序）而不是条目对象：对象是下钻那一刻的数据
    // 快照，选完后调用侧重建了带新勾选态的 groups，拿旧对象继续渲染子菜单
    // 勾选永远不会变；每轮重组用键从最新 groups 里解析。
    // 归位在打开时而不是关闭时：关闭时归位会让返回主菜单的下钻动画
    // 和收起淡出叠在一起播，收回那一下内容闪一次；冻结原样淡出才平。
    var submenuKey by remember { mutableStateOf<String?>(null) }
    val groups = action.resolvedGroups

    MaterialIconButton(
        onClick = {
            submenuKey = null
            expanded = true
        },
        modifier = action.modifier,
        enabled = action.enabled && groups.isNotEmpty(),
    ) {
        MaterialIcon(
            imageVector = action.icon,
            contentDescription = action.contentDescription,
        )
        // Expressive 菜单:每个分组一个大圆角容器 + 按位置计算的分段 item 形状，
        // 与 miuix 的 DropdownEntry 分组语义对齐。下钻子菜单时只展示该子菜单。
        MeowMaterialMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            // 下钻子菜单时内容滑动切换并平滑变尺寸：瞬间换内容会让弹窗尺寸跳变、
            // 锦点重算后整个弹窗跳位置。
            // 下钻时锁定主菜单宽度：过渡中两级内容同时在组合，弹窗被较宽一级
            // 撑着，动画结束旧内容移除后宽度瞬间收缩、锚点重算——整个弹窗
            // 在收尾那一帧跳位。两级同宽后宽度全程恒定，只剩高度顶锚平滑变化。
            val density = LocalDensity.current
            var menuWidthPx by remember { mutableStateOf(0) }
            AnimatedContent(
                targetState = submenuKey,
                transitionSpec = {
                    val forward = targetState != null
                    val slide = if (forward) 1 else -1
                    (
                        slideInHorizontally { it / 4 * slide } + fadeIn(tween(MenuDrillFadeMillis))
                        ) togetherWith (
                        slideOutHorizontally { -it / 4 * slide } + fadeOut(tween(MenuDrillFadeMillis))
                        ) using SizeTransform(clip = false)
                },
                label = "MeowTopBarMenuDrill",
            ) { drillKey ->
                // 键是 "组序:项序"：每轮重组从最新 groups 解析，勾选态永远新鲜；
                // 按位置而不按文本，同名条目不会串台。
                val parent = drillKey?.let { key ->
                    val (groupIndex, itemIndex) = key.split(':').map(String::toInt)
                    groups.getOrNull(groupIndex)?.getOrNull(itemIndex)
                        ?.takeIf { it.children.isNotEmpty() }
                }
                Column(
                    modifier = if (menuWidthPx > 0) {
                        Modifier.width(with(density) { menuWidthPx.toDp() })
                    } else {
                        Modifier.onSizeChanged { size ->
                            if (drillKey == null) menuWidthPx = size.width
                        }
                    },
                ) {
                    val visibleGroups = parent?.let { listOf(it.children) } ?: groups
                    visibleGroups.forEachIndexed { groupIndex, groupItems ->
                        if (groupIndex > 0) {
                            Spacer(Modifier.height(MenuDefaults.GroupSpacing))
                        }
                        MaterialDropdownMenuGroup(
                            shapes = MenuDefaults.groupShapes(),
                        ) {
                            val headerCount = if (parent != null) 1 else 0
                            val count = groupItems.size + headerCount
                            parent?.let {
                                MaterialDropdownMenuItem(
                                    onClick = { submenuKey = null },
                                    text = { MaterialText(it.text) },
                                    shape = MenuDefaults.itemShape(index = 0, count = count).shape,
                                    leadingIcon = {
                                        MaterialIcon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                            contentDescription = null,
                                        )
                                    },
                                )
                            }
                            groupItems.forEachIndexed { index, item ->
                                // 分段项之间的小间隙：相邻选中项各自成 pill，
                                // 没有间隙时会贴成一整块。
                                if (index + headerCount > 0) {
                                    Spacer(Modifier.height(ListItemDefaults.SegmentedGap))
                                }
                                MaterialTopBarMenuItem(
                                    item = item,
                                    shapes = MenuDefaults.itemShape(
                                        index = index + headerCount,
                                        count = count,
                                    ),
                                    // Material 只支持一层下钻：子菜单里的带 children 项
                                    // 不再作为下钻入口（indexOf 在子视图里也拿不到组序）。
                                    onOpenSubmenu = {
                                        val groupIndex = groups.indexOf(groupItems)
                                        if (groupIndex >= 0) {
                                            submenuKey = "$groupIndex:$index"
                                        }
                                    },
                                    onPicked = {
                                        if (action.collapseOnSelection) expanded = false
                                        item.onClick()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialTopBarMenuItem(
    item: MeowMenuItem,
    shapes: androidx.compose.material3.MenuItemShapes,
    onOpenSubmenu: () -> Unit,
    onPicked: () -> Unit,
) {
    // 带 selected 的 Expressive 重载：选中态背景与形状由组件自己处理，对应 miuix 的 ✓。
    // ✓ / 子菜单箭头不走 trailing 槽：那个槽紧跟在文字后面，短标题的行里
    // 标记会浮在行中间；自己在 text 槽里两端对齐，标记恒定贴行右缘。
    MaterialDropdownMenuItem(
        selected = item.selected == true,
        onClick = {
            if (item.children.isNotEmpty()) onOpenSubmenu() else onPicked()
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    MaterialText(item.text)
                    item.summary?.takeIf(String::isNotBlank)?.let { summary ->
                        MaterialText(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                when {
                    item.children.isNotEmpty() -> MaterialIcon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                    // 可选中项恒定保留 ✓ 槽位（未选中时隐形）：否则切换选中会
                    // 改变行宽，不收起的菜单每点一下弹窗就重排跳位。
                    item.selected != null -> MaterialIcon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .graphicsLayer {
                                alpha = if (item.selected == true) 1f else 0f
                            },
                    )
                }
            }
        },
        shapes = shapes,
        enabled = item.enabled,
        leadingIcon = item.icon?.let { icon ->
            {
                MaterialIcon(
                    imageVector = icon,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun MiuixTopBarAction(action: MeowTopBarAction) {
    when (action) {
        is MeowTopBarAction.Icon -> {
            MiuixIconButton(
                onClick = action.onClick,
                modifier = action.modifier,
                enabled = action.enabled,
            ) {
                MiuixIcon(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                )
            }
        }

        is MeowTopBarAction.Text -> {
            MiuixTextButton(
                text = action.text,
                onClick = action.onClick,
                modifier = action.modifier,
                enabled = action.enabled,
            )
        }

        is MeowTopBarAction.Menu -> MiuixTopBarMenu(action)
    }
}

@Composable
private fun MiuixTopBarMenu(action: MeowTopBarAction.Menu) {
    val groups = action.resolvedGroups
    val entries = remember(groups) {
        groups.map { group -> DropdownEntry(items = group.map { it.toMiuixDropdownItem() }) }
    }

    // 级联需要 CascadingListPopup;OverlayIconDropdownMenu 只渲染平铺列表,会忽略 children。
    var expanded by remember { mutableStateOf(false) }
    MiuixIconButton(
        onClick = { expanded = true },
        modifier = action.modifier,
        enabled = action.enabled && groups.isNotEmpty(),
    ) {
        MiuixIcon(
            imageVector = action.icon,
            contentDescription = action.contentDescription,
        )
        OverlayCascadingListPopup(
            show = expanded,
            entries = entries,
            onDismissRequest = { expanded = false },
            renderInRootScaffold = false,
            collapseOnSelection = action.collapseOnSelection,
        )
    }
}

/** 递归转换为 miuix DropdownItem;children 非空时由 miuix 原生级联弹窗堆叠展开。 */
private fun MeowMenuItem.toMiuixDropdownItem(): DropdownItem = DropdownItem(
    text = text,
    summary = summary,
    enabled = enabled,
    selected = selected == true,
    onClick = onClick,
    icon = icon?.let { image ->
        { modifier ->
            MiuixIcon(
                imageVector = image,
                contentDescription = null,
                modifier = modifier,
            )
        }
    },
    children = children.takeIf { it.isNotEmpty() }?.map { it.toMiuixDropdownItem() },
)

private const val MenuDrillFadeMillis = 120

private const val MenuDismissMillis = 220
