package io.github.lingqiqi5211.meowui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenuGroup as MaterialDropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem as MaterialDropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup as MaterialDropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.LargeFlexibleTopAppBar as MaterialLargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemShapes
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import io.github.lingqiqi5211.meowui.theme.LocalMeowBlurEnabled
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
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
        val cascade: MeowMenuCascade = MeowMenuCascade.Drill,
    ) : MeowTopBarAction
}

/**
 * 子菜单在 Material 分支的展开形态。Miuix 分支不受影响——它一直用自己的原生级联弹窗。
 */
enum class MeowMenuCascade {
    /** 同一弹窗内下钻：子菜单替换主菜单内容，顶部给一行返回上级。 */
    Drill,

    /**
     * 下沉堆叠：主菜单原地下沉压暗，子菜单从父项那一行长出来盖在上面——复刻 miuix
     * 级联弹窗的观感。点遮罩或父项收起。
     */
    Sink,
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
    // 展开状态存的是稳定键（组序:项序）而不是条目对象：对象是展开那一刻的数据快照，
    // 选完后调用侧重建了带新勾选态的 groups，拿旧对象继续渲染子菜单勾选永远不会变；
    // 每轮重组用键从最新 groups 里解析。
    // 归位在打开时而不是关闭时：关闭时归位会让回到主菜单的动画和收起淡出叠在一起播，
    // 收回那一下内容闪一次；冻结原样淡出才平。
    var submenuKey by remember { mutableStateOf<String?>(null) }
    val groups = action.resolvedGroups
    val haptics = rememberMeowHaptics()

