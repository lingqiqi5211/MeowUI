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
 * 外观页的内置头图：一块按当前窗口和当前设置绘制的迷你界面。
 *
 * 画的是「现在这台设备、按这些设置，界面长什么样」，所以下面几项改完这里立刻跟着变：
 *
 * - 机型与横竖屏：直接照当前窗口的长宽比，折叠屏另外画一条铰链线。
 * - 导航栏位置：窄屏在底部，够宽移到左侧，再宽的话右边还会分出详情栏。判据与
 *   [MeowAdaptiveLayout] 同源，见 [MeowWindowWidth] 与 [MeowWindowHeight]。
 * - 悬浮导航栏：底栏画成一颗离边的胶囊，而不是通栏。
 * - 背景模糊：内容铺到顶栏底栏下面，栏本身画成半透明，透出后面的色块。
 * - 取色与主题：配色全部取自 [MeowTheme]，主题色、深浅、AMOLED 都在里面。
 */
@Composable
fun MeowAppearancePreview(
    appearance: MeowAppearance,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.toFloat()
    val heightDp = configuration.screenHeightDp.toFloat()
    val shortest = min(widthDp, heightDp)
    val longest = max(widthDp, heightDp)
    // 展开的折叠屏接近方形，平板长宽比明显更大。
    val foldable = shortest >= 600f && longest / shortest < 1.25f
    val sideRail = widthDp >= MeowWindowWidth.Medium.value
    val twoPane = widthDp >= MeowWindowWidth.Expanded.value &&
        heightDp >= MeowWindowHeight.Medium.value

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        PreviewFrame(
            aspectRatio = if (heightDp > 0f) widthDp / heightDp else 0.46f,
            foldable = foldable,
            sideRail = sideRail,
            twoPane = twoPane,
            floatingBar = appearance.floatingNavigationBarEnabled,
            blur = appearance.blurEnabled,
        )
    }
}

@Composable
private fun PreviewFrame(
    aspectRatio: Float,
    foldable: Boolean,
    sideRail: Boolean,
    twoPane: Boolean,
    floatingBar: Boolean,
    blur: Boolean,
) {
    val shape = RoundedCornerShape(if (aspectRatio > 1f) 18.dp else 20.dp)
    // 横向的机身画宽一点，竖着的画窄一点，两者在头图里高度差不多。
    val widthFraction = if (aspectRatio > 1f) 0.52f else 0.4f
    Row(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .aspectRatio(aspectRatio.coerceIn(0.4f, 1.9f))
            .clip(shape)
            .border(1.dp, MeowTheme.colors.outline.copy(alpha = 0.4f), shape)
            .background(MeowTheme.colors.background)
            .padding(10.dp),
    ) {
        if (sideRail) {
            PreviewRail()
            PreviewSpacer(8.dp)
        }
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            PreviewContent(twoPane = twoPane, foldable = foldable && !sideRail)
            PreviewTopBar(
                modifier = Modifier.align(Alignment.TopCenter),
                blur = blur,
            )
            if (!sideRail) {
                PreviewBottomBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    floating = floatingBar,
                    blur = blur,
                )
            }
        }
    }
}

/** 内容区。开了模糊时它一直铺到顶栏底栏底下，栏是半透明盖上去的。 */
@Composable
private fun PreviewContent(twoPane: Boolean, foldable: Boolean) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            PreviewBlock(
                modifier = Modifier.weight(1f),
                color = MeowTheme.colors.primary.copy(alpha = 0.28f),
            )
            PreviewSpacer(6.dp)
            PreviewBlock(
                modifier = Modifier.weight(1f),
                color = MeowTheme.colors.surfaceVariant,
            )
        }
        if (foldable) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .background(MeowTheme.colors.outline.copy(alpha = 0.3f)),
            )
        }
        if (twoPane || foldable) {
            PreviewSpacer(6.dp)
            PreviewBlock(
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                color = MeowTheme.colors.surfaceVariant,
            )
        }
    }
}

/** 左侧导航栏。 */
@Composable
private fun PreviewRail() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(MeowTheme.colors.surfaceVariant)
            .padding(horizontal = 5.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PreviewDot(color = MeowTheme.colors.primary)
        PreviewDot(color = MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
        PreviewDot(color = MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
    }
}

@Composable
private fun PreviewTopBar(modifier: Modifier, blur: Boolean) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MeowTheme.colors.background.copy(alpha = barAlpha(blur)))
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MeowTheme.colors.primary),
        )
        Box(
            modifier = Modifier
                .padding(start = 5.dp)
                .size(width = 32.dp, height = 7.dp)
                .clip(CircleShape)
                .background(MeowTheme.colors.onBackground.copy(alpha = 0.55f)),
        )
    }
}

@Composable
private fun PreviewBottomBar(modifier: Modifier, floating: Boolean, blur: Boolean) {
    Row(
        modifier = modifier
            .then(if (floating) Modifier.padding(bottom = 4.dp) else Modifier.fillMaxWidth())
            .clip(CircleShape)
            .background(MeowTheme.colors.surfaceVariant.copy(alpha = barAlpha(blur)))
            .padding(horizontal = if (floating) 10.dp else 0.dp, vertical = 5.dp),
        horizontalArrangement = if (floating) {
            Arrangement.spacedBy(10.dp)
        } else {
            Arrangement.SpaceEvenly
        },
    ) {
        PreviewDot(color = MeowTheme.colors.primary)
        PreviewDot(color = MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
        PreviewDot(color = MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
    }
}

/** 顶栏底栏的不透明度：开了模糊就透出后面的内容，关了就是实心。 */
private fun barAlpha(blur: Boolean): Float = if (blur) 0.72f else 1f

@Composable
private fun PreviewDot(color: Color) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun PreviewBlock(modifier: Modifier, color: Color) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color),
    )
}

@Composable
private fun PreviewSpacer(size: Dp) {
    Spacer(Modifier.size(size))
}
