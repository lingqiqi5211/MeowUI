package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.layout.Arrangement
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
import io.github.lingqiqi5211.meowui.theme.LocalMeowDarkTheme
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
    actionModifier: Modifier = Modifier,
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
                    iconTint = contentColor,
                    titleColor = contentColor,
                    bodyColor = contentColor,
                    action = actionText?.let { text ->
                        {
                            MaterialTextButton(
                                onClick = onAction!!,
                                modifier = actionModifier,
                            ) {
                                MaterialText(text)
                            }
                        }
                    },
                )
            }
        },
        miuix = {
            // KernelSU 的 Miuix 告警卡形态:警告/错误用淡色容器整卡着色,
            // 一般信息保持普通卡面、只用图标点出语义。
            val colors = miuixTipColors(style)
            MiuixCard(
                modifier = modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                insideMargin = PaddingValues(0.dp),
                colors = MiuixCardColors(
                    color = colors.container,
                    contentColor = colors.content,
                ),
            ) {
                TipContent(
                    title = title,
                    message = message,
                    icon = resolvedIcon,
                    iconTint = colors.accent,
                    titleColor = colors.content,
                    bodyColor = colors.body,
                    action = actionText?.let { text ->
                        {
                            MiuixTextButton(
                                text = text,
                                onClick = onAction!!,
                                modifier = actionModifier,
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
    /**
     * Miuix 指示器下方的四条状态文案:下拉/到位/刷新中/完成。
     *
     * 默认为空(不显示文字)。miuix 的内置文案是英文,库里也没有本地化设施,
     * 需要文字的调用侧自己传本地化好的四条。Material 分支的指示器无文字,忽略。
     */
    refreshTexts: List<String> = emptyList(),
    /**
     * 所在 MeowScaffold 给内容区的内边距,用于把刷新指示器压到顶栏下方。
     *
     * 默认自动取当前 scaffold 的值。若调用侧已经自己把这份内边距应用到了内容上
     * （内容并不伸到顶栏底下），必须传 `PaddingValues(0.dp)`,否则顶栏高度会被
     * 叠加两次,指示器出现的位置明显偏低。
     */
    scaffoldPadding: PaddingValues = LocalMeowScaffoldContentPadding.current,
    content: @Composable () -> Unit,
) {
    // 刷新指示器从顶栏下方出现，而不是盖在顶栏上。
    val indicatorTopPadding =
        scaffoldPadding.calculateTopPadding() + contentPadding.calculateTopPadding()

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
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = indicatorTopPadding),
                    )
                },
            ) {
                // 连接挂在内侧（比刷新逻辑更靠近列表）：顶栏折叠着时下拉，
                // 增量先喂给顶栏展开，展开完毕剩余才进入下拉刷新。
                Box(
                    modifier = Modifier
                        .padding(contentPadding)
                        .meowScaffoldScroll(),
                ) {
                    content()
                }
            }
        },
        miuix = {
            // miuix 只用 contentPadding.top 偏移指示器，内容垫边由外层 Box 负责。
            MiuixPullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier,
                contentPadding = PaddingValues(top = indicatorTopPadding),
                refreshTexts = refreshTexts,
                topAppBarScrollBehavior = LocalMeowScrollContext.current.miuixTopBar,
                content = {
                    // 同 Material 分支：下拉先展开折叠着的顶栏，再进入刷新。
                    Box(
                        modifier = Modifier
                            .padding(contentPadding)
                            .meowScaffoldScroll(),
                    ) {
                        content()
                    }
                },
            )
        },
    )
}

@Composable
private fun TipContent(
    title: String?,
    message: String,
    icon: ImageVector,
    iconTint: Color,
    titleColor: Color,
    bodyColor: Color,
    action: (@Composable () -> Unit)?,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MeowStyleContent(
                materialExpressive = {
                    MaterialIcon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconTint,
                    )
                },
                miuix = {
                    MiuixIcon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconTint,
                    )
                },
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                title?.takeIf(String::isNotBlank)?.let { text ->
                    MeowText(
                        text = text,
                        color = titleColor,
                        style = MeowTheme.typography.title,
                    )
                }
                MeowText(
                    text = message,
                    color = bodyColor,
                    style = MeowTheme.typography.summary,
                )
        }
        }
        // 动作独占一行、靠右。和图标、正文挤在同一行时，正文被压成很窄的一栏——
        // 有标题、正文又不止一句的提示卡尤其明显。
        action?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                it()
            }
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

private data class MiuixTipColors(
    val container: Color,
    val content: Color,
    val body: Color,
    val accent: Color,
)

/**
 * Miuix 提示卡配色,参考 KernelSU 的告警卡:警告/错误整卡淡色容器,信息类保持
 * 普通卡面。miuix 色板没有 tertiary/secondaryContainer,警告色调用固定淡色,
 * 深浅各一套。
 */
@Composable
private fun miuixTipColors(style: MeowTipStyle): MiuixTipColors {
    val dark = LocalMeowDarkTheme.current
    return when (style) {
        MeowTipStyle.Info, MeowTipStyle.Success -> MiuixTipColors(
            container = MiuixTheme.colorScheme.surfaceContainer,
            content = MiuixTheme.colorScheme.onSurface,
            body = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            accent = MiuixTheme.colorScheme.primary,
        )
        MeowTipStyle.Warning -> if (dark) {
            MiuixTipColors(
                container = Color(0xFF3B301A),
                content = Color(0xFFEBD6A0),
                body = Color(0xFFD9C083),
                accent = Color(0xFFEBD6A0),
            )
        } else {
            MiuixTipColors(
                container = Color(0xFFFBF0D9),
                content = Color(0xFF6E5410),
                body = Color(0xFF83671F),
                accent = Color(0xFF8A6D1D),
            )
        }
        // miuix 自带的 errorContainer 淡得几乎看不出是错误;错误也用固定色调,
        // 深浅各一套,饱和度对齐 KernelSU 的告警红。
        MeowTipStyle.Error -> if (dark) {
            MiuixTipColors(
                container = Color(0xFF4A211D),
                content = Color(0xFFF6B9B2),
                body = Color(0xFFE8A49D),
                accent = Color(0xFFF6B9B2),
            )
        } else {
            MiuixTipColors(
                container = Color(0xFFF6D2CD),
                content = Color(0xFF7F1D14),
                body = Color(0xFF933227),
                accent = Color(0xFF9C271B),
            )
        }
    }
}

private fun MeowTipStyle.defaultIcon(): ImageVector = when (this) {
    MeowTipStyle.Info -> Icons.Rounded.Info
    MeowTipStyle.Success -> Icons.Rounded.CheckCircle
    MeowTipStyle.Warning -> Icons.Rounded.WarningAmber
    MeowTipStyle.Error -> Icons.Rounded.ErrorOutline
}
