package io.github.lingqiqi5211.meowui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WrapText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close2
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Report
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.icon.extended.Timer

/**
 * 按当前风格解析的图标集。
 *
 * 和 [MeowTheme.colors] 是同一件事的另一半:Material 的图标是 Google 的字重与圆角,
 * Miuix 的是小米那套细笔画,两者混在一页上比配色不一致更扎眼 —— 顶栏用 Material 的删除
 * 桶、下面的卡片却是 Miuix 的圆角,一眼就能看出是两套东西拼的。
 *
 * 语义命名而不是形状命名:调用侧要表达的是「删除」「筛选」,不是「垃圾桶」「漏斗」。哪套
 * 图标里该用哪个字形是这里的事,风格切换时调用侧不需要跟着改。
 *
 * Miuix 那套只有一份字形、靠字重分档,所以底栏选中态用 Demibold、未选中用 Normal，
 * 对应 Material 的 filled / outlined 一对。
 *
 * 有几处是 Miuix 图标集里确实没有的:它是为系统应用画的,没有警告三角、没有断线的云。
 * 这些落在语义最接近的字形上(警告与崩溃都用 Report,失败用 Close2),而不是在 Miuix
 * 皮肤下混进一个 Material 字形。
 */
object MeowIcons {

    // 顶栏动作。

    val Delete: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.Delete) { MiuixIcons.Delete }

    val Copy: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.ContentCopy) { MiuixIcons.Copy }

    val Share: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.Share) { MiuixIcons.Share }

    val Filter: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.FilterList) { MiuixIcons.Filter }

    /** 自动换行开启:文字排成段落。 */
    val WrapLines: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.WrapText) { MiuixIcons.Notes }

    /** 自动换行关闭:每条独占一行,超出的横向滚动。 */
    val NoWrapLines: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.Notes) { MiuixIcons.ListView }

    // 底栏。成对出现,未选中/选中。

    val Home: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Outlined.Home) { MiuixIcons.Normal.Home }

    val HomeSelected: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.Home) { MiuixIcons.Demibold.Home }

    val Crashes: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Outlined.BugReport) { MiuixIcons.Normal.Report }

    val CrashesSelected: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.BugReport) { MiuixIcons.Demibold.Report }

    val Apps: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Outlined.Apps) { MiuixIcons.Normal.GridView }

    val AppsSelected: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.Apps) { MiuixIcons.Demibold.GridView }

    val Settings: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Outlined.Settings) { MiuixIcons.Normal.Settings }

    val SettingsSelected: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.Settings) { MiuixIcons.Demibold.Settings }

    // 状态。多用在页面级插图或状态徽标里。
    //
    // 这一组一律用线性字形。Material 侧 `CheckCircle` 是实心的,混在 `WarningAmber`、
    // `ErrorOutline`、`Inbox` 这些线条图标里就成了同一页上唯一一个填充块——放进圆形徽标
    // 更明显,看着像另一套图标混进来了。要挑选中/未选中那种「实心 vs 空心」的对比,是底栏
    // 上面那几对的事,状态图标之间不该有这种差别。

    /** 一切正常。 */
    val Healthy: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Outlined.CheckCircle) { MiuixIcons.Ok }

    /** 需要注意,但还能用。 */
    val Warning: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.WarningAmber) { MiuixIcons.Report }

    /** 出错了。 */
    val Error: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.ErrorOutline) { MiuixIcons.Close2 }

    /** 连不上另一端。 */
    val Offline: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Outlined.CloudOff) { MiuixIcons.Info }

    /** 还没发生,等着。 */
    val Pending: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Rounded.HourglassEmpty) { MiuixIcons.Timer }

    /** 空列表。 */
    val Empty: ImageVector
        @Composable @ReadOnlyComposable
        get() = byStyle(Icons.Outlined.Inbox) { MiuixIcons.File }
}

/**
 * Miuix 侧放在 lambda 里:那套字形是取用时才构建并缓存的,Material 皮肤下一次都不该构建。
 * Material 侧的 `Icons.X` 本身就是缓存好的静态量,取一下不额外产生开销。
 */
@Composable
@ReadOnlyComposable
private inline fun byStyle(material: ImageVector, miuix: () -> ImageVector): ImageVector =
    when (MeowTheme.style) {
        MeowUiStyle.MaterialExpressive -> material
        MeowUiStyle.Miuix -> miuix()
    }
