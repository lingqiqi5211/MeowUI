package io.github.lingqiqi5211.meowui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DockedSearchBar as MaterialDockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.SearchBarDefaults as MaterialSearchBarDefaults
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.lingqiqi5211.meowui.theme.MeowStyleContent
import io.github.lingqiqi5211.meowui.theme.MeowTheme
import top.yukonga.miuix.kmp.basic.InputField as MiuixInputField
import top.yukonga.miuix.kmp.basic.SearchBar as MiuixSearchBar
import top.yukonga.miuix.kmp.basic.Text as MiuixText

/**
 * 搜索框：折叠时是一条输入框，聚焦展开后在下方显示 [content]（搜索结果）。
 *
 * 展开状态由调用侧持有：输入框获得焦点时回调 `onExpandedChange(true)`，
 * 提交搜索或点击取消/返回时回调 `false`。Material 分支使用 M3 DockedSearchBar
 * （结果区在输入框下方有界展开，可安全放进滚动页面），Miuix 分支使用原生
 * SearchBar 并在展开时显示取消按钮。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeowSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    cancelText: String = "Cancel",
    onSearch: (String) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    MeowStyleContent(
        materialExpressive = {
            // 用 DockedSearchBar 而不是全屏 SearchBar:后者按最大约束测量全屏容器,
            // 放进 MeowPreferenceScreen 这类高度无限的滚动容器会直接抛
            // IllegalArgumentException;docked 形态结果区在输入框下方有界展开,
            // 与 miuix 分支的内联行为也一致。
            MaterialDockedSearchBar(
                inputField = {
                    MaterialSearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = onQueryChange,
                        onSearch = { value ->
                            onSearch(value)
                            onExpandedChange(false)
                        },
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        placeholder = { MaterialText(placeholder) },
                        leadingIcon = {
                            MaterialIcon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                            )
                        },
                    )
                },
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                // M3 SearchBar 默认按内容收窄,统一铺满可用宽度。
                modifier = modifier.fillMaxWidth(),
                content = content,
            )
        },
        miuix = {
            MiuixSearchBar(
                inputField = {
                    MiuixInputField(
                        query = query,
                        onQueryChange = onQueryChange,
                        onSearch = { value ->
                            onSearch(value)
                            onExpandedChange(false)
                        },
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        label = placeholder,
                    )
                },
                onExpandedChange = onExpandedChange,
                modifier = modifier.fillMaxWidth(),
                // miuix 默认 insideMargin 会在输入框左右各留 12dp,搜索框因此比页面
                // 其它内容窄一圈。外边距交给调用侧的页面内边距决定。
                insideMargin = DpSize(0.dp, 0.dp),
                expanded = expanded,
                outsideEndAction = {
                    MiuixText(
                        text = cancelText,
                        // 间距放在起始侧：取消按钮与输入框之间需要留白,
                        // 而它本身应当与页面右边缘对齐。
                        color = MeowTheme.colors.primary,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                            ) {
                                onQueryChange("")
                                onExpandedChange(false)
                            },
                    )
                },
                content = content,
            )
        },
    )
}
