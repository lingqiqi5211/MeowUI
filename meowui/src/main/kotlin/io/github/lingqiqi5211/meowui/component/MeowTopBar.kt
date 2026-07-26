package io.github.lingqiqi5211.meowui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.DropdownMenuGroup as MaterialDropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem as MaterialDropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup as MaterialDropdownMenuPopup
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.LargeFlexibleTopAppBar as MaterialLargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as MaterialText
import androidx.compose.material3.TextButton as MaterialTextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme

sealed interface MeowTopBarAction {
    val enabled: Boolean

    data class Icon(
        val icon: ImageVector,
        val contentDescription: String,
        override val enabled: Boolean = true,
        val onClick: () -> Unit,
    ) : MeowTopBarAction

    data class Text(
        val text: String,
        override val enabled: Boolean = true,
        val onClick: () -> Unit,
    ) : MeowTopBarAction

    data class Menu(
        val icon: ImageVector,
        val contentDescription: String,
        val items: List<MeowMenuItem>,
        override val enabled: Boolean = true,
    ) : MeowTopBarAction
}

data class MeowMenuItem(
    val text: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MeowTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    onBackClick: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actionItems: List<MeowTopBarAction> = emptyList(),
) {
    val effect = LocalMeowScaffoldEffect.current
    val scrollContext = LocalMeowScrollContext.current
    val effectModifier = modifier.then(effect.topBarModifier)
    // navigationIcon 优先；只给 onBackClick 时渲染风格原生的返回按钮。
    val resolvedNavigationIcon: (@Composable () -> Unit)? = navigationIcon
        ?: onBackClick?.let { back -> { MeowBackIconButton(onClick = back) } }

    MeowStyleContent(
        materialExpressive = {
            val containerColor = effect.topBarContainerColor
                ?: MaterialTheme.colorScheme.surfaceContainer
            val colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                scrolledContainerColor = containerColor,
            )

            MaterialLargeFlexibleTopAppBar(
                title = { MaterialText(title) },
                modifier = effectModifier,
                subtitle = subtitle.takeIf(String::isNotBlank)?.let { text ->
                    { MaterialText(text) }
                },
                navigationIcon = { resolvedNavigationIcon?.invoke() },
                actions = {
                    actionItems.forEach { MaterialTopBarAction(it) }
                },
                colors = colors,
                scrollBehavior = scrollContext.materialTopBar,
            )
        },
        miuix = {
            MiuixTopAppBar(
                title = title,
                modifier = effectModifier,
                color = effect.topBarContainerColor ?: MiuixTheme.colorScheme.surface,
                subtitle = subtitle,
                navigationIcon = { resolvedNavigationIcon?.invoke() },
                actions = {
                    actionItems.forEach { MiuixTopBarAction(it) }
                },
                scrollBehavior = scrollContext.miuixTopBar,
            )
        },
    )
}

@Composable
internal fun MeowBackIconButton(onClick: () -> Unit) {
    MeowStyleContent(
        materialExpressive = {
            MaterialIconButton(onClick = onClick) {
                MaterialIcon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        miuix = {
            MiuixIconButton(onClick = onClick) {
                MiuixIcon(
                    imageVector = MiuixIcons.Back,
                    contentDescription = "Back",
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
        },
    )
}

@Composable
private fun MaterialTopBarAction(action: MeowTopBarAction) {
    when (action) {
        is MeowTopBarAction.Icon -> {
            MaterialIconButton(
                onClick = action.onClick,
                enabled = action.enabled,
            ) {
                MaterialIcon(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                )
            }
        }

        is MeowTopBarAction.Text -> {
            MaterialTextButton(
                onClick = action.onClick,
                enabled = action.enabled,
            ) {
                MaterialText(action.text)
            }
        }

        is MeowTopBarAction.Menu -> MaterialTopBarMenu(action)
    }
}

@Composable
private fun MaterialTopBarMenu(action: MeowTopBarAction.Menu) {
    var expanded by remember { mutableStateOf(false) }

    MaterialIconButton(
        onClick = { expanded = true },
        enabled = action.enabled && action.items.isNotEmpty(),
    ) {
        MaterialIcon(
            imageVector = action.icon,
            contentDescription = action.contentDescription,
        )
        // Expressive 菜单:分组大圆角容器 + 按位置计算的分段 item 形状。
        MaterialDropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            MaterialDropdownMenuGroup(
                shapes = MenuDefaults.groupShapes(),
            ) {
                action.items.forEachIndexed { index, item ->
                    MaterialDropdownMenuItem(
                        onClick = {
                            expanded = false
                            item.onClick()
                        },
                        text = { MaterialText(item.text) },
                        shape = MenuDefaults.itemShape(
                            index = index,
                            count = action.items.size,
                        ).shape,
                        enabled = item.enabled,
                        leadingIcon = item.icon?.let { icon ->
                            {
                                MaterialIcon(
                                    imageVector = icon,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MiuixTopBarAction(action: MeowTopBarAction) {
    when (action) {
        is MeowTopBarAction.Icon -> {
            MiuixIconButton(
                onClick = action.onClick,
                enabled = action.enabled,
            ) {
                MiuixIcon(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                )
            }
        }

        is MeowTopBarAction.Text -> {
            MiuixTextButton(
                text = action.text,
                onClick = action.onClick,
                enabled = action.enabled,
            )
        }

        is MeowTopBarAction.Menu -> MiuixTopBarMenu(action)
    }
}

@Composable
private fun MiuixTopBarMenu(action: MeowTopBarAction.Menu) {
    val entry = remember(action.items) {
        DropdownEntry(
            items = action.items.map { item ->
                DropdownItem(
                    text = item.text,
                    enabled = item.enabled,
                    onClick = item.onClick,
                    icon = item.icon?.let { icon ->
                        { modifier ->
                            MiuixIcon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = modifier,
                            )
                        }
                    },
                )
            },
        )
    }

    OverlayIconDropdownMenu(
        entry = entry,
        enabled = action.enabled,
        collapseOnSelection = true,
    ) {
        MiuixIcon(
            imageVector = action.icon,
            contentDescription = action.contentDescription,
        )
    }
}