    MaterialIconButton(
        onClick = {
            haptics.menuOpened()
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
        // 与 miuix 的 DropdownEntry 分组语义对齐。
        MeowMaterialMenuPopup(
            expanded = expanded,
            // 下沉堆叠展开时，点外部空白（或返回键）先收回一级，再点一次才关掉弹窗——
            // 和 miuix 级联弹窗一致：二级是叠在一级上的一层，不是另一个弹窗。
            onDismissRequest = {
                if (action.cascade == MeowMenuCascade.Sink && submenuKey != null) {
                    submenuKey = null
                } else {
                    expanded = false
                }
            },
        ) {
            when (action.cascade) {
                MeowMenuCascade.Drill -> MaterialMenuDrillContent(
                    groups = groups,
                    submenuKey = submenuKey,
                    collapseOnSelection = action.collapseOnSelection,
                    onSubmenuKeyChange = { submenuKey = it },
                    onCollapsePopup = { expanded = false },
                )

                MeowMenuCascade.Sink -> MaterialMenuSinkContent(
                    groups = groups,
                    submenuKey = submenuKey,
                    collapseOnSelection = action.collapseOnSelection,
                    onSubmenuKeyChange = { submenuKey = it },
                    onCollapsePopup = { expanded = false },
                )
            }
        }
    }
}

/** "组序:项序" → 最新 groups 里的那个子菜单入口；解析不到（分组变了）就当没展开。 */
private fun submenuParent(groups: List<List<MeowMenuItem>>, key: String?): MeowMenuItem? {
    val indices = key?.split(':')?.mapNotNull(String::toIntOrNull)?.takeIf { it.size == 2 }
        ?: return null
    return groups.getOrNull(indices[0])?.getOrNull(indices[1])?.takeIf { it.children.isNotEmpty() }
}

/** 一级/二级共用的分组渲染：分组容器、分段形状、组间距与项间距都在这里。 */
@Composable
private fun MaterialMenuGroups(
    groups: List<List<MeowMenuItem>>,
    collapseOnSelection: Boolean,
    onOpenSubmenu: (groupIndex: Int, itemIndex: Int) -> Unit,
    onCollapsePopup: () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable (shape: Shape) -> Unit)? = null,
    itemModifier: (groupIndex: Int, itemIndex: Int) -> Modifier = { _, _ -> Modifier },
    itemArrowRotation: (groupIndex: Int, itemIndex: Int) -> Float = { _, _ -> 0f },
) {
    Column(modifier = modifier) {
        groups.forEachIndexed { groupIndex, groupItems ->
            if (groupIndex > 0) {
                Spacer(Modifier.height(MenuDefaults.GroupSpacing))
            }
            MaterialDropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                val headerCount = if (groupIndex == 0 && header != null) 1 else 0
                val count = groupItems.size + headerCount
                if (headerCount == 1 && header != null) {
                    header(MenuDefaults.itemShape(index = 0, count = count).shape)
                }
                groupItems.forEachIndexed { index, item ->
                    // 分段项之间的小间隙：相邻选中项各自成 pill，没有间隙时会贴成一整块。
                    if (index + headerCount > 0) {
                        Spacer(Modifier.height(ListItemDefaults.SegmentedGap))
                    }
                    MaterialTopBarMenuItem(
                        item = item,
                        shapes = MenuDefaults.itemShape(
                            index = index + headerCount,
                            count = count,
                        ),
                        modifier = itemModifier(groupIndex, index),
                        arrowRotation = { itemArrowRotation(groupIndex, index) },
                        onOpenSubmenu = { onOpenSubmenu(groupIndex, index) },
                        onPicked = {
                            if (collapseOnSelection) onCollapsePopup()
                            item.onClick()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialMenuDrillContent(
    groups: List<List<MeowMenuItem>>,
    submenuKey: String?,
    collapseOnSelection: Boolean,
    onSubmenuKeyChange: (String?) -> Unit,
    onCollapsePopup: () -> Unit,
) {
    // 下钻子菜单时内容滑动切换并平滑变尺寸：瞬间换内容会让弹窗尺寸跳变、锚点重算后
    // 整个弹窗跳位置。
    // 下钻时锁定主菜单宽度：过渡中两级内容同时在组合，弹窗被较宽一级撑着，动画结束旧
    // 内容移除后宽度瞬间收缩、锚点重算——整个弹窗在收尾那一帧跳位。两级同宽后宽度
    // 全程恒定，只剩高度顶锚平滑变化。
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
        val parent = submenuParent(groups, drillKey)
        MaterialMenuGroups(
            groups = parent?.let { listOf(it.children) } ?: groups,
            collapseOnSelection = collapseOnSelection,
            // Material 只支持一层下钻：子菜单里的带 children 项不再作为下钻入口。
            onOpenSubmenu = { groupIndex, itemIndex ->
                if (parent == null) onSubmenuKeyChange("$groupIndex:$itemIndex")
            },
            onCollapsePopup = onCollapsePopup,
            modifier = if (menuWidthPx > 0) {
                Modifier.width(with(density) { menuWidthPx.toDp() })
            } else {
                Modifier.onSizeChanged { size ->
                    if (drillKey == null) menuWidthPx = size.width
                }
            },
            header = parent?.let { item ->
                { shape ->
                    MaterialDropdownMenuItem(
                        onClick = { onSubmenuKeyChange(null) },
                        text = { MaterialText(item.text) },
                        shape = shape,
                        leadingIcon = {
                            MaterialIcon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = null,
                            )
                        },
                    )
                }
            },
        )
    }
}

/**
 * 下沉堆叠：复刻 miuix 级联弹窗的形态。
 *
 * 整只弹窗只有一张卡、一层阴影。二级不是叠上去的第二张卡片，而是同一个面上的一块：它的
 * 表面被裁进一条从**父项那一行的矩形**长到**完整二级矩形**的圆角路径里，内容随裁剪露出来。
 *
 * 三个容易做歪的地方，都照 miuix 的做法：
 *
 * - **一级不是被压了一层遮罩**，而是自己"沉进背景"：内容整体降低不透明度，露出下面弹窗
 *   本身的表面色，于是被盖住的部分和背景同色，而不是多出一块带边界的灰。
 * - **头行与锚点行严丝合缝**：头行就是父项自己，用 [Layout] 把行高从锚点行的实测高度插值
 *   到固有高度，并按二级面板里的实际偏移反向落位，所以展开那一刻它正好压在原来那一行上，
 *   不会先跳一下再展开。箭头只做旋转，且走自己更快的弹簧。
 * - **缩放绕弹窗的生长原点**：顶栏菜单从末端的图标底下长出来，一级下沉也要朝那个点收，
 *   绕中心缩会让它在展开时横向漂移。
 */
@Composable
private fun MaterialMenuSinkContent(
    groups: List<List<MeowMenuItem>>,
    submenuKey: String?,
    collapseOnSelection: Boolean,
    onSubmenuKeyChange: (String?) -> Unit,
    onCollapsePopup: () -> Unit,
) {
    var displayedKey by remember { mutableStateOf<String?>(null) }
    if (submenuKey != null && submenuKey != displayedKey) displayedKey = submenuKey
    val expanded = submenuKey != null
    // miuix 用的是 folme 弹簧而不是定时曲线：展开 response 0.45、收起 0.2，阻尼近临界不回弹。
    // 折算成 Compose 刚度是 (2π/response)²——收起明显更快，这一慢一快是手感的来源。
    // 箭头另走一条更快的弹簧（response 0.2 / 0.3），旋转不跟着面板的形变拖拍子。
    val expandFraction by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = SinkSpringDamping,
            stiffness = if (expanded) SinkExpandStiffness else SinkCollapseStiffness,
            visibilityThreshold = SinkSpringThreshold,
        ),
        label = "MeowTopBarMenuSink",
    )
    val arrowFraction by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = SinkSpringDamping,
            stiffness = if (expanded) SinkArrowExpandStiffness else SinkArrowCollapseStiffness,
            visibilityThreshold = SinkSpringThreshold,
        ),
        label = "MeowTopBarMenuSinkArrow",
    )
    if (expandFraction == 0f && !expanded && displayedKey != null) displayedKey = null

    // 锚点：父项那一行在一级里的位置、高度，以及它在自己分组里的形状——二级的头行要顶着
    // 这一行落位，形状不一致的话展开第一帧会露出一圈圆角。
    val anchors = remember { mutableStateMapOf<String, SinkAnchor>() }
    var primaryTop by remember { mutableIntStateOf(0) }
    // 头行在二级面板里的偏移（分组容器自己的内边距），面板要往上挪这么多才对得齐。
    var headerOffset by remember { mutableIntStateOf(0) }
    val parent = submenuParent(groups, displayedKey)
    val anchor = displayedKey?.let { anchors[it] }
    // 展开的是弹窗全局第一行时，二级从面板顶端起、盖住整张卡（下面把它的最小高度撑到一级
    // 那么高），一级的缩放与压暗就没有意义——上游那边也是这个视觉结果：miuix 的一级照样缩，
    // 但二级按 union rect 测量，第一行展开时它整块盖住，缩和压暗都看不见。空分组要跳过，
    // 否则"第一行"会算到一个没有行的分组上。
    val firstNonEmptyGroup = remember(groups) { groups.indexOfFirst { it.isNotEmpty() } }
    val anchorIsPopupFirst = displayedKey != null && displayedKey == "$firstNonEmptyGroup:0"
    val sinkFraction = if (anchorIsPopupFirst) 0f else expandFraction
    val anchorTop = anchor?.top ?: 0
    val anchorHeight = anchor?.height ?: 0
    val cornerPx = with(LocalDensity.current) { SinkCornerRadius.toPx() }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Layout(
        content = {
            Box {
                MaterialMenuGroups(
                    groups = groups,
                    collapseOnSelection = collapseOnSelection,
                    onOpenSubmenu = { groupIndex, itemIndex ->
                        val key = "$groupIndex:$itemIndex"
                        onSubmenuKeyChange(if (key == submenuKey) null else key)
                    },
                    onCollapsePopup = onCollapsePopup,
                    modifier = Modifier
                        .onGloballyPositioned { primaryTop = it.positionInWindow().y.roundToInt() }
                        .graphicsLayer {
                            // 朝弹窗生长的那个角收（顶栏菜单在末端图标底下），不是绕中心。
                            transformOrigin = TransformOrigin(if (rtl) 0f else 1f, 0f)
                            val sink = 1f - (1f - SinkShrunkScale) * sinkFraction
                            scaleX = sink
                            scaleY = sink
                        }
                        // 弱化靠压深卡片底色，不用模糊也不铺遮罩：SrcAtop 只染一级实际画到
                        // 的像素（卡片本身），分组之间的空隙和圆角外面都不受影响，所以不会
                        // 出现一块带边界的暗方块。
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                color = Color.Black,
                                alpha = SinkDarkenAmount * sinkFraction,
                                blendMode = BlendMode.SrcAtop,
                            )
                        },
                    itemArrowRotation = { groupIndex, itemIndex ->
                        if ("$groupIndex:$itemIndex" == displayedKey) {
                            -SinkArrowDegrees * arrowFraction
                        } else {
                            0f
                        }
                    },
                    itemModifier = { groupIndex, itemIndex ->
                        val key = "$groupIndex:$itemIndex"
                        Modifier.onGloballyPositioned { coordinates ->
                            val anchor = SinkAnchor(
                                top = coordinates.positionInWindow().y.roundToInt() - primaryTop,
                                height = coordinates.size.height,
                                index = itemIndex,
                                count = groups[groupIndex].size,
                            )
                            // 每趟布局都会回调；没变就不写，免得白白触发一轮重组。
                            if (anchors[key] != anchor) anchors[key] = anchor
                        }
                    },
                )
                if (expandFraction > 0f) {
                    // 透明的挡板：展开期间一级不可点，点它就是收回二级。没有底色——压暗
                    // 已经由上面的 alpha 完成了。
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = { onSubmenuKeyChange(null) },
                            ),
                    )
                }
            }
            if (parent != null && anchor != null) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            // 收回的最后几帧：裁剪只剩锚点行那么高，但它仍然实心盖在卡片上，
                            // 归零那一刻整块摘掉就会"啪"地消失。末段补一段淡出，摘除时它
                            // 已经透明，看不出接缝。
                            alpha = (expandFraction / SinkFadeOutHead).coerceAtMost(1f)
                        }
                        .drawWithCache {
                        val path = Path()
                        onDrawWithContent {
                            // 从锚点行的矩形长到整块面板：上沿从头行位置升到 0，下沿从行底
                            // 落到面板底，圆角同步张开。第一帧正好等于那一行，所以没有跳变。
                            val fraction = expandFraction
                            val top = lerp(headerOffset.toFloat(), 0f, fraction)
                            val bottom = lerp(
                                (headerOffset + anchorHeight).toFloat(),
                                size.height,
                                fraction,
                            )
                            val radius = CornerRadius(cornerPx * fraction, cornerPx * fraction)
                            path.rewind()
                            path.addRoundRect(
                                RoundRect(
                                    left = 0f,
                                    top = top,
                                    right = size.width,
                                    bottom = bottom,
                                    topLeftCornerRadius = radius,
                                    topRightCornerRadius = radius,
                                    bottomRightCornerRadius = radius,
                                    bottomLeftCornerRadius = radius,
                                ),
                            )
                            clipPath(path) { this@onDrawWithContent.drawContent() }
                        }
                    },
                ) {
                    // 分组容器由 M3 自己画：底色、内边距、分段形状都和一级一致，而且不透明
                    // ——一级就在正下方，透一点它的边缘就会露出来。
                    MaterialDropdownMenuGroup(shapes = MenuDefaults.groupShapes()) {
                        val count = parent.children.size + 1
                        // 头行就是父项自己，形状取锚点行在一级里的那一个，展开第一帧完全重合。
                        MaterialDropdownMenuItem(
                            onClick = { onSubmenuKeyChange(null) },
                            // 箭头不走 trailing 槽：那个槽紧跟在文字后面，一级的箭头却是
                            // 贴右缘的，两者位置一差，展开那一刻箭头就横跳一格。这里用和
                            // 一级完全相同的排布（text 槽内两端对齐），只让它原地旋转。
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MaterialText(
                                        text = parent.text,
                                        modifier = Modifier.weight(1f, fill = false),
                                    )
                                    MaterialIcon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(start = 12.dp)
                                            .graphicsLayer {
                                                // 逆时针：右箭头转成朝上，指回它展开出来的那一行。
                                                rotationZ = -SinkArrowDegrees * arrowFraction
                                            },
                                    )
                                }
                            },
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                headerOffset = coordinates.positionInParent().y.roundToInt()
                            },
                            shape = MenuDefaults.itemShape(
                                index = anchor.index,
                                count = anchor.count,
                            ).shape,
                        )
                        parent.children.forEachIndexed { index, child ->
                            Spacer(Modifier.height(ListItemDefaults.SegmentedGap))
                            MaterialTopBarMenuItem(
                                item = child,
                                shapes = MenuDefaults.itemShape(index = index + 1, count = count),
                                onOpenSubmenu = {},
                                onPicked = {
                                    if (collapseOnSelection) onCollapsePopup()
                                    child.onClick()
                                },
                            )
                        }
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val primary = measurables[0].measure(constraints)
        // 二级与一级同宽：宽度一变，弹窗就要重算锚点并跳位置。
        val secondary = measurables.getOrNull(1)?.measure(
            constraints.copy(
                minWidth = primary.width,
                maxWidth = primary.width,
                // 第一行展开时二级要盖住整张卡（上游按 union rect 测量二级，同一个意思）。
                minHeight = if (anchorIsPopupFirst) {
                    primary.height.coerceAtMost(constraints.maxHeight)
                } else {
                    constraints.minHeight
                },
            ),
        )
        // 头行要压在锚点行上，所以面板整体上移它在面板里的偏移。
        val secondaryY = (anchorTop - headerOffset).coerceAtLeast(0)
        val secondaryBottom = if (secondary == null) 0 else secondaryY + secondary.height
        layout(primary.width, maxOf(primary.height, secondaryBottom)) {
            primary.place(0, 0)
            secondary?.place(0, secondaryY)
        }
    }
}

