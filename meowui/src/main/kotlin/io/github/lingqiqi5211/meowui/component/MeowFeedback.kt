package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface as MaterialSurface
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox as MaterialPullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.CardColors as MiuixCardColors
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.PullToRefresh as MiuixPullToRefresh
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class MeowTipStyle {
    Info,
    Success,
    Warning,
    Error,
}

@Composable
fun MeowTip(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    style: MeowTipStyle = MeowTipStyle.Info,
    icon: ImageVector? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    require(actionText == null || onAction != null) {
        "onAction is required when actionText is provided"
    }
    val resolvedIcon = icon ?: style.defaultIcon()

    MeowStyleContent(
        materialExpressive = {
            val (containerColor, contentColor) = materialTipColors(style)
            MaterialSurface(
                modifier = modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = containerColor,
                contentColor = contentColor,
            ) {
                TipContent(
                    title = title,
                    message = message,
                    icon = resolvedIcon,
                    contentColor = contentColor,
                    action = actionText?.let { text ->
                        {
                            MaterialTextButton(onClick = onAction!!) {
                                MaterialText(text)
                            }
                        }
                    },
                )
            }
        },
        miuix = {
            val (containerColor, contentColor) = miuixTipColors(style)
            MiuixCard(
                modifier = modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                insideMargin = PaddingValues(0.dp),
                colors = MiuixCardColors(
                    color = containerColor,
                    contentColor = contentColor,
                ),
            ) {
                TipContent(
                    title = title,
                    message = message,
                    icon = resolvedIcon,
                    contentColor = contentColor,
                    action = actionText?.let { text ->
                        {
                            MiuixTextButton(
                                text = text,
                                onClick = onAction!!,
                            )
                        }
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MeowPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    MeowStyleContent(
        materialExpressive = {
            val state = rememberPullToRefreshState()
            MaterialPullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier,
                state = state,
                // Expressive 形变加载指示器，替换默认的圆形箭头指示器。
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = state,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                },
            ) {
                Box(modifier = Modifier.padding(contentPadding)) {
                    content()
                }
            }
        },
        miuix = {
            MiuixPullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier,
                contentPadding = contentPadding,
                topAppBarScrollBehavior = LocalMeowScrollContext.current.miuixTopBar,
                content = content,
            )
        },
    )
}

@Composable
private fun TipContent(
    title: String?,
    message: String,
    icon: ImageVector,
    contentColor: Color,
    action: (@Composable () -> Unit)?,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MeowStyleContent(
            materialExpressive = {
                MaterialIcon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor,
                )
            },
            miuix = {
                MiuixIcon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor,
                )
            },
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            title?.takeIf(String::isNotBlank)?.let { text ->
                MeowText(
                    text = text,
                    color = contentColor,
                    style = MeowTheme.typography.title,
                )
            }
            MeowText(
                text = message,
                color = contentColor,
                style = MeowTheme.typography.summary,
            )
        }
        action?.let {
            Spacer(Modifier.width(8.dp))
            it()
        }
    }
}

@Composable
private fun materialTipColors(style: MeowTipStyle): Pair<Color, Color> = when (style) {
    MeowTipStyle.Info -> MaterialTheme.colorScheme.primaryContainer to
        MaterialTheme.colorScheme.onPrimaryContainer
    MeowTipStyle.Success -> MaterialTheme.colorScheme.tertiaryContainer to
        MaterialTheme.colorScheme.onTertiaryContainer
    MeowTipStyle.Warning -> MaterialTheme.colorScheme.secondaryContainer to
        MaterialTheme.colorScheme.onSecondaryContainer
    MeowTipStyle.Error -> MaterialTheme.colorScheme.errorContainer to
        MaterialTheme.colorScheme.onErrorContainer
}

@Composable
private fun miuixTipColors(style: MeowTipStyle): Pair<Color, Color> = when (style) {
    MeowTipStyle.Info -> MiuixTheme.colorScheme.tertiaryContainer to
        MiuixTheme.colorScheme.onTertiaryContainer
    MeowTipStyle.Success -> MiuixTheme.colorScheme.primaryContainer to
        MiuixTheme.colorScheme.onPrimaryContainer
    MeowTipStyle.Warning -> MiuixTheme.colorScheme.secondaryContainer to
        MiuixTheme.colorScheme.onSecondaryContainer
    MeowTipStyle.Error -> MiuixTheme.colorScheme.errorContainer to
        MiuixTheme.colorScheme.onErrorContainer
}

private fun MeowTipStyle.defaultIcon(): ImageVector = when (this) {
    MeowTipStyle.Info -> Icons.Rounded.Info
    MeowTipStyle.Success -> Icons.Rounded.CheckCircle
    MeowTipStyle.Warning -> Icons.Rounded.WarningAmber
    MeowTipStyle.Error -> Icons.Rounded.ErrorOutline
}
