package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.MeowAppearance
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import kotlin.math.max
import kotlin.math.min

/**
 * 外观页的头图：按当前窗口和当前设置画一块迷你界面。
 *
 * 下面几项改完，这里立刻跟着变：
 *
 * - **机型与横竖屏**：机身长宽比直接照当前窗口，折叠屏另画一条铰链线。
 * - **导航栏位置**：窄屏在底部，够宽移到左侧，再宽右边还会分出详情栏。判据与
 *   [MeowAdaptiveLayout] 同源，见 [MeowWindowWidth] 与 [MeowWindowHeight]。
 * - **侧边导航栏**：这是调用侧的可选特性（[sideNavigationRail]）。应用没接就不画侧栏，
 *   底栏在多宽的窗口上都留在底部。
 * - **悬浮导航栏**：底栏收成一颗离边的胶囊，而不是通栏贴底；它开着时侧栏不出现。
 * - **背景模糊**：开着时内容铺到顶栏底栏下面、栏画成半透明；关着时内容与栏上下排开。
 *   两者是结构差异，不只是透明度差一点。
 * - **取色与主题**：三块内容分别用主色、次色、第三色的容器色，配色一换三块一起变。
 *
 * 高度是定死的（横屏更矮），机身按长宽比算宽度。头图不该占掉一屏，把下面的配置项挤出去。
 */
@Composable
fun MeowAppearancePreview(
    appearance: MeowAppearance,
    modifier: Modifier = Modifier,
    sideNavigationRail: Boolean = true,
) {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.toFloat()
    val heightDp = configuration.screenHeightDp.toFloat()
    val shortest = min(widthDp, heightDp)
    val longest = max(widthDp, heightDp)
    val landscape = widthDp > heightDp
    // 展开的折叠屏接近方形，平板长宽比明显更大。
    val foldable = shortest >= 600f && longest / shortest < 1.25f
    val floatingBar = appearance.floatingNavigationBarEnabled
    // 侧栏是调用侧的可选特性；悬浮底栏是使用者挑的样子，宽屏也留在底下，那时同样不出侧栏。
    val rail = sideNavigationRail && !floatingBar && widthDp >= MeowWindowWidth.Medium.value
    val twoPane = widthDp >= MeowWindowWidth.Expanded.value &&
        heightDp >= MeowWindowHeight.Medium.value

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        PreviewFrame(
            height = if (landscape) LandscapeFrameHeight else PortraitFrameHeight,
            aspectRatio = if (heightDp > 0f) widthDp / heightDp else 0.46f,
            foldable = foldable,
            sideRail = rail,
            twoPane = twoPane,
            floatingBar = floatingBar,
            blur = appearance.blurEnabled,
        )
    }
}

@Composable
private fun PreviewFrame(
    height: Dp,
    aspectRatio: Float,
    foldable: Boolean,
    sideRail: Boolean,
    twoPane: Boolean,
    floatingBar: Boolean,
    blur: Boolean,
) {
    val shape = RoundedCornerShape(FrameCorner)
    Row(
        modifier = Modifier
            .height(height)
            .aspectRatio(aspectRatio.coerceIn(0.46f, 2.0f))
            .clip(shape)
            .border(1.dp, MeowTheme.colors.outline.copy(alpha = 0.4f), shape)
            .background(MeowTheme.colors.background)
            .padding(FramePadding),
    ) {
        if (sideRail) {
            PreviewRail()
            Spacer(Modifier.width(FrameGap))
        }
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (blur) {
                // 内容铺到顶栏底栏下面，栏半透明盖上去。
                PreviewContent(Modifier.fillMaxSize(), twoPane, foldable && !sideRail)
                PreviewTopBar(Modifier.align(Alignment.TopCenter), translucent = true)
                if (!sideRail) {
                    PreviewBottomBar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .then(if (floatingBar) Modifier.padding(bottom = 4.dp) else Modifier),
                        floating = floatingBar,
                        translucent = true,
                    )
                }
            } else {
                // 不模糊：栏是实心的，内容让开它们。
                Column(modifier = Modifier.fillMaxSize()) {
                    PreviewTopBar(Modifier, translucent = false)
                    Spacer(Modifier.height(FrameGap))
                    PreviewContent(Modifier.weight(1f), twoPane, foldable && !sideRail)
                    if (!sideRail) {
                        Spacer(Modifier.height(FrameGap))
                        PreviewBottomBar(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            floating = floatingBar,
                            translucent = false,
                        )
                    }
                }
            }
        }
    }
}

/** 内容区。三块分别用主色、次色、第三色，配色一换整块跟着变。 */
@Composable
private fun PreviewContent(modifier: Modifier, twoPane: Boolean, foldable: Boolean) {
    Row(modifier = modifier) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            PreviewBlock(
                modifier = Modifier.weight(1f),
                color = MeowTheme.colors.primaryContainer,
            )
            Spacer(Modifier.height(BlockGap))
            PreviewBlock(
                modifier = Modifier.weight(1f),
                color = MeowTheme.colors.secondaryContainer,
            )
        }
        if (foldable) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(MeowTheme.colors.outline.copy(alpha = 0.3f)),
            )
        }
        if (twoPane || foldable) {
            Spacer(Modifier.width(BlockGap))
            PreviewBlock(
                modifier = Modifier.weight(1.4f).fillMaxHeight(),
                color = MeowTheme.colors.tertiaryContainer,
            )
        }
    }
}

@Composable
private fun PreviewRail() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(MeowTheme.colors.surfaceVariant)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PreviewDot(MeowTheme.colors.primary)
        PreviewDot(MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
        PreviewDot(MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
    }
}

@Composable
private fun PreviewTopBar(modifier: Modifier, translucent: Boolean) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BlockCorner))
            .background(MeowTheme.colors.surfaceVariant.copy(alpha = barAlpha(translucent)))
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(DotSize)
                .clip(CircleShape)
                .background(MeowTheme.colors.primary),
        )
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .size(width = 26.dp, height = 5.dp)
                .clip(CircleShape)
                .background(MeowTheme.colors.onBackground.copy(alpha = 0.5f)),
        )
    }
}

@Composable
private fun PreviewBottomBar(
    modifier: Modifier,
    floating: Boolean,
    translucent: Boolean,
) {
    Row(
        modifier = modifier
            .then(if (floating) Modifier else Modifier.fillMaxWidth())
            .clip(CircleShape)
            .background(MeowTheme.colors.surfaceVariant.copy(alpha = barAlpha(translucent)))
            .padding(horizontal = if (floating) 8.dp else 0.dp, vertical = 4.dp),
        horizontalArrangement = if (floating) {
            Arrangement.spacedBy(8.dp)
        } else {
            Arrangement.SpaceEvenly
        },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PreviewDot(MeowTheme.colors.primary)
        PreviewDot(MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
        PreviewDot(MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
    }
}

/** 半透明的那一档要能一眼看出后面有东西，所以压得比较低。 */
private fun barAlpha(translucent: Boolean): Float = if (translucent) 0.55f else 1f

@Composable
private fun PreviewDot(color: Color) {
    Box(
        modifier = Modifier
            .size(DotSize)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun PreviewBlock(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BlockCorner))
            .background(color),
    )
}

private val PortraitFrameHeight = 190.dp

private val LandscapeFrameHeight = 128.dp

private val FrameCorner = 14.dp

private val FramePadding = 6.dp

private val FrameGap = 5.dp

private val BlockGap = 4.dp

private val BlockCorner = 6.dp

private val DotSize = 6.dp
