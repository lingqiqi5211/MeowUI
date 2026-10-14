package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.basic.BreadcrumbBar
import top.yukonga.miuix.kmp.basic.BreadcrumbItem

/** 固定的 Miuix 源码提交使用 LazyListState。 */
@Composable
internal fun MiuixBreadcrumbContent(
    items: List<BreadcrumbItem>,
    onItemClick: (Int) -> Unit,
    highlightIndex: Int,
    enabled: Boolean,
    itemMaxWidth: Dp,
    contentPadding: PaddingValues,
    edgeFadeWidth: Dp,
    edgeFadeInset: Dp,
) {
    val state = rememberLazyListState()
    BreadcrumbBar(
        items = items,
        onItemClick = onItemClick,
        modifier = Modifier.breadcrumbEdgeFade(state, edgeFadeWidth, edgeFadeInset),
        highlightIndex = highlightIndex,
        enabled = enabled,
        itemMaxWidth = itemMaxWidth,
        insideMargin = contentPadding,
        listState = state,
    )
}
