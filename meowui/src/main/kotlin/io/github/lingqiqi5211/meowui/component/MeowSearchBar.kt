package io.github.lingqiqi5211.meowui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.IconButtonDefaults as MaterialIconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search as MiuixSearchIcon
import top.yukonga.miuix.kmp.icon.basic.SearchCleanup as MiuixSearchCleanupIcon
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 搜索框：折叠时是页面里的一条药丸形输入条，展开后整屏被搜索面接管。
 *
 * 动画照 KernelSU 的两套实现做同一个动作：输入条从它在页面里的位置飞到状态栏下方，
 * 底面淡入盖住页面，[content]（搜索结果）随后铺满整屏。KernelSU 的 Material 端用 M3
 * 的 contained search bar、Miuix 端手写 SearchPager，动作是同一个；这里两端合用一份
 * 骨架，外观各自原生——Material 前导图标换成返回按钮，Miuix 右侧滑入 [cancelText]。
 *
 * 展开态交给外层 [MeowScaffold] 的浮层插槽（因此本组件必须用在 MeowScaffold 内）：
 * 就地铺满整屏会被父级约束挡住、或把滚动容器按最大约束撑爆；开一个 Popup 则多一个窗口，
 * 它的 inset 与页面不同一套，顶底两条系统栏区要么漏出页面、要么多出一块空白。
 * 浮层起始位置取自折叠输入条在窗口中的实测坐标，所以两者重合，看上去是同一条输入条飞上去。
 *
 * 交互约定（两端相同）：
 * - 折叠时整条输入条可点，点按回调 `onExpandedChange(true)`，展开后自动抢焦点弹键盘；
 * - 返回按钮 / 取消 / 系统返回键都等于「取消」——清空查询词并回调 `onExpandedChange(false)`；
 * - 查询词非空时行内出现清空按钮（缩放淡入），只清词不收起，焦点保留；
 * - [content] 自带容器：组件只管边距与系统栏/键盘让位，卡片形态由调用侧决定；
 * - 键盘上的搜索键回调 [onSearch] 后**只收键盘、不收起**——结果就在下方，收起反而看不到
 *   结果。需要提交后收起的场景由调用侧在 [onSearch] 里自己置 `expanded = false`。
 *
 * 展开状态与查询词都由调用侧持有，组件只负责回调。
 */
