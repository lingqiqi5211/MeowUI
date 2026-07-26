package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.MeowTheme

enum class MeowCompactPane {
    List,
    Detail,
}

@Composable
fun MeowAdaptiveLayout(
    compactPane: MeowCompactPane,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    expandedBreakpoint: Dp = 840.dp,
    listPaneWidth: Dp = 360.dp,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        if (availableWidth >= expandedBreakpoint) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .width(listPaneWidth.coerceAtMost(availableWidth * 0.45f))
                        .fillMaxHeight(),
                ) {
                    listPane()
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MeowTheme.colors.divider),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    detailPane()
                }
            }
        } else {
            when (compactPane) {
                MeowCompactPane.List -> listPane()
                MeowCompactPane.Detail -> detailPane()
            }
        }
    }
}
