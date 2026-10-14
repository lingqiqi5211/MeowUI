package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.basic.BreadcrumbBar
import top.yukonga.miuix.kmp.basic.BreadcrumbItem

/** Maven 0.9.4 的 BreadcrumbBar 仍使用 ScrollState。 */
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
    val state = rememberScrollState()
    BreadcrumbBar(
        items = items,
        onItemClick = onItemClick,
        modifier = Modifier.breadcrumbEdgeFade(state, edgeFadeWidth, edgeFadeInset),
        highlightIndex = highlightIndex,
        enabled = enabled,
        itemMaxWidth = itemMaxWidth,
        insideMargin = contentPadding,
        scrollState = state,
    )
}
