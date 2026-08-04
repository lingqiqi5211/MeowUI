package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet as MaterialModalBottomSheet
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet as MiuixOverlayBottomSheet
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * 底部抽屉。Material 分支为 ModalBottomSheet，Miuix 分支为应用内 OverlayBottomSheet。
 *
 * Miuix 分支必须在某个 [MeowScaffold]（或 miuix Scaffold）的组合子树内调用：抽屉经由
 * Scaffold 提供的 popup host 渲染，放在 Scaffold 之外（例如与 Scaffold 平级的导航层）
 * 时不会显示。Material 分支无此限制，但为两风格行为一致，请统一放进 Scaffold 内容里。
 *
 * [fullHeight] 让抽屉一开就顶满、也不会停在半展开：内容自己占满剩余高度，并且不再由抽屉
 * 代为滚动——里面装浏览器一类会换内容的东西时，抽屉高度就不跟着内容一级一级地跳。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeowBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    startAction: (@Composable () -> Unit)? = null,
    endAction: (@Composable () -> Unit)? = null,
    fullHeight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    MeowStyleContent(
        materialExpressive = {
            val sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                // 顶满时不给半展开这一档，否则抽屉会先停在一半高的位置。
                enabledValues = if (fullHeight) {
                    setOf(SheetValue.Hidden, SheetValue.Expanded)
                } else {
                    SheetValue.entries.toSet()
                },
            )
            // show 变为 false 时先播放收起动画再移除，和 Miuix WindowBottomSheet 行为一致。
            var visible by remember { mutableStateOf(show) }
            LaunchedEffect(show) {
                if (show) {
                    visible = true
                } else if (visible) {
                    sheetState.hide()
                    visible = false
                }
            }
            if (visible) {
                // 返回键交给 ModalBottomSheet 自己（直接关，没有跟手过程）。M3 至今只给抽屉式
                // 导航栏做了预测式返回，底部抽屉没有；自己接需要先关掉 dialog 的「按返回即关」
                // 再在抽屉内容里收手势，落地效果不理想——等上游给方案。
                MaterialModalBottomSheet(
                    onDismissRequest = onDismissRequest,
                    modifier = modifier,
                    sheetState = sheetState,
                ) {
                    MaterialBottomSheetHeader(
                        title = title,
                        startAction = startAction,
                        endAction = endAction,
                    )
                    // ModalBottomSheet 的内容槽位没有任何内边距,分组卡片会顶到抽屉
                    // 左右边缘。miuix 的 insideMargin 默认已经留了边距,这里补齐。
                    Column(
                        modifier = Modifier
                            .then(if (fullHeight) Modifier.weight(1f) else Modifier)
                            .padding(
                                start = MeowTheme.dimensions.pageHorizontalPadding,
                                end = MeowTheme.dimensions.pageHorizontalPadding,
                                bottom = MeowTheme.dimensions.pageHorizontalPadding,
                            ),
                        content = content,
                    )
                }
            }
        },
        miuix = {
            // 与 miuix 官方 example 对齐：使用应用内 OverlayBottomSheet（而不是开新窗口的
            // WindowBottomSheet），空标题/空动作传 null；安全区由组件自身处理，
            // 内容可滚动并带 overscroll 与滚动到底触感。
            MiuixOverlayBottomSheet(
                show = show,
                modifier = modifier,
                title = title.takeIf(String::isNotBlank),
                startAction = startAction?.let { action -> { action() } },
                endAction = endAction?.let { action -> { action() } },
                // 在根 Scaffold 中渲染,抽屉才会覆盖全屏而不是被限制在当前 Scaffold 的
                // 范围内。注意 OverlayBottomSheet 无论如何都需要一个 miuix Scaffold 祖先
                // 提供 MiuixPopupHost —— 没有的话抽屉不会出现,renderInRootScaffold 改成
                // false 也救不了。
                renderInRootScaffold = true,
                onDismissRequest = onDismissRequest,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            // 顶满时内容自己管滚动，抽屉不再代为滚动——否则里面的列表
                            // 拿到的是无界高度，占不满也滚不动。
                            .then(
                                if (fullHeight) {
                                    Modifier.fillMaxHeight()
                                } else {
                                    Modifier
                                        .scrollEndHaptic()
                                        .overScrollVertical()
                                        .verticalScroll(rememberScrollState())
                                },
                            )
                            // 抽屉内容与底部边缘之间的垫高,与 Material 分支一致。
                            .padding(bottom = 16.dp),
                    ) {
                        CompositionLocalProvider(LocalMeowOnSheet provides true) {
                            content()
                        }
                    }
                },
            )
        },
    )
}

@Composable
private fun MaterialBottomSheetHeader(
    title: String,
    startAction: (@Composable () -> Unit)?,
    endAction: (@Composable () -> Unit)?,
) {
    if (title.isBlank() && startAction == null && endAction == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            startAction?.invoke()
        }
        if (title.isBlank()) {
            Spacer(Modifier.weight(2f))
        } else {
            MaterialText(
                text = title,
                modifier = Modifier.weight(2f),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            endAction?.invoke()
        }
    }
}
