package io.github.lingqiqi5211.meowui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.LocalContentColor as MaterialLocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.lingqiqi5211.meowui.core.MeowUiStyle
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.AddFolder
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.LocalContentColor as MiuixLocalContentColor

/**
 * 「点进去」的行尾箭头，取当前风格自己的那一个。
 *
 * 偏好行（[MeowActionPreference] 的 `navigation`）自己会画箭头；这个是给数据列表里手写的
 * 行用的——那种行的构成由业务决定，走不了偏好行，但箭头不该因此退回某一套风格的图标。
 */
@Composable
fun MeowNavigateIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialIcon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint.takeIf { it != Color.Unspecified } ?: MaterialLocalContentColor.current,
            )
        },
        miuix = {
            MiuixIcon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint.takeIf { it != Color.Unspecified } ?: MiuixLocalContentColor.current,
            )
        },
    )
}

/**
 * 文件夹图标。文件列表在两套风格下各有各的画法，Material 用 M3 的实心文件夹，Miuix 用
 * miuix 自己那套线性图标——混用会让同一个列表看着来自两个应用。
 */
@Composable
fun MeowFolderIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
) {
    MeowStyleIcon(
        material = Icons.Rounded.Folder,
        miuix = MiuixIcons.Folder,
        modifier = modifier,
        contentDescription = contentDescription,
        tint = tint,
    )
}

/** 文件图标，与 [MeowFolderIcon] 成对。 */
@Composable
fun MeowFileIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
) {
    MeowStyleIcon(
        material = Icons.Rounded.Description,
        miuix = MiuixIcons.File,
        modifier = modifier,
        contentDescription = contentDescription,
        tint = tint,
    )
}

/** 排序图标，给顶栏的排序入口用。 */
@Composable
fun MeowSortIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
) {
    MeowStyleIcon(
        material = Icons.AutoMirrored.Rounded.Sort,
        miuix = MiuixIcons.Sort,
        modifier = modifier,
        contentDescription = contentDescription,
        tint = tint,
    )
}

/**
 * 顶栏动作只收 `ImageVector`（图标由 MeowUI 内部按风格渲染），所以这里也给一份按风格取
 * 矢量的入口，不经过 Composable 的图标外壳。
 */
object MeowIcons {

    val folder: ImageVector
        @Composable get() = meowStyleVector(Icons.Rounded.Folder, MiuixIcons.Folder)

    val file: ImageVector
        @Composable get() = meowStyleVector(Icons.Rounded.Description, MiuixIcons.File)

    val sort: ImageVector
        @Composable get() = meowStyleVector(Icons.AutoMirrored.Rounded.Sort, MiuixIcons.Sort)

    val navigateInto: ImageVector
        @Composable get() = meowStyleVector(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            MiuixIcons.Basic.ArrowRight,
        )

    /** 确认/提交，顶栏右上角那一枚。 */
    val confirm: ImageVector
        @Composable get() = meowStyleVector(Icons.Rounded.Check, MiuixIcons.Ok)

    /** 选项/调节，多用于顶栏的设置类菜单入口。 */
    val options: ImageVector
        @Composable get() = meowStyleVector(Icons.Rounded.Tune, MiuixIcons.Tune)

    val refresh: ImageVector
        @Composable get() = meowStyleVector(Icons.Rounded.Refresh, MiuixIcons.Refresh)

    /** 新增/添加。 */
    val add: ImageVector
        @Composable get() = meowStyleVector(Icons.Rounded.Add, MiuixIcons.Add)

    /** 新建文件夹。 */
    val createFolder: ImageVector
        @Composable get() = meowStyleVector(Icons.Rounded.CreateNewFolder, MiuixIcons.AddFolder)

    /** 返回上一步——页内的一步，不是页面级返回（那个由顶栏自己画）。 */
    val back: ImageVector
        @Composable get() = meowStyleVector(
            Icons.AutoMirrored.Rounded.ArrowBack,
            MiuixIcons.Back,
        )

    /** 从列表里移掉一项（不是删除数据）。 */
    val remove: ImageVector
        @Composable get() = meowStyleVector(Icons.Rounded.Close, MiuixIcons.Close)

    /** 删除。 */
    val delete: ImageVector
        @Composable get() = meowStyleVector(Icons.Rounded.Delete, MiuixIcons.Delete)
}

@Composable
private fun meowStyleVector(material: ImageVector, miuix: ImageVector): ImageVector =
    when (MeowTheme.style) {
        MeowUiStyle.MaterialExpressive -> material
        MeowUiStyle.Miuix -> miuix
    }

@Composable
private fun MeowStyleIcon(
    material: ImageVector,
    miuix: ImageVector,
    modifier: Modifier,
    contentDescription: String?,
    tint: Color,
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialIcon(
                imageVector = material,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint.takeIf { it != Color.Unspecified } ?: MaterialLocalContentColor.current,
            )
        },
        miuix = {
            MiuixIcon(
                imageVector = miuix,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint.takeIf { it != Color.Unspecified } ?: MiuixLocalContentColor.current,
            )
        },
    )
}
