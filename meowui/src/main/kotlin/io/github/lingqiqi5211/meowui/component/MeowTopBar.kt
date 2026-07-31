package io.github.lingqiqi5211.meowui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import io.github.lingqiqi5211.meowui.theme.LocalMeowBlurEnabled
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayCascadingListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * A top bar action.
 *
 * The bar renders these itself in the active style, so [modifier] is how the call
 * site reaches the resulting control — to tag it for tests, or to constrain it.
 * It is applied to the button each style builds, not to a wrapper, so semantics
 * merge onto the same node that carries the click.
 */
sealed interface MeowTopBarAction {
    val enabled: Boolean

    /** The modifier applied to the control this action renders as. */
    val modifier: Modifier

    data class Icon(
        val icon: ImageVector,
        val contentDescription: String,
        override val modifier: Modifier = Modifier,
        override val enabled: Boolean = true,
        val onClick: () -> Unit,
    ) : MeowTopBarAction

    data class Text(
        val text: String,
        override val modifier: Modifier = Modifier,
        override val enabled: Boolean = true,
        val onClick: () -> Unit,
    ) : MeowTopBarAction

    data class Menu(
        val icon: ImageVector,
        val contentDescription: String,
        val items: List<MeowMenuItem>,
        override val modifier: Modifier = Modifier,
        override val enabled: Boolean = true,
    ) : MeowTopBarAction
}

/**
 * 菜单项。[children] 非空时该项成为子菜单入口（点击展开下一级，[onClick] 被忽略）：
 * Miuix 使用原生级联弹窗堆叠展开，Material 在同一弹窗内下钻并提供返回上级的行。
 */
data class MeowMenuItem(
    val text: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {},
    val children: List<MeowMenuItem> = emptyList(),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MeowTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    onBackClick: (() -> Unit)? = null,
    navigationModifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actionItems: List<MeowTopBarAction> = emptyList(),
) {
    val effect = LocalMeowScaffoldEffect.current
    val scrollContext = LocalMeowScrollContext.current
    // 主题开启模糊且顶栏在 MeowScaffold 里(拿得到内容图层快照)时,顶栏变成磨砂:
    // 自身底色透明,由背景模糊 + 半透明底色承担容器色。内容需要滚到顶栏下方才看
    // 得出效果,由页面把 scaffold 顶部内边距交给滚动容器实现。
    val backdrop = LocalMeowBackdrop.current
    val useBlur = LocalMeowBlurEnabled.current &&
        backdrop != null &&
        isRuntimeShaderSupported()
    val effectModifier = modifier.then(effect.topBarModifier)
    // navigationIcon 优先；只给 onBackClick 时渲染风格原生的返回按钮。
    // navigationModifier 只作用于内置返回按钮；自定义 navigationIcon 由调用侧自己带 modifier。
    val resolvedNavigationIcon: (@Composable () -> Unit)? = navigationIcon
        ?: onBackClick?.let { back ->
            { MeowBackIconButton(onClick = back, modifier = navigationModifier) }
        }

    MeowStyleContent(
        materialExpressive = {
            val baseColor = effect.topBarContainerColor
                ?: MaterialTheme.colorScheme.surfaceContainer
            val containerColor = if (useBlur) Color.Transparent else baseColor
            val colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                scrolledContainerColor = containerColor,
            )
            val barModifier = if (useBlur && backdrop != null) {
                effectModifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { RectangleShape },
                    effects = { blur(MeowBlurRadius.toPx(), MeowBlurRadius.toPx()) },
                    onDrawSurface = { drawRect(baseColor.copy(alpha = MeowBlurSurfaceAlpha)) },
                )
            } else {
                effectModifier
            }

            MaterialLargeFlexibleTopAppBar(
                title = { MaterialText(title) },
                modifier = barModifier,
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
            val baseColor = effect.topBarContainerColor ?: MiuixTheme.colorScheme.surface
            val barModifier = if (useBlur && backdrop != null) {
                effectModifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { RectangleShape },
                    effects = { blur(MeowBlurRadius.toPx(), MeowBlurRadius.toPx()) },
                    onDrawSurface = { drawRect(baseColor.copy(alpha = MeowBlurSurfaceAlpha)) },
                )
            } else {
                effectModifier
            }
            MiuixTopAppBar(
                title = title,
                modifier = barModifier,
                color = if (useBlur) Color.Transparent else baseColor,
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
internal fun MeowBackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MeowStyleContent(
        materialExpressive = {
            MaterialIconButton(onClick = onClick, modifier = modifier) {
                MaterialIcon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        miuix = {
            MiuixIconButton(onClick = onClick, modifier = modifier) {
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
                modifier = action.modifier,
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
                modifier = action.modifier,
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
    // Material 无原生级联弹窗:子菜单在同一弹窗内下钻,顶部提供返回上级的行。
    var submenu by remember { mutableStateOf<MeowMenuItem?>(null) }
    LaunchedEffect(expanded) {
        if (!expanded) submenu = null
    }

    MaterialIconButton(
        onClick = { expanded = true },
        modifier = action.modifier,
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
                val parent = submenu
                val items = parent?.children ?: action.items
                val headerCount = if (parent != null) 1 else 0
                val count = items.size + headerCount
                parent?.let {
                    MaterialDropdownMenuItem(
                        onClick = { submenu = null },
                        text = { MaterialText(it.text) },
                        shape = MenuDefaults.itemShape(index = 0, count = count).shape,
                        leadingIcon = {
                            MaterialIcon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = null,
                            )
                        },
                    )
                }
                items.forEachIndexed { index, item ->
                    MaterialDropdownMenuItem(
                        onClick = {
                            if (item.children.isNotEmpty()) {
                                submenu = item
                            } else {
                                expanded = false
                                item.onClick()
                            }
                        },
                        text = { MaterialText(item.text) },
                        shape = MenuDefaults.itemShape(
                            index = index + headerCount,
                            count = count,
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
                        trailingIcon = if (item.children.isNotEmpty()) {
                            {
                                MaterialIcon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            null
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
                modifier = action.modifier,
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
                modifier = action.modifier,
                enabled = action.enabled,
            )
        }

        is MeowTopBarAction.Menu -> MiuixTopBarMenu(action)
    }
}

@Composable
private fun MiuixTopBarMenu(action: MeowTopBarAction.Menu) {
    val entry = remember(action.items) {
        DropdownEntry(items = action.items.map { it.toMiuixDropdownItem() })
    }

    // 级联需要 CascadingListPopup;OverlayIconDropdownMenu 只渲染平铺列表,会忽略 children。
    var expanded by remember { mutableStateOf(false) }
    MiuixIconButton(
        onClick = { expanded = true },
        modifier = action.modifier,
        enabled = action.enabled && action.items.isNotEmpty(),
    ) {
        MiuixIcon(
            imageVector = action.icon,
            contentDescription = action.contentDescription,
        )
        OverlayCascadingListPopup(
            show = expanded,
            entries = listOf(entry),
            onDismissRequest = { expanded = false },
            renderInRootScaffold = false,
        )
    }
}

/** 递归转换为 miuix DropdownItem;children 非空时由 miuix 原生级联弹窗堆叠展开。 */
private fun MeowMenuItem.toMiuixDropdownItem(): DropdownItem = DropdownItem(
    text = text,
    enabled = enabled,
    onClick = onClick,
    icon = icon?.let { image ->
        { modifier ->
            MiuixIcon(
                imageVector = image,
                contentDescription = null,
                modifier = modifier,
            )
        }
    },
    children = children.takeIf { it.isNotEmpty() }?.map { it.toMiuixDropdownItem() },
)