/** 锚点行的实测几何与它在自己分组里的位置——二级的头行靠这些和它对齐。 */
private data class SinkAnchor(
    val top: Int,
    val height: Int,
    val index: Int,
    val count: Int,
)

/** ✓ 淡入淡出的时长：够短不拖慢点击反馈，够长能被看见。 */
private val CheckMarkAnimationSpec = tween<Float>(durationMillis = 150, easing = EaseOutCubic)

/** ✓ 出现时的起始缩放；不从 0 开始，否则末尾一段几乎看不见，只剩突然出现。 */
private const val CheckMarkScaleFrom = 0.7f

@Composable
private fun MaterialTopBarMenuItem(
    item: MeowMenuItem,
    shapes: MenuItemShapes,
    onOpenSubmenu: () -> Unit,
    onPicked: () -> Unit,
    modifier: Modifier = Modifier,
    arrowRotation: () -> Float = { 0f },
) {
    val haptics = rememberMeowHaptics()
    // 带 selected 的 Expressive 重载：选中态背景与形状由组件自己处理，对应 miuix 的 ✓。
    // ✓ / 子菜单箭头不走 trailing 槽：那个槽紧跟在文字后面，短标题的行里
    // 标记会浮在行中间；自己在 text 槽里两端对齐，标记恒定贴行右缘。
    MaterialDropdownMenuItem(
        selected = item.selected == true,
        modifier = modifier,
        onClick = {
            if (item.children.isNotEmpty()) {
                onOpenSubmenu()
            } else {
                // 与 miuix 弹窗选项一致：选中一项响 Confirm。
                haptics.picked()
                onPicked()
            }
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
                        modifier = Modifier
                            .padding(start = 12.dp)
                            // 下沉堆叠展开时这一行的箭头也在转：二级被摘掉的那一刻，底下
                            // 这个箭头必须已经停在同一角度，否则就是一次瞬跳。
                            .graphicsLayer { rotationZ = arrowRotation() },
                    )
                    // 可选中项恒定保留 ✓ 槽位（未选中时隐形）：否则切换选中会
                    // 改变行宽，不收起的菜单每点一下弹窗就重排跳位。
                    //
                    // 也正因为槽位恒定，切换时没有任何位移可以带眼睛，一帧硬切 alpha
                    // 看着像闪一下而不像"选中变了"；不收起的菜单里标记要当着用户的面
                    // 从一行挪到另一行，所以淡入淡出，并带一点缩放让它是"长出来"的。
                    item.selected != null -> {
                        val selectedProgress by animateFloatAsState(
                            targetValue = if (item.selected == true) 1f else 0f,
                            animationSpec = CheckMarkAnimationSpec,
                            label = "menuItemCheck",
                        )
                        MaterialIcon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .graphicsLayer {
                                    alpha = selectedProgress
                                    val scale = CheckMarkScaleFrom +
                                        (1f - CheckMarkScaleFrom) * selectedProgress
                                    scaleX = scale
                                    scaleY = scale
                                },
                        )
                    }
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
    // miuix 的级联弹窗自己不响；照它的下拉入口（Spinner / DropdownMenu）与下拉弹窗的做法：
    // 展开响 ContextClick，选中一项响 Confirm。
    val haptics = rememberMeowHaptics()
    val entries = remember(groups, haptics) {
        groups.map { group ->
            DropdownEntry(items = group.map { it.toMiuixDropdownItem(haptics) })
        }
    }
    val collapseOnSelection = action.collapseOnSelection

    var expanded by remember { mutableStateOf(false) }
    MiuixIconButton(
        onClick = {
            haptics.menuOpened()
            expanded = true
        },
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
            collapseOnSelection = collapseOnSelection,
        )
    }
}