@Composable
fun MeowSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    cancelText: String = "Cancel",
    onSearch: (String) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isExpanded by rememberUpdatedState(expanded)

    // 折叠输入条在窗口中的位置：浮层里的输入条以它为动画起点，两者重合才不会有跳变。
    var collapsedTop by remember { mutableStateOf(0.dp) }
    // 收起动画播完之前浮层必须留着，否则输入条会瞬间消失而不是飞回原位。
    var overlayMounted by remember { mutableStateOf(expanded) }
    LaunchedEffect(expanded) { if (expanded) overlayMounted = true }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val barTop by animateDpAsState(
        targetValue = if (expanded) statusBarTop + OverlayBarTopGap else collapsedTop,
        animationSpec = tween(LiftMillis, easing = LinearOutSlowInEasing),
        label = "MeowSearchBarLift",
        finishedListener = { if (!isExpanded) overlayMounted = false },
    )
    val surfaceAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(SurfaceFadeMillis, easing = FastOutSlowInEasing),
        label = "MeowSearchBarSurface",
    )

    val collapse: () -> Unit = {
        // 取消 = 放弃这次搜索,所以连查询词一起清掉,下次展开是干净的空框。
        onQueryChange("")
        onExpandedChange(false)
        focusManager.clearFocus()
        keyboardController?.hide()
    }
    val submit: () -> Unit = {
        onSearch(query)
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    // 展开着被整页弹栈时键盘会留在新页面上,这里兜底。
    DisposableEffect(Unit) {
        onDispose { if (isExpanded) keyboardController?.hide() }
    }

    // 折叠态的占位输入条：只负责显示和接点击，输入交给浮层里的那一条。
    // 浮层挂载期间隐藏（保留占位）：收起动画里浮层那条在飞回来，
    // 占位条同时画着的话屏幕上就是两条搜索框叠在一起。
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                collapsedTop = with(density) { coordinates.positionInWindow().y.toDp() }
            }
            .graphicsLayer { alpha = if (overlayMounted) 0f else 1f },
    ) {
        MeowStyleContent(
            materialExpressive = { MaterialCollapsedBar(placeholder = placeholder) },
            miuix = { MiuixCollapsedBar(placeholder = placeholder) },
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(interactionSource = null, indication = null) { onExpandedChange(true) },
        )
    }

    val overlayHost = LocalMeowOverlayHost.current
    val overlay: @Composable () -> Unit = {
        BackHandler(enabled = expanded, onBack = collapse)
        // 焦点在浮层自己的组合里要：输入框属于宿主的子树，搜索框那边发起的
        // requestFocus 会快一拍，报 FocusRequester is not initialized 并静默失败（键盘不弹）。
        LaunchedEffect(expanded) {
            if (expanded) focusRequester.requestFocus()
        }
        MeowStyleContent(
            materialExpressive = {
                val colorScheme = MaterialTheme.colorScheme
                SearchOverlay(
                    // 底面用与 MeowScaffold 页面同一个 role，结果卡片才能沿用库里已有的
                    // 页面/卡片对比（surfaceContainer 上放 surfaceBright）；刷 surface 的话卡片与底面同色。
                    surfaceColor = colorScheme.surfaceContainer.copy(alpha = surfaceAlpha),
                    barTop = barTop,
                    expanded = expanded,
                    content = content,
                ) {
                    MaterialSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = OverlayBarHorizontalPadding),
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerHigh,
                        contentColor = colorScheme.onSurface,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = MaterialBarHeight)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // 前导槽位淡入淡出地换人（放大镜 ↔ 返回按钮）,而不是位移或直接替换：
                            // 位移会带着输入框里的文字一起抖,直接替换又缺了展开的仪式感。
                            AnimatedContent(
                                targetState = expanded,
                                transitionSpec = {
                                    fadeIn(tween(LeadingFadeMillis)) togetherWith
                                        fadeOut(tween(LeadingFadeMillis))
                                },
                                label = "MeowSearchBarLeading",
                            ) { showBack ->
                                if (showBack) {
                                    MaterialIconButton(
                                        onClick = collapse,
                                        colors = MaterialIconButtonDefaults.iconButtonColors(
                                            containerColor = colorScheme.surfaceContainerHighest,
                                            contentColor = colorScheme.onSurfaceVariant,
                                        ),
                                    ) {
                                        MaterialIcon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = cancelText,
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.size(MaterialSlotSize),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        MaterialIcon(
                                            imageVector = Icons.Rounded.Search,
                                            contentDescription = null,
                                            tint = colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                            MeowSearchTextField(
                                query = query,
                                onQueryChange = onQueryChange,
                                placeholder = placeholder,
                                textStyle = MaterialTheme.typography.bodyLarge
                                    .copy(color = colorScheme.onSurface),
                                placeholderColor = colorScheme.onSurfaceVariant,
                                cursorColor = colorScheme.primary,
                                focusRequester = focusRequester,
                                onSubmit = submit,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                            )
                            ClearAction(visible = query.isNotEmpty()) {
                                MaterialIconButton(onClick = { onQueryChange("") }) {
                                    MaterialIcon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = ClearContentDescription,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            miuix = {
                val colorScheme = MiuixTheme.colorScheme
                SearchOverlay(
                    surfaceColor = colorScheme.surface.copy(alpha = surfaceAlpha),
                    barTop = barTop,
                    expanded = expanded,
                    content = content,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = OverlayBarHorizontalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = MiuixBarHeight)
                                .background(colorScheme.surfaceContainerHigh, CircleShape),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MiuixIcon(
                                imageVector = MiuixIcons.Basic.MiuixSearchIcon,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceContainerHigh,
                                modifier = Modifier
                                    .size(MiuixSlotSize)
                                    .padding(start = 16.dp, end = 8.dp),
                            )
                            MeowSearchTextField(
                                query = query,
                                onQueryChange = onQueryChange,
                                placeholder = placeholder,
                                textStyle = TextStyle(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onSurface,
                                ),
                                placeholderColor = colorScheme.onSurfaceVariantSummary,
                                cursorColor = colorScheme.primary,
                                focusRequester = focusRequester,
                                onSubmit = submit,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                            )
                            ClearAction(visible = query.isNotEmpty()) {
                                MiuixIcon(
                                    imageVector = MiuixIcons.Basic.MiuixSearchCleanupIcon,
                                    contentDescription = ClearContentDescription,
                                    tint = colorScheme.onSurface,
                                    modifier = Modifier
                                        .size(MiuixSlotSize)
                                        .padding(start = 8.dp, end = 16.dp)
                                        .clickable(
                                            interactionSource = null,
                                            indication = null,
                                        ) { onQueryChange("") },
                                )
                            }
                        }
                        // 取消按钮从右侧滑出来并把输入条挤窄,这是 miuix/HyperOS 搜索的招牌动作;
                        // Material 那端不做这个,它的返回箭头已经在输入条内部承担了同一职责。
                        AnimatedVisibility(
                            visible = expanded,
                            enter = expandHorizontally() + slideInHorizontally { it },
                            exit = shrinkHorizontally() + slideOutHorizontally { it },
                        ) {
                            MiuixText(
                                text = cancelText,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .clickable(
                                        interactionSource = null,
                                        indication = null,
                                        onClick = collapse,
                                    ),
                            )
                        }
                    }
                }
            },
        )
    }

    // 插槽是整个 scaffold 一份，而搜索框可能同时组合着好几个（页面常驻的 pager 里
    // 每个页面一个）。KernelSU 每个屏幕自带浮层不存在争抢；这里把同一语义搬进插槽：
    // 发布物身份稳定（内容经 rememberUpdatedState 透传，不靠反复重写来刷新），
    // 只有挂载中的实例写插槽，清空只能清自己的——否则任何一个折叠着的搜索框
    // 重组一次，就会把别人正展开的搜索面打掉（表现为搜索面反复消失又出现）。
    //
    // overlayMounted 在组合体里读：SideEffect 里的读取不订阅快照状态，
    // 只在那里读的话它置 true 也不会触发重组，插槽永远发不出去（展开没反应）。
    val latestOverlay by rememberUpdatedState(overlay)
    val slot: @Composable () -> Unit = remember { { latestOverlay() } }
    val mounted = overlayMounted
    SideEffect {
        val host = overlayHost ?: return@SideEffect
        if (mounted) {
            host.content = slot
        } else if (host.content === slot) {
            host.content = null
        }
    }
    DisposableEffect(overlayHost) {
        onDispose {
            if (overlayHost?.content === slot) overlayHost.content = null
        }
    }
}

/**
 * 展开浮层的骨架：底面淡入、输入条从 [barTop] 处起飞、结果在落位后铺满剩余整屏。
 *
 * 结果晚于输入条出现（[expanded] 决定），飞行途中屏幕上只有一条正在上移的输入条，
 * 和 KernelSU 一样；结果一开始就画出来的话，飞行过程会变成一堆内容跟着抖。
 */
@Composable
private fun SearchOverlay(
    surfaceColor: Color,
    barTop: Dp,
    expanded: Boolean,
    content: @Composable ColumnScope.() -> Unit,
    bar: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            // 整层参与命中测试：没落在交互子节点上的触摸（尤其收起动画期间
            // 取消按钮正在退场那几帧）在这里被吞掉，不会穿透到下面的顶栏按钮。
            .pointerInput(Unit) {},
    ) {
        Box(modifier = Modifier.padding(top = barTop)) { bar() }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(ResultsFadeMillis)),
            exit = fadeOut(tween(ResultsFadeMillis)),
        ) {
            // 底面铺满整屏（含系统栏），所以结果自己让开导航栏和键盘。
            //
            // 结果容器交给调用侧：给整份结果套一层大卡片在这里不成立——列表项本身
            // 就是一块一块的卡片（MeowPreferenceSection / MeowCard），再包一层就是卡中卡。
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = OverlayBarHorizontalPadding, vertical = 8.dp),
                content = content,
            )
        }
    }
}

