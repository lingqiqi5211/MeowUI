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
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import kotlin.math.max
import kotlin.math.min

/** 内置头图按设备形态选择的迷你界面样式。 */
private enum class PreviewFormFactor {
    Phone,
    Foldable,
    Tablet,
}

/**
 * 外观页的内置头图（参考 KernelSU 的主题预览）：一块按当前设备形态绘制的迷你界面，
 * 随 [MeowTheme] 的配色实时着色。手机显示竖屏单栏；展开态折叠屏显示铰链双栏；
 * 平板显示横屏侧栏布局。也可以在自定义外观页中直接复用。
 */
@Composable
fun MeowAppearancePreview(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.toFloat()
    val heightDp = configuration.screenHeightDp.toFloat()
    val shortest = min(widthDp, heightDp)
    val longest = max(widthDp, heightDp)
    val formFactor = when {
        shortest < 600f -> PreviewFormFactor.Phone
        // 展开的折叠屏接近方形；平板长宽比明显更大。
        longest / shortest < 1.25f -> PreviewFormFactor.Foldable
        else -> PreviewFormFactor.Tablet
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (formFactor) {
            PreviewFormFactor.Phone -> PhonePreviewFrame(
                aspectRatio = if (heightDp > 0f) widthDp / heightDp else 0.46f,
            )

            PreviewFormFactor.Foldable -> FoldablePreviewFrame()

            PreviewFormFactor.Tablet -> TabletPreviewFrame()
        }
    }
}

@Composable
private fun PhonePreviewFrame(aspectRatio: Float) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth(0.4f)
            .aspectRatio(aspectRatio.coerceIn(0.4f, 0.6f))
            .clip(shape)
            .border(1.dp, MeowTheme.colors.outline.copy(alpha = 0.4f), shape)
            .background(MeowTheme.colors.background)
            .padding(10.dp),
    ) {
        PreviewTopBar()
        PreviewSpacer(10.dp)
        PreviewBlock(
            modifier = Modifier.weight(1f),
            color = MeowTheme.colors.primary.copy(alpha = 0.28f),
        )
        PreviewSpacer(6.dp)
        PreviewBlock(
            modifier = Modifier.weight(1f),
            color = MeowTheme.colors.surfaceVariant,
        )
        PreviewSpacer(10.dp)
        PreviewBottomBar()
    }
}

@Composable
private fun FoldablePreviewFrame() {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .aspectRatio(1.05f)
            .clip(shape)
            .border(1.dp, MeowTheme.colors.outline.copy(alpha = 0.4f), shape)
            .background(MeowTheme.colors.background)
            .padding(10.dp),
    ) {
        PreviewTopBar()
        PreviewSpacer(10.dp)
        Row(modifier = Modifier.weight(1f)) {
            // 左右两栏，中间是铰链分割线。
            Column(modifier = Modifier.weight(1f)) {
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
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .background(MeowTheme.colors.outline.copy(alpha = 0.3f)),
            )
            Box(modifier = Modifier.weight(1.4f).padding(start = 6.dp)) {
                PreviewBlock(
                    modifier = Modifier.fillMaxSize(),
                    color = MeowTheme.colors.surfaceVariant,
                )
            }
        }
        PreviewSpacer(10.dp)
        PreviewBottomBar()
    }
}

@Composable
private fun TabletPreviewFrame() {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .aspectRatio(1.45f)
            .clip(shape)
            .border(1.dp, MeowTheme.colors.outline.copy(alpha = 0.4f), shape)
            .background(MeowTheme.colors.background)
            .padding(10.dp),
    ) {
        // 左侧导航栏：三个导航点。
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
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            PreviewTopBar()
            PreviewSpacer(8.dp)
            Row(modifier = Modifier.weight(1f)) {
                PreviewBlock(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    color = MeowTheme.colors.primary.copy(alpha = 0.28f),
                )
                PreviewSpacer(6.dp)
                PreviewBlock(
                    modifier = Modifier.weight(1.6f).fillMaxHeight(),
                    color = MeowTheme.colors.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PreviewTopBar() {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
private fun PreviewBottomBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MeowTheme.colors.surfaceVariant)
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        PreviewDot(color = MeowTheme.colors.primary)
        PreviewDot(color = MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
        PreviewDot(color = MeowTheme.colors.onSurfaceVariant.copy(alpha = 0.45f))
    }
}

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
