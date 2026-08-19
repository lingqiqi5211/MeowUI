package io.github.lingqiqi5211.meowui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge as MaterialBadge
import androidx.compose.material3.BadgedBox as MaterialBadgedBox
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar as MaterialShortNavigationBar
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.ShortNavigationBarItem as MaterialShortNavigationBarItem
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.ToggleButton as MaterialToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.LocalMeowBlurEnabled
import io.github.lingqiqi5211.meowui.theme.LocalMeowDarkTheme
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.Badge as MiuixBadge
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.TabRow as MiuixTabRow
import top.yukonga.miuix.kmp.basic.TabRowWithContour as MiuixTabRowWithContour
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
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
        isRuntimeShaderSupported()

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
        val settleSpec = spring<Float>(dampingRatio = 0.85f, stiffness = 550f)
        val indicatorProgress = remember { Animatable(selectedIndex.toFloat()) }
        var dragProgress by remember { mutableFloatStateOf(selectedIndex.toFloat()) }
        var dragging by remember { mutableStateOf(false) }
        val currentSelectedIndex by rememberUpdatedState(selectedIndex)
        val currentOnItemSelected by rememberUpdatedState(onItemSelected)

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
                                val resolved = if (items[target].enabled) {
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