/** 清空按钮的出场：缩放淡入,只清词不收起。 */
@Composable
private fun ClearAction(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
    ) {
        content()
    }
}

/** 折叠态的 Material 输入条：只是长得像搜索框的一块表面，输入在浮层里进行。 */
@Composable
private fun MaterialCollapsedBar(placeholder: String) {
    val colorScheme = MaterialTheme.colorScheme
    MaterialSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        color = colorScheme.surfaceContainerHigh,
        contentColor = colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MaterialBarHeight)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(MaterialSlotSize), contentAlignment = Alignment.Center) {
                MaterialIcon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                )
            }
            BasicText(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge
                    .copy(color = colorScheme.onSurfaceVariant),
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

/** 折叠态的 Miuix 输入条。 */
@Composable
private fun MiuixCollapsedBar(placeholder: String) {
    val colorScheme = MiuixTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MiuixBarHeight)
            .background(colorScheme.surfaceContainerHigh, CircleShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiuixIcon(
            imageVector = MiuixIcons.Basic.MiuixSearchIcon,
            contentDescription = null,
            tint = colorScheme.onSurfaceContainerHigh,
            modifier = Modifier
                .size(MiuixSlotSize)
                .padding(start = 16.dp, end = 8.dp),
        )
        BasicText(
            text = placeholder,
            style = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariantSummary,
            ),
            maxLines = 1,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}

/**
 * 两端共用的输入内核：占位符自己画在下层，省掉一个只为显示提示文字的假搜索框组件。
 *
 * 选区由本地的 [TextFieldValue] 持有，而不是用 `BasicTextField` 的 String 重载：查询词
 * 是调用侧持有的，每次回流都会重建一个选区在 0 的值，光标于是钉在最前面，打字变成倒着插。
 * 外部值真的变了（清空、程序化赋值）才同步，并把光标放到末尾。
 */
@Composable
private fun MeowSearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    placeholderColor: Color,
    cursorColor: Color,
    focusRequester: FocusRequester,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }
    LaunchedEffect(query) {
        if (fieldValue.text != query) {
            fieldValue = TextFieldValue(query, TextRange(query.length))
        }
    }

    BasicTextField(
        value = fieldValue,
        onValueChange = { value ->
            fieldValue = value
            if (value.text != query) onQueryChange(value.text)
        },
        modifier = modifier.focusRequester(focusRequester),
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(cursorColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (fieldValue.text.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        style = textStyle.copy(color = placeholderColor),
                        maxLines = 1,
                    )
                }
                innerTextField()
            }
        },
    )
}

private const val ClearContentDescription = "Clear"
private const val LeadingFadeMillis = 120
private const val LiftMillis = 300
private const val SurfaceFadeMillis = 200
private const val ResultsFadeMillis = 200
private val OverlayBarTopGap = 5.dp
private val OverlayBarHorizontalPadding = 12.dp
private val MaterialBarHeight = 56.dp
private val MaterialSlotSize = 48.dp
private val MiuixBarHeight = 45.dp
private val MiuixSlotSize = 44.dp