/**
 * 递归转换为 miuix DropdownItem;children 非空时由 miuix 原生级联弹窗堆叠展开。
 * 叶子项被点中时先响一下再回调；子菜单入口的 onClick 由 miuix 忽略，不用包。
 */
private fun MeowMenuItem.toMiuixDropdownItem(haptics: MeowHaptics): DropdownItem {
    val onPicked = onClick
    return DropdownItem(
        text = text,
        summary = summary,
        enabled = enabled,
        selected = selected == true,
        onClick = {
            haptics.picked()
            onPicked()
        },
        icon = icon?.let { image ->
            { modifier ->
                MiuixIcon(
                    imageVector = image,
                    contentDescription = null,
                    modifier = modifier,
                )
            }
        },
        children = children.takeIf { it.isNotEmpty() }?.map { it.toMiuixDropdownItem(haptics) },
    )
}

private const val MenuDrillFadeMillis = 120

private const val MenuDismissMillis = 220

// 下沉堆叠。缩放比例与 miuix 的 PRIMARY_SHRUNK_SCALE 一致；刚度由它的 folme response
// 折算：(2π/0.45)² ≈ 195、(2π/0.2)² ≈ 987、(2π/0.3)² ≈ 439。
private const val SinkShrunkScale = 0.95f
private const val SinkDarkenAmount = 0.14f
private const val SinkArrowDegrees = 90f
private const val SinkSpringDamping = 0.99f
private const val SinkExpandStiffness = 195f
private const val SinkCollapseStiffness = 987f
private const val SinkArrowExpandStiffness = 987f
private const val SinkArrowCollapseStiffness = 439f
private const val SinkSpringThreshold = 0.001f
// 收回末段补淡出的区间：形变到这个进度以下才开始透明，摘除时已看不见。
private const val SinkFadeOutHead = 0.25f
private val SinkCornerRadius = 16.dp

