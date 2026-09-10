package io.github.lingqiqi5211.meowui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Badge as MaterialBadge
import androidx.compose.material3.BadgedBox as MaterialBadgedBox
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.material3.ShortNavigationBar as MaterialShortNavigationBar
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.ShortNavigationBarItem as MaterialShortNavigationBarItem
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.ToggleButton as MaterialToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.LocalMeowBlurEnabled
import io.github.lingqiqi5211.meowui.theme.LocalMeowDarkTheme
import io.github.lingqiqi5211.meowui.theme.MeowBlur
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.anim.folmeSpring
import top.yukonga.miuix.kmp.basic.Badge as MiuixBadge
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail as MiuixNavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailDefaults as MiuixNavigationRailDefaults
import top.yukonga.miuix.kmp.basic.NavigationRailItem as MiuixNavigationRailItem
import top.yukonga.miuix.kmp.basic.NavigationRailValue as MiuixNavigationRailValue
import top.yukonga.miuix.kmp.basic.rememberNavigationRailState as rememberMiuixNavigationRailState
import top.yukonga.miuix.kmp.basic.TabRow as MiuixTabRow
import top.yukonga.miuix.kmp.basic.TabRowWithContour as MiuixTabRowWithContour
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class MeowTabRowStyle {
    Standard,
    Contour,
}

