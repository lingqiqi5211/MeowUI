package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowBottomSheet as MiuixWindowBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeowBottomSheet(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    startAction: (@Composable () -> Unit)? = null,
    endAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    MeowStyleContent(
        materialExpressive = {
            val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
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
                    content()
                }
            }
        },
        miuix = {
            // 与 miuix 官方 example 对齐：空标题/空动作传 null（避免占位），
            // 内容可滚动并带 overscroll 与滚动到底触感，底部补导航栏安全区。
            MiuixWindowBottomSheet(
                show = show,
                modifier = modifier,
                title = title.takeIf(String::isNotBlank),
                startAction = startAction?.let { action -> { action() } },
                endAction = endAction?.let { action -> { action() } },
                onDismissRequest = onDismissRequest,
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scrollEndHaptic()
                            .overScrollVertical()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        content()
                        Spacer(
                            Modifier.height(
                                WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding() +
                                    WindowInsets.captionBar.asPaddingValues()
                                        .calculateBottomPadding(),
                            ),
                        )
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