enum class MeowNavigationBarStyle {
    Standard,
    Floating,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MeowTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: MeowTabRowStyle = MeowTabRowStyle.Standard,
) {
    if (tabs.isEmpty()) return
    require(selectedIndex in tabs.indices) {
        "selectedIndex must point to an item in tabs"
    }

    MeowStyleContent(
        materialExpressive = {
            Row(
                modifier = modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(
                    ButtonGroupDefaults.ConnectedSpaceBetween,
                ),
            ) {
                tabs.forEachIndexed { index, title ->
                    MaterialToggleButton(
                        checked = index == selectedIndex,
                        onCheckedChange = { checked ->
                            if (checked) onTabSelected(index)
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                            .semantics { role = Role.RadioButton },
                        shapes = when {
                            tabs.size == 1 -> ToggleButtonShapes(
                                shape = ToggleButtonDefaults.shape,
                                pressedShape = ToggleButtonDefaults.pressedShape,
                                checkedShape = ToggleButtonDefaults.checkedShape,
                            )
                            index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            index == tabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                    ) {
                        MaterialText(title)
                    }
                }
            }
        },
        miuix = {
            when (style) {
                MeowTabRowStyle.Standard -> {
                    MiuixTabRow(
                        tabs = tabs,
                        selectedTabIndex = selectedIndex,
                        onTabSelected = onTabSelected,
                        modifier = modifier,
                        minWidth = 84.dp,
                        maxWidth = 132.dp,
                        height = 48.dp,
                        cornerRadius = 14.dp,
                    )
                }

                MeowTabRowStyle.Contour -> {
                    MiuixTabRowWithContour(
                        tabs = tabs,
                        selectedTabIndex = selectedIndex,
                        onTabSelected = onTabSelected,
                        modifier = modifier,
                        minWidth = 72.dp,
                        maxWidth = 120.dp,
                        height = 50.dp,
                        cornerRadius = 10.dp,
                    )
                }
            }
        },
    )
}

/**
 * One navigation destination.
 *
 * The bar renders these itself in the active style, so [modifier] is how the call
 * site reaches the resulting item — to tag it for tests, or to constrain it. It is
 * applied to the node that carries the click and the selected state, and only to
 * that node: the Material floating bar draws a second, decorative copy of every
 * item inside its sliding indicator, and applying the modifier there too would
 * duplicate the call site's semantics.
 */
@Immutable
data class MeowNavigationItem(
    val label: String,
    val icon: ImageVector,
    val badge: String? = null,
    val enabled: Boolean = true,
    val modifier: Modifier = Modifier,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MeowNavigationBar(
    items: List<MeowNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: MeowNavigationBarStyle = MeowNavigationBarStyle.Standard,
    showFloatingLabels: Boolean = true,
) {
    if (items.isEmpty()) return
    require(selectedIndex in items.indices) {
        "selectedIndex must point to an item in items"
    }

    val effect = LocalMeowScaffoldEffect.current
    val effectModifier = modifier.then(effect.bottomBarModifier)
    val currentItems by rememberUpdatedState(items)
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)
    val selectItem = remember {
        { index: Int ->
            val item = currentItems.getOrNull(index)
            if (item?.enabled == true && index != currentSelectedIndex) {
                currentOnItemSelected(index)
            }
        }
    }

    MeowStyleContent(
        materialExpressive = {
            NavigationBarStyleSwitch(
                style = style,
                standard = {
                    MaterialShortNavigationBar(
                        modifier = effectModifier,
                        containerColor = effect.bottomBarContainerColor
                            ?: ShortNavigationBarDefaults.containerColor,
                    ) {
                        items.forEachIndexed { index, item ->
                            MaterialShortNavigationBarItem(
                                selected = index == selectedIndex,
                                onClick = { selectItem(index) },
                                modifier = item.modifier,
                                enabled = item.enabled,
                                icon = { MaterialNavigationIcon(item) },
                                label = { MaterialText(item.label) },
                            )
                        }
                    }
                },
                floating = {
                    MeowFloatingNavigationBar(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = selectItem,
                        modifier = modifier,
                        showLabels = showFloatingLabels,
                    )
                },
            )
        },
        miuix = {
            NavigationBarStyleSwitch(
                style = style,
                floatingShadowHeadroom = MiuixFloatingShadowHeadroom,
                standard = {
                    MiuixNavigationBar(
                        modifier = effectModifier,
                        color = effect.bottomBarContainerColor ?: MiuixTheme.colorScheme.surface,
                    ) {
                        items.forEachIndexed { index, item ->
                            MiuixNavigationBarItem(
                                selected = index == selectedIndex,
                                onClick = { selectItem(index) },
                                modifier = item.modifier,
                                icon = item.icon,
                                label = item.label,
                                enabled = item.enabled,
                                badge = { MiuixNavigationBadge(item.badge) },
                            )
                        }
                    }
                },
                floating = {
                    MeowFloatingNavigationBar(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = selectItem,
                        modifier = modifier,
                        showLabels = showFloatingLabels,
                    )
                },
            )
        },
    )
}

/**
 * 标准与悬浮底栏之间的开关切换，参考 miuix 官方 example 的过渡方式：
 * 标准栏淡出并向上收起，悬浮栏淡入。首帧按当前 [style] 直接呈现，不播放动画。
 *
 * [floatingShadowHeadroom] 为悬浮栏顶部预留的阴影余量：淡入淡出会把内容
 * 栅格化到自身边界内，向上绘制的 dropShadow 若超出边界会在过渡期间被裁切。
 */
@Composable
private fun NavigationBarStyleSwitch(
    style: MeowNavigationBarStyle,
    standard: @Composable () -> Unit,
    floating: @Composable () -> Unit,
    floatingShadowHeadroom: Dp = 0.dp,
) {
    // 悬浮时插槽顶部这一段是量进高度但没画东西的：阴影余量 + 胶囊自己的外边距。
    // 报给 scaffold，snackbar 才知道该往下贴多少（见 LocalMeowBottomBarInset）。
    val bottomBarInset = LocalMeowBottomBarInset.current
    val deadSpaceTop = if (style == MeowNavigationBarStyle.Floating) {
        floatingShadowHeadroom + FloatingNavigationBarOuterPadding
    } else {
        0.dp
    }
    if (bottomBarInset != null) {
        SideEffect { bottomBarInset.deadSpaceTop = deadSpaceTop }
        DisposableEffect(Unit) {
            onDispose { bottomBarInset.deadSpaceTop = 0.dp }
        }
    }

    Box(contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = style == MeowNavigationBarStyle.Standard,
            // clip = false：Miuix 标准底栏向上绘制的阴影超出内容边界，
            // 展开/收起动画默认的裁切会把顶部阴影切掉一块。
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top, clip = false),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top, clip = false),
        ) {
            standard()
        }
        AnimatedVisibility(
            visible = style == MeowNavigationBarStyle.Floating,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(modifier = Modifier.padding(top = floatingShadowHeadroom)) {
                floating()
            }
        }
    }
}

/**
 * 两种风格共用的悬浮底栏,布局参考 KernelSU 的 FloatingBottomBar：等宽图文项的
 * 胶囊栏 + 随选中项平滑滑动、支持拖动的指示器；指示器内部裁剪出高亮配色的同一行
 * 内容并反向平移,让内容颜色随胶囊滑动自然过渡。
 *
 * 主题开启模糊且设备支持 RuntimeShader 时,栏体对身后的 scaffold 内容做背景模糊
 * （纯模糊,无折射高光那套液态玻璃效果）;否则使用不透明底色。
 */
@Composable
private fun MeowFloatingNavigationBar(
    items: List<MeowNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
    showLabels: Boolean,
) {
    val effect = LocalMeowScaffoldEffect.current
    val isDark = LocalMeowDarkTheme.current
    val miuix = MeowTheme.style == MeowUiStyle.Miuix
    val backdrop = LocalMeowBackdrop.current
    val useBlur = LocalMeowBlurEnabled.current &&
        backdrop != null &&
        MeowBlur.isSupported

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = FloatingNavigationBarOuterHorizontalPadding,
                vertical = FloatingNavigationBarOuterPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        val barWidth = minOf(
            maxWidth,
            420.dp,
            FloatingNavigationItemWidth * items.size + FloatingNavigationBarPadding * 2,
        )
        val tabWidth = (barWidth - FloatingNavigationBarPadding * 2) / items.size
        val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

        val animationScope = rememberCoroutineScope()
        // 固定的 spring 而不是 MaterialTheme.motionScheme:Miuix 分支没有装配
        // Material 主题,读它只会拿到与风格无关的默认值。
        val settleSpec = remember { spring<Float>(dampingRatio = 0.85f, stiffness = 550f) }
        val indicatorProgress = remember { Animatable(selectedIndex.toFloat()) }
        var dragProgress by remember { mutableFloatStateOf(selectedIndex.toFloat()) }
        var dragging by remember { mutableStateOf(false) }
        val currentSelectedIndex by rememberUpdatedState(selectedIndex)
        val currentOnItemSelected by rememberUpdatedState(onItemSelected)
        val currentItems by rememberUpdatedState(items)

        // 点击或外部改变选中项时把胶囊动画到目标位；拖动期间由手势直接驱动。
        LaunchedEffect(selectedIndex) {
            if (!dragging) {
                indicatorProgress.animateTo(selectedIndex.toFloat(), settleSpec)
            }
        }

        val containerColor = effect.floatingBottomBarContainerColor
            ?: if (miuix) {
                MiuixTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        val contentColor = if (miuix) {
            MiuixTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        val activeContentColor = MeowTheme.colors.primary

        Box(
            modifier = Modifier
                .size(width = barWidth, height = FloatingNavigationBarHeight)
                .dropShadow(
                    shape = CircleShape,
                    shadow = Shadow(
                        radius = 10.dp,
                        color = Color.Black,
                        alpha = if (isDark) 0.2f else 0.1f,
                    ),
                )
                .clip(CircleShape)
                .then(effect.floatingBottomBarModifier)
                .then(
                    if (useBlur && backdrop != null) {
                        // 半透明底色叠在模糊后的内容上;模糊不可用时退回不透明。
                        Modifier.drawBackdrop(
                            backdrop = backdrop,
                            shape = { CircleShape },
                            effects = {
                                blur(MeowBlurRadius.toPx(), MeowBlurRadius.toPx())
                            },
                            onDrawSurface = {
                                drawRect(containerColor.copy(alpha = MeowBlurSurfaceAlpha))
                            },
                        )
                    } else {
                        Modifier.background(containerColor)
                    },
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            // 底层:未选中配色的整行,承载点击与无障碍语义。
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(FloatingNavigationBarPadding)
                    .selectableGroup(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    MeowFloatingNavigationItem(
                        item = item,
                        selected = index == selectedIndex,
                        onClick = { onItemSelected(index) },
                        tint = contentColor,
                        showLabel = showLabels,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(item.modifier),
                    )
                }
            }

            // 滑动胶囊指示器,内部透出高亮配色的同一行内容。
            Box(
                modifier = Modifier
                    .padding(horizontal = FloatingNavigationBarPadding)
                    // 指示器位置只在 graphicsLayer 内读取：动画/拖动帧只重放该层，
                    // 不会让整条底栏参与重组。
                    .graphicsLayer {
                        val position = if (dragging) dragProgress else indicatorProgress.value
                        val offset = (position * tabWidthPx).roundToInt().toFloat()
                        translationX = if (isLtr) offset else -offset
                    }
                    // 胶囊支持手动拖动切页：跟手移动，抬手吸附到最近一项。
                    .pointerInput(items.size, tabWidthPx, isLtr) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragProgress = indicatorProgress.value
                                dragging = true
                                animationScope.launch { indicatorProgress.stop() }
                            },
                            onDragEnd = {
                                val releasedProgress = dragProgress
                                val target = releasedProgress
                                    .roundToInt()
                                    .coerceIn(0, items.lastIndex)
                                val resolved = if (currentItems.getOrNull(target)?.enabled == true) {
                                    target
                                } else {
                                    currentSelectedIndex
                                }
                                animationScope.launch {
                                    indicatorProgress.snapTo(releasedProgress)
                                    dragging = false
                                    indicatorProgress.animateTo(resolved.toFloat(), settleSpec)
                                }
                                if (resolved != currentSelectedIndex) {
                                    currentOnItemSelected(resolved)
                                }
                            },
                            onDragCancel = {
                                animationScope.launch {
                                    indicatorProgress.snapTo(dragProgress)
                                    dragging = false
                                    indicatorProgress.animateTo(
                                        currentSelectedIndex.toFloat(),
                                        settleSpec,
                                    )
                                }
                            },
                        ) { change, dragAmount ->
                            change.consume()
                            val deltaTabs = dragAmount / tabWidthPx * if (isLtr) 1f else -1f
                            dragProgress = (dragProgress + deltaTabs)
                                .coerceIn(0f, items.lastIndex.toFloat())
                        }
                    }
                    .clip(CircleShape)
                    .background(activeContentColor.copy(alpha = 0.15f))
                    .size(width = tabWidth, height = FloatingNavigationItemHeight),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier
                        .clearAndSetSemantics {}
                        .wrapContentWidth(align = Alignment.Start, unbounded = true)
                        .requiredWidth(barWidth - FloatingNavigationBarPadding * 2)
                        .height(FloatingNavigationItemHeight)
                        .graphicsLayer {
                            val position = if (dragging) dragProgress else indicatorProgress.value
                            val offset = (position * tabWidthPx).roundToInt().toFloat()
                            translationX = if (isLtr) -offset else offset
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEach { item ->
                        MeowFloatingNavigationItem(
                            item = item,
                            selected = false,
                            onClick = null,
                            tint = activeContentColor,
                            showLabel = showLabels,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeowFloatingNavigationItem(
    item: MeowNavigationItem,
    selected: Boolean,
    onClick: (() -> Unit)?,
    tint: Color,
    showLabel: Boolean,
    modifier: Modifier = Modifier,
) {
    val resolvedTint = if (item.enabled) tint else tint.copy(alpha = 0.38f)

    Column(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.selectable(
                        selected = selected,
                        interactionSource = null,
                        indication = null,
                        enabled = item.enabled,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MeowFloatingNavigationIcon(item = item, tint = resolvedTint)
        if (showLabel) {
            MaterialText(
                text = item.label,
                color = resolvedTint,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MeowFloatingNavigationIcon(
    item: MeowNavigationItem,
    tint: Color,
) {
    val icon: @Composable () -> Unit = {
        MaterialIcon(
            imageVector = item.icon,
            contentDescription = item.label,
            modifier = Modifier.size(26.dp),
            tint = tint,
        )
    }
    val badge = item.badge?.takeIf(String::isNotEmpty)

    if (badge == null) {
        icon()
    } else {
        MaterialBadgedBox(
            badge = {
                MaterialBadge {
                    MaterialText(badge)
                }
            },
        ) {
            icon()
        }
    }
}

@Composable
private fun MaterialNavigationIcon(item: MeowNavigationItem) {
    val badge = item.badge?.takeIf(String::isNotEmpty)
    if (badge == null) {
        MaterialIcon(
            imageVector = item.icon,
            contentDescription = item.label,
        )
        return
    }

    MaterialBadgedBox(
        badge = {
            MaterialBadge {
                MaterialText(badge)
            }
        },
    ) {
        MaterialIcon(
            imageVector = item.icon,
            contentDescription = item.label,
        )
    }
}

@Composable
private fun MiuixNavigationBadge(badge: String?) {
    badge?.takeIf(String::isNotEmpty)?.let {
        MiuixBadge {
            MiuixText(it)
        }
    }
}

private val MiuixFloatingShadowHeadroom = 24.dp

/**
 * 胶囊与插槽边界之间的外边距。竖直方向这一段在顶部是「留白」,会被报给
 * [LocalMeowBottomBarInset]——改这个值时那边自动跟着走,不要写死成两份。
 */
private val FloatingNavigationBarOuterPadding = 12.dp
private val FloatingNavigationBarOuterHorizontalPadding = 20.dp
private val FloatingNavigationBarHeight = 64.dp
private val FloatingNavigationBarPadding = 4.dp
private val FloatingNavigationItemHeight = 56.dp
private val FloatingNavigationItemWidth = 76.dp

/**
 * 侧边导航栏。宽屏用它代替 [MeowNavigationBar]：入口贴在左侧一列，页面横向留给内容。
 *
 * 两端都用各自官方的 rail：Material 的 `WideNavigationRail`、Miuix 的 `NavigationRail`。
 * 给了 [state] 就能展开成一列带文字的入口，展开按钮由侧栏自己画；
 * 宽度会从 [MeowNavigationRailDefaults.CollapsedWidth] 变到 [MeowNavigationRailDefaults.ExpandedWidth]，
 * 调用侧要留出这段，否则展开时旁边的内容被挤没。
 * [header] 画在入口上方，通常放应用图标；展开按钮不占这个位置。
 */
@Composable
fun MeowNavigationRail(
    items: List<MeowNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    state: MeowNavigationRailState? = null,
    expandContentDescription: String = MiuixNavigationRailDefaults.ExpandContentDescription,
    collapseContentDescription: String = MiuixNavigationRailDefaults.CollapseContentDescription,
    header: (@Composable ColumnScope.() -> Unit)? = null,
) {
    if (items.isEmpty()) return
    require(selectedIndex in items.indices) {
        "selectedIndex must point to an item in items"
    }

    val currentItems by rememberUpdatedState(items)
    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)
    val selectItem = remember {
        { index: Int ->
            val item = currentItems.getOrNull(index)
            if (item?.enabled == true && index != currentSelectedIndex) {
                currentOnItemSelected(index)
            }
        }
    }
    val expanded = state?.isExpanded == true

    MeowStyleContent(
        materialExpressive = {
            // 照 KernelSU 那套 md3e 侧栏：用官方的 WideNavigationRail，展开按钮放进 header 槽。
            // 宽度、图标位置、文字显隐都归它一套动画管；外面再包一层弹簧只会跟它抢，展开那下就是跳的。
            //
            // 它自带的状态是内部的，这里双向跟 [state] 对齐：初值照 [state] 给，
            // 不然切换风格或者转屏之后，已经展开的那份会被它按回收起。
            val railState = rememberWideNavigationRailState(
                initialValue = if (expanded) {
                    WideNavigationRailValue.Expanded
                } else {
                    WideNavigationRailValue.Collapsed
                },
            )
            val scope = rememberCoroutineScope()
            // 展开与否只看 rail 自己的状态：外面那份点下去就变，同步到 rail 还要过一帧。
            val showExpanded = railState.targetValue == WideNavigationRailValue.Expanded
            // 文字要比宽度晚一步。rail 的宽度是动画，文字却是直接换的，一起放出去的话
            // 文字先在折叠时的位置闪一下，再被动画推到最终位置。这条进度线与 rail 同规格，
            // 过了门限才给文字；收起时它先掉下来，文字也就先撤走。
            val motionScheme = MaterialTheme.motionScheme
            val expandSpec = remember(motionScheme) { motionScheme.defaultSpatialSpec<Float>() }
            val expandProgress = animateFloatAsState(
                targetValue = if (showExpanded) 1f else 0f,
                animationSpec = expandSpec,
                label = "meowNavigationRailExpand",
            )
            val showLabel by remember(expandProgress) {
                derivedStateOf { expandProgress.value > RailLabelRevealFraction }
            }
            if (state != null) {
                LaunchedEffect(state, railState) {
                    snapshotFlow { railState.targetValue == WideNavigationRailValue.Expanded }
                        .collect { state.isExpanded = it }
                }
                LaunchedEffect(state, railState) {
                    snapshotFlow { state.isExpanded }.collect {
                        if (it) railState.expand() else railState.collapse()
                    }
                }
            }
            val defaultRailColors = WideNavigationRailDefaults.colors()
            val railContainerColor = MaterialTheme.colorScheme.surfaceContainer
            val railColors = remember(defaultRailColors, railContainerColor) {
                defaultRailColors.copy(containerColor = railContainerColor)
            }
            WideNavigationRail(
                modifier = modifier,
                state = railState,
                colors = railColors,
                windowInsets = RailInsets(),
                contentPadding = PaddingValues(vertical = RailVerticalPadding),
                header = if (state != null || header != null) {
                    {
                        Column {
                            state?.let {
                                MaterialIconButton(
                                    onClick = { scope.launch { railState.toggle() } },
                                    modifier = Modifier.padding(start = RailHeaderStartPadding),
                                ) {
                                    MaterialIcon(
                                        imageVector = if (showExpanded) {
                                            Icons.AutoMirrored.Rounded.MenuOpen
                                        } else {
                                            Icons.Rounded.Menu
                                        },
                                        contentDescription = if (showExpanded) {
                                            collapseContentDescription
                                        } else {
                                            expandContentDescription
                                        },
                                    )
                                }
                            }
                            header?.invoke(this)
                        }
                    }
                } else {
                    null
                },
            ) {
                items.forEachIndexed { index, item ->
                    WideNavigationRailItem(
                        railExpanded = showExpanded,
                        selected = index == selectedIndex,
                        onClick = { selectItem(index) },
                        icon = { RailIcon(item.icon, item.badge, item.label) },
                        // 收起来只留图标。M3E 折叠态本来在图标下还排一行小字，那行字
                        // 跟 Miuix 那边收起后的样子对不上，这里一律不要。
                        label = if (showLabel) {
                            { MaterialText(item.label) }
                        } else {
                            null
                        },
                        modifier = item.modifier,
                        enabled = item.enabled,
                    )
                }
            }
        },
        miuix = {
            // Miuix 的展开按钮由 rail 自己画，条件是给了 state。它自带的状态是内部的，
            // 这里双向跟 [state] 对齐：点自带按钮改的是它，外面改的是 [state]。
            //
            // 初值必须照着 [state] 给：不给的话它从收起开始，下面第一条同步会把已经展开的
            // [state] 按回收起——切换风格或转屏之后，展开状态就这么丢了。
            val railState = state?.let {
                rememberMiuixNavigationRailState(
                    initialValue = if (it.isExpanded) {
                        MiuixNavigationRailValue.Expanded
                    } else {
                        MiuixNavigationRailValue.Collapsed
                    },
                )
            }
            if (state != null && railState != null) {
                LaunchedEffect(state, railState) {
                    snapshotFlow { railState.isExpanded }.collect { state.isExpanded = it }
                }
                LaunchedEffect(state, railState) {
                    snapshotFlow { state.isExpanded }.collect {
                        if (it) railState.expand() else railState.collapse()
                    }
                }
            }
            // 排版同样看 rail 自己的状态，进度线也照它那条弹簧的规格来。
            // 文字得等宽度基本到位才出现：Miuix 的 item 是在布局期把文字从图标下方挪到右边，
            // 一按下去就给文字的话，它会先在图标下面闪一次再被挪过去。
            val showExpanded = railState?.isExpanded == true
            val expandSpec = remember {
                folmeSpring<Float>(
                    damping = MiuixRailSpringDamping,
                    response = MiuixRailSpringResponse,
                    visibilityThreshold = MiuixRailSpringThreshold,
                )
            }
            val expandProgress = animateFloatAsState(
                targetValue = if (showExpanded) 1f else 0f,
                animationSpec = expandSpec,
                label = "meowNavigationRailExpand",
            )
            val showLabel by remember(expandProgress) {
                derivedStateOf { expandProgress.value > RailLabelRevealFraction }
            }
            // 宽度按 Miuix 自己的来：它的图标在折叠中线与展开起始缩进上是同一条竖线，
            // 那条线由它的默认宽度算出来，改宽度会让图标在展开动画里横着漂。
            MiuixNavigationRail(
                modifier = modifier,
                state = railState,
                header = header,
                expandContentDescription = expandContentDescription,
                collapseContentDescription = collapseContentDescription,
            ) {
                items.forEachIndexed { index, item ->
                    MiuixNavigationRailItem(
                        selected = index == selectedIndex,
                        onClick = { selectItem(index) },
                        icon = item.icon,
                        // 折叠时不显示文字。Miuix 的 item 没有「不要文字」这个选项，只能给空串，
                        // 那一行的高度它照样留着。
                        label = if (showLabel) item.label else "",
                        modifier = if (showLabel) {
                            item.modifier
                        } else {
                            item.modifier.semantics { contentDescription = item.label }
                        },
                        enabled = item.enabled,
                        badge = { MiuixNavigationBadge(item.badge) },
                    )
                }
            }
        },
    )
}

/** 侧栏入口的图标。角标叠在右上角；颜色由 Material 的入口通过 LocalContentColor 给。 */
@Composable
private fun RailIcon(icon: ImageVector, badge: String?, description: String?) {
    val content = @Composable {
        MaterialIcon(imageVector = icon, contentDescription = description)
    }
    if (badge.isNullOrEmpty()) {
        content()
        return
    }
    MaterialBadgedBox(badge = { MaterialBadge { MaterialText(badge) } }) { content() }
}

/** 侧栏上下的内边距。短窗口（手机横屏）也要装得下所有入口，侧栏本身不滚动。 */
private val RailVerticalPadding = 20.dp

/** 展开按钮离侧栏起始边的距离。这个数让它落在入口图标那条竖线上。 */
private val RailHeaderStartPadding = 24.dp

/** 文字露出来的门限：展开进度过了这条线才给文字，收起时掉回线下就撤走。 */
private const val RailLabelRevealFraction = 0.6f

/** Miuix 侧栏展开那条弹簧的参数，抄自它的 RailExpandSpring，好让上面的门限跟它同一条曲线。 */
private const val MiuixRailSpringDamping = 1f
private const val MiuixRailSpringResponse = 0.35f
private const val MiuixRailSpringThreshold = 0.001f

/**
 * Material 侧栏的宽度。M3E 规范是折叠 96dp、展开 220dp，官方把这两个数留在了内部，
 * 调用侧要给侧栏留位置只能照抄一份。
 */
private val MaterialCollapsedRailWidth = 96.dp
private val MaterialExpandedRailWidth = 220.dp

/** 侧边导航栏的展开状态。展开与收起要跨风格一致，所以状态放在这里，不用两端各自的。 */
@Stable
class MeowNavigationRailState internal constructor(expanded: Boolean) {
    var isExpanded: Boolean by mutableStateOf(expanded)
        internal set

    fun expand() {
        isExpanded = true
    }

    fun collapse() {
        isExpanded = false
    }

    fun toggle() {
        isExpanded = !isExpanded
    }

    internal companion object {
        val Saver: Saver<MeowNavigationRailState, Boolean> = Saver(
            save = { it.isExpanded },
            restore = { MeowNavigationRailState(it) },
        )
    }
}

/** 记住一份 [MeowNavigationRailState]，转屏后仍是原来的展开状态。 */
@Composable
fun rememberMeowNavigationRailState(
    initiallyExpanded: Boolean = false,
): MeowNavigationRailState = rememberSaveable(saver = MeowNavigationRailState.Saver) {
    MeowNavigationRailState(initiallyExpanded)
}

/**
 * 侧边导航栏的宽度。调用侧要给侧栏留位置时读这里，别自己写死。
 *
 * 两种风格各一组：Material 照 M3E 规范，Miuix 照它自己的默认值。取错了留白就差一截，
 * 展开时旁边的内容会被挤掉一块。
 */
object MeowNavigationRailDefaults {
    /** 折叠宽度，只放图标。 */
    val CollapsedWidth: Dp
        @Composable get() = when (MeowTheme.style) {
            MeowUiStyle.MaterialExpressive -> MaterialCollapsedRailWidth
            MeowUiStyle.Miuix -> MiuixNavigationRailDefaults.MinWidth
        }

    /** 展开成图标加文字后的宽度。 */
    val ExpandedWidth: Dp
        @Composable get() = when (MeowTheme.style) {
            MeowUiStyle.MaterialExpressive -> MaterialExpandedRailWidth
            MeowUiStyle.Miuix -> MiuixNavigationRailDefaults.ExpandedWidth
        }
}

/**
 * 侧栏自己让开的系统栏区域：起始侧与上下。
 *
 * Material 的默认值只算 system bars，不算刘海；Miuix 那边两样都算。这里对齐成同一份，
 * 两种风格下侧栏的留白才一样。不并入 ime：键盘弹出时侧栏不该跟着缩。
 */
@Composable
private fun RailInsets(): WindowInsets = WindowInsets.systemBars
    .union(WindowInsets.displayCutout)
    .only(WindowInsetsSides.Start + WindowInsetsSides.Vertical)
