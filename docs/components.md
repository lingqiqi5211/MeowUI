# 组件手册

MeowUI 的公共组件只暴露一套业务 API。`MeowTheme` 根据 `MeowUiStyle.MaterialExpressive` 或 `MeowUiStyle.Miuix` 选择对应实现，调用侧不应编写两份页面。

## 组件总览

| 类别 | API |
| --- | --- |
| 页面 | `MeowPreferencePage`、`MeowAppearancePage`、`MeowPreferenceScreen`、`MeowPreferenceSection`、`MeowScaffold` |
| 设置项 | `MeowSwitchPreference`、`MeowCheckboxPreference`、`MeowSliderPreference`、`MeowPopupPreference`、`MeowTextInputPreference`、`MeowActionPreference`、`MeowButton` |
| Dialog | `MeowAlertDialog`、`MeowSingleChoiceDialog`、`MeowTextInputDialog`、`MeowLoadingDialog` |
| 顶栏 | `MeowTopBar`、`MeowTopBarAction`、`MeowMenuItem` |
| 导航 | `MeowTabRow`、`MeowNavigationBar`、`MeowNavigationItem` |
| 搜索 | `MeowSearchBar` |
| 页面栈 | `MeowNavHost`（调用侧持有返回栈 `List`；宿主负责推入/弹出转场、预测式返回拖拽与页面层级） |
| 容器 | `MeowBottomSheet`、`MeowAdaptiveLayout`、`MeowCard`（KernelSU 首页式状态卡/信息卡；`containerColor` 给状态色调，`index`/`count` 让相邻卡片在 Material 下拼成分组卡片） |
| 提示与刷新 | `MeowTip`、`MeowPullToRefresh`、`rememberMeowSnackbarState` |
| 取色 | `MeowColorPicker`、`MeowColorPickerDialog`、`MeowColorPickerDefaults`、`MeowColorPalette`、`MeowColorPaletteDialog` |
| 效果 | `MeowScaffoldEffect` |

## 设置分组

`MeowPreferenceSection` 会在首次显示前收集本组最终可见的全部项目，取得完整项目数后再统一绘制。Material 3 Expressive 因此能从第一帧就按最终位置计算单项、首项、中间项和末项圆角，不会先以错误数量或错误方向显示，再二次修正。

动态条件项建议使用稳定 key。绑定 `PreferenceKey` 的设置项会使用对应 key 保持身份稳定；自定义内容通过 `item(key = ...)` 显式提供稳定 key。

## 触觉反馈

两种风格下同一交互给同一种震感，取值照 Miuix 原生组件：开关、复选、单选切换；弹出菜单展开与选中一项；滑条到端与换档；下拉刷新到位；列表甩到尽头。Miuix 分支由原生组件触发，Material 分支由库补齐，调用侧不需要自己调 `LocalHapticFeedback`。

```kotlin
MeowPreferenceSection(title = "功能") {
    MeowSwitchPreference(
        title = "启用功能",
        key = SettingsKeys.Enabled,
    )

    if (showAdvanced) {
        MeowSliderPreference(
            title = "强度",
            key = SettingsKeys.Intensity,
        )
    }
}
```

自定义内容必须通过 `item` 加入分组，并提供稳定 key：

```kotlin
MeowPreferenceSection(title = "操作") {
    item(key = "reset-button") {
        MeowButton(
            text = "恢复默认设置",
            onClick = onReset,
        )
    }
}
```

## 设置项

### Switch

布尔设置的主要开关。整行可以点击，组件负责语义、触觉反馈和一次写入。

```kotlin
MeowSwitchPreference(
    title = "启用功能",
    summary = "重启目标应用后生效",
    key = SettingsKeys.Enabled,
)
```

不绑定存储时使用 `checked` 与 `onCheckedChange`。

### Checkbox

适合次级、多项并列或不会作为页面主开关的布尔选项。

```kotlin
MeowCheckboxPreference(
    title = "显示详细日志",
    key = SettingsKeys.ShowDetails,
)
```

### Slider

当前 key 必须是 `PreferenceKey<Float>`。可设置范围、步数和显示文本。拖到两端时有一次触觉反馈；`steps > 0` 时每换一档轻响一下，两种风格相同。

绑定 key 的滑条默认在轨道上标出 key 的默认值，拖到附近会吸上去；`showDefaultValue = false` 关掉。不绑定 key 时用 `defaultValue` 传入。分档滑条默认只吸附、不画刻度点，`showSteps = true` 画出来。

`onClick` 让整行（标题、副文本、数值）可点，滑条本身仍归拖动，两种风格都不追加行尾箭头；用来打开精确输入之类的对话框。

```kotlin
MeowSliderPreference(
    title = "动画强度",
    summary = "调整界面动效幅度",
    key = SettingsKeys.Intensity,
    valueRange = 0f..1f,
    steps = 9,
    valueText = { "${(it * 100).toInt()}%" },
    onClick = { showIntensityDialog = true },
)
```

### Popup / Dropdown / Spinner

普通单选设置默认使用 `MeowPopupPreference`，表达 Dropdown/Spinner 的即时选择语义。Material 分支使用 Expressive 分组菜单并标记当前选项；Miuix 分支使用原生 `WindowSpinnerPreference`，由组件负责右侧当前值、箭头、弹窗位置与选中样式。两套样式都只在选中时提交一次新值。行尾的当前值最多占六成宽度，长文本在其中换行而不是截成一行。

```kotlin
MeowPopupPreference(
    title = "运行模式",
    key = SettingsKeys.Mode,
    options = listOf("balanced", "performance", "battery"),
    optionLabel = { mode ->
        when (mode) {
            "performance" -> "性能"
            "battery" -> "省电"
            else -> "均衡"
        }
    },
)
```

选项可以更丰富：`optionLeading`（选项图标）、`optionSummary`（副文本）、`optionEnabled`（单项禁用）；`groups` 非空时每个子列表为一个视觉分组（组间分隔，`options` 被忽略）；`collapseOnSelection = false` 时选中不收起弹窗，适合连续调整或对比选项。

```kotlin
MeowPopupPreference(
    title = "分组选择器",
    value = current,
    options = emptyList(),
    groups = listOf(listOf("快速", "均衡"), listOf("慢速", "手动", "关闭")),
    onValueChange = onPick,
    collapseOnSelection = false,
    optionEnabled = { it != "手动" },
)
```

只有需要先暂存选择、再由确认按钮提交，或选项说明较多、内容较复杂时，才使用 `MeowSingleChoiceDialog`。普通设置选项不要改成确认式 Dialog。

### Text input

点击设置行后编辑文本，确认时才提交。支持空值限制、行数和校验函数。

```kotlin
MeowTextInputPreference(
    title = "昵称",
    key = SettingsKeys.Nickname,
    placeholder = "输入昵称",
    allowBlank = false,
    validator = { value ->
        if (value.trim().length < 2) "至少输入两个字符" else null
    },
)
```

### Action

用于打开页面、Dialog、链接或执行一次性动作。`value` 可显示当前状态，也可以提供自定义 `trailing`。

```kotlin
MeowActionPreference(
    title = "检查更新",
    summary = "查看当前版本状态",
    value = "0.1.0",
    onClick = onCheckUpdate,
)
```

动作是打开另一个页面时置 `navigation = true`：Material 分支在行尾（`value` 之后）追加 > 箭头；Miuix 分支的行本来就带原生右箭头，不受该参数影响。

### 带图标的列表项

Switch、Checkbox、Action 都有可为空的 `leading` 槽位，用来做“左侧图标 + 常规列表项”的行。两种常见形态（sample 的 Controls 页有完整示例）：

```kotlin
// 应用列表（HyperCeiler 首页式）：大图标 + 应用名/包名
MeowActionPreference(
    title = "Meow Music",
    summary = "com.meow.music",
    leading = { AppIcon(packageInfo) }, // 约 40dp，通常是真实应用图标
    onClick = { openAppConfig() },
)

// 选项列表（KernelSU 设置式）：小图标 + 标题/摘要
MeowActionPreference(
    title = "Theme palette",
    summary = "Seed color and palette style",
    leading = {
        Image(
            imageVector = Icons.Rounded.Palette,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(MeowTheme.colors.onSurfaceVariant),
        )
    },
    onClick = onOpenPalette,
)
```

`leading` 为 null 时布局自动收紧，同一分组内可以混排有图标和无图标的行。

### Button

`MeowButton` 是独立按钮，不代表设置值，**不要放进设置分组**——按钮不是列表项，放在分组卡片内会破坏分组语义与圆角结构。把它放在分组之外（如页面底部）：

```kotlin
MeowPreferenceSection(title = "Controls") { /* 设置项 */ }

MeowButton(
    text = "立即应用",
    onClick = onApply,
    modifier = Modifier.fillMaxWidth(),
)
```

## Dialog

所有 Dialog 都是受控组件：`show` 由调用侧持有，`onDismissRequest` 负责关闭。

### Alert

```kotlin
MeowAlertDialog(
    show = showResetDialog,
    title = "恢复默认设置",
    message = "此操作会清除当前配置。",
    style = MeowAlertStyle.Warning,
    confirmText = "恢复",
    cancelText = "取消",
    onConfirm = {
        resetSettings()
        showResetDialog = false
    },
    onDismissRequest = { showResetDialog = false },
)
```

`MeowAlertStyle.Standard` 用于普通信息或确认；`Warning` 用于需要警示的操作。

### Single choice

```kotlin
MeowSingleChoiceDialog(
    show = showModeDialog,
    title = "选择高级模式",
    selected = mode,
    options = modes,
    onSelected = {
        mode = it
        showModeDialog = false
    },
    onDismissRequest = { showModeDialog = false },
)
```

Dialog 内部保存临时选择，只有点击确认后才调用 `onSelected`；取消不会修改正式值。

### Text input

`MeowTextInputDialog` 适合需要明确确认的文本输入。它与设置项版本一样支持 `allowBlank`、`validator`、单行和多行设置。

```kotlin
MeowTextInputDialog(
    show = showNameDialog,
    title = "修改昵称",
    initialValue = nickname,
    allowBlank = false,
    onConfirm = {
        nickname = it
        showNameDialog = false
    },
    onDismissRequest = { showNameDialog = false },
)
```

### Loading

`MeowLoadingDialog` 用于短时间、必须阻止其他操作的任务。传入 `onDismissRequest = null` 时不可由用户关闭；普通后台加载优先使用页面内进度或 Tip，避免长期遮挡页面。

```kotlin
MeowLoadingDialog(
    show = saving,
    title = "正在保存",
    message = "请稍候",
)
```

## 顶栏图标与菜单 Popup

顶栏优先使用图标操作。多个次要操作放入 `MeowTopBarAction.Menu`，不要把 Bottom Sheet 按钮当作固定顶栏入口。

```kotlin
MeowPreferencePage(
    title = "模块设置",
    actionItems = listOf(
        MeowTopBarAction.Icon(
            icon = Icons.Rounded.Refresh,
            contentDescription = "刷新",
            onClick = onRefresh,
        ),
        MeowTopBarAction.Menu(
            icon = Icons.Rounded.MoreVert,
            contentDescription = "更多",
            items = listOf(
                MeowMenuItem("导入设置", onClick = onImport),
                MeowMenuItem("导出设置", onClick = onExport),
            ),
        ),
    ),
) {
    // 页面内容
}
```

Material 分支使用 Material Popup，Miuix 分支使用 Miuix Popup。所有图标操作都必须提供可读的 `contentDescription`。

`MeowMenuItem.children` 非空时该项成为子菜单入口（点击展开下一级，`onClick` 被忽略）：Miuix 用原生级联弹窗向外堆叠展开，Material 在同一弹窗内下钻并在顶部提供返回上级的行（下钻期间弹窗宽度锁定为主菜单宽度，不会跳位；Material 端只支持一层下钻）。

菜单还支持分组与选中态，对应 miuix 的 `DropdownEntry`/`DropdownItem` 模型：

- `Menu.groups`：每个子列表一个视觉分组（组间有分隔），非空时 `items` 被忽略；
- `MeowMenuItem.selected`：非 null 即可选中项，true 时行尾显示选中标记（两端都固定贴行右缘，槽位恒定，切换选中不会让弹窗重排）；`summary` 为副文本行，常用于父行显示当前值；
- `Menu.collapseOnSelection = false`：点选不收起弹窗，用于单选/多选式菜单；子菜单场景通常也应设为 false——选一下就把整栈收掉会丢掉用户导航进来的层级。

```kotlin
MeowTopBarAction.Menu(
    icon = Icons.Rounded.Tune,
    contentDescription = "显示选项",
    collapseOnSelection = false,
    groups = listOf(
        listOf(
            MeowMenuItem(
                text = "筛选",
                summary = currentFilterLabel,
                children = filters.map { filter ->
                    MeowMenuItem(
                        text = filter.label,
                        selected = filter == current,
                        onClick = { onFilter(filter) },
                    )
                },
            ),
        ),
        listOf(
            MeowMenuItem(
                text = "已启用优先",
                selected = redirectedFirst,
                onClick = { onRedirectedFirst(!redirectedFirst) },
            ),
        ),
    ),
)
```

## 搜索框

```kotlin
var query by rememberSaveable { mutableStateOf("") }
var expanded by rememberSaveable { mutableStateOf(false) }

MeowSearchBar(
    query = query,
    onQueryChange = { query = it },
    expanded = expanded,
    onExpandedChange = { expanded = it },
    placeholder = "搜索设置",
    cancelText = "取消",
) {
    // 展开后的搜索结果；空查询建议什么都不画（全量列表不是搜索结果）。
    // content 自带容器：列表项用 MeowPreferenceSection / MeowCard 就是
    // 库里一致的一块一块形态，组件只负责边距与系统栏/键盘让位。
    if (query.isNotBlank()) {
        MeowPreferenceSection {
            results.forEach { item ->
                MeowActionPreference(title = item.title, onClick = { onPick(item) })
            }
        }
    }
}
```

动画照 KernelSU：折叠时是页面里的一条药丸输入条，点按后输入条从原位飞到状态栏下方，底面淡入盖住整页（含顶栏与底栏），结果随后铺满整屏；取消/系统返回时输入条飞回原位。**必须用在 `MeowScaffold` 内**——展开态画在 scaffold 的浮层插槽里，inset 与页面同一套；同一 scaffold 下多个搜索框（常驻 pager 的多页面）互不干扰，折叠中的实例不会打掉别人正展开的搜索面。

交互约定（两端相同）：

- 折叠时整条输入条可点，点按回调 `onExpandedChange(true)`，随后组件自动抢焦点、弹键盘；
- 展开时的「取消」：Material 是输入条内前导位淡出换成的返回按钮，Miuix 是从右侧滑入的 `cancelText` 文字按钮；两者与系统返回键等效——清空查询词并回调 `onExpandedChange(false)`；
- 查询词非空且已展开时，行内出现清空按钮（缩放淡入），只清词不收起，焦点保留；
- 键盘上的搜索键回调 `onSearch(query)` 后**只收键盘、不收起**（结果就在下方，收起反而看不到）。需要提交即收起，在 `onSearch` 里自己置 `expanded = false`；
- 调用侧程序化把 `expanded` 置回 `false`（例如点中某条结果）不会清词，收起后输入条里仍留着这次的查询词——与组件自己的「取消」是两条不同的路径。

两端差异只在视觉：Material 为 `surfaceContainerHigh` 药丸容器、56dp 高、前导槽位在放大镜与返回按钮间淡入淡出；Miuix 为 45dp 圆角输入条、miuix 原生放大镜与清空图标，取消文字按钮从右侧挤进来。

## 页面栈

```kotlin
var backStack by rememberSaveable { mutableStateOf(listOf(Route.Home)) }

MeowNavHost(
    backStack = backStack,
    onBack = { backStack = backStack.dropLast(1) },
    predictiveBackEnabled = appearance.predictiveBackEnabled,
) { route ->
    when (route) {
        Route.Home -> HomePage(onOpenDetail = { backStack = backStack + Route.Detail })
        Route.Detail -> DetailPage(onBack = { backStack = backStack.dropLast(1) })
    }
}
```

返回栈由调用侧持有——一个普通 `List`，入栈/出栈就是换一个列表传进来。宿主负责经典 activity 式推入/弹出转场（新页全宽滑入、旧页约 1/4 视差并轻微压暗）、预测式返回手势直接拖拽弹出转场进度（`predictiveBackEnabled = false` 时退化为普通返回键），以及由栈深度决定的方向与层级。`onBack` 为 null 时完全不注册返回处理，只做转场。被盖住页面的 `rememberSaveable` 状态（滚动位置、pager 页等）在弹回时原样恢复；页面对象需要稳定且互不相同的 `toString`（枚举、data object/data class 天然满足）。

## Scaffold 与滚动顶栏

### Preference page

`MeowPreferencePage` 是设置页的推荐入口，已经组合 `MeowScaffold`、滚动顶栏和 `MeowPreferenceScreen`。

```kotlin
MeowPreferencePage(
    title = "模块设置",
    subtitle = "MeowUI",
    bottomBar = {
        MeowNavigationBar(
            items = navigationItems,
            selectedIndex = selectedPage,
            onItemSelected = { selectedPage = it },
            style = MeowNavigationBarStyle.Floating,
        )
    },
) {
    // MeowPreferenceSection...
}
```

页面内容滚动时，Material 3 Expressive 与 Miuix 会分别使用各自的顶栏滚动行为。顶栏展开、过渡和收起期间，背景应始终与正文表面连续。

`MeowPreferenceScreen`、`MeowPullToRefresh` 等库内容器自动接入顶栏滚动行为，调用侧不必重复接。自建滚动容器（`LazyColumn` / `verticalScroll`）想让顶栏跟随折叠时，在其祖先上加 `Modifier.meowScaffoldScroll()`（不在 `MeowScaffold` 内时为空操作）。`MeowPullToRefresh` 还会在顶栏折叠着时把下拉增量先喂给顶栏展开，展开完毕剩余才进入下拉刷新。

### Scaffold

非设置页面可以直接使用 `MeowScaffold`。`content` 会收到系统栏、顶栏和底栏对应的 `PaddingValues`，内容必须使用这些间距。

## Tab

```kotlin
MeowTabRow(
    tabs = listOf("常规", "高级", "关于"),
    selectedIndex = selectedTab,
    onTabSelected = { selectedTab = it },
    style = MeowTabRowStyle.Standard,
)
```

- Material 3 Expressive：使用 connected button group 结构。
- Miuix：`Standard` 使用原生 `TabRow`，`Contour` 使用原生 `TabRowWithContour`。
- `selectedIndex` 必须位于 `tabs.indices`，页面状态由调用侧持有。

## 普通与悬浮底栏

```kotlin
val navigationItems = listOf(
    MeowNavigationItem("设置", Icons.Rounded.Settings),
    MeowNavigationItem("日志", Icons.Rounded.List, badge = "3"),
)

MeowNavigationBar(
    items = navigationItems,
    selectedIndex = selectedPage,
    onItemSelected = { selectedPage = it },
    style = MeowNavigationBarStyle.Floating,
)
```

- `MeowNavigationBarStyle.Standard`：Material 使用 Expressive `ShortNavigationBar`，Miuix 使用原生普通底栏。
- `MeowNavigationBarStyle.Floating`：Material 使用 64 dp 胶囊底栏，选中指示器随选中项平滑滑动，并支持直接拖动指示器切页；Miuix 使用原生悬浮底栏容器并补充名称显示。两者都保留图标、单行文字、选中状态与安全区间距。
- `showFloatingLabels = false` 可隐藏悬浮底栏图标下方的名称，仅悬浮样式受影响。
- sample 的“Floating bottom bar”开关可直接比较普通与悬浮样式。
- `badge` 为空时不显示；`enabled = false` 时该项不可操作。
- 重复点击当前项不会再次触发切页；悬浮指示器拖动时会接管并取消旧动画，松手后只提交一次最终目标。

## Bottom Sheet

```kotlin
MeowBottomSheet(
    show = showSheet,
    title = "更多设置",
    onDismissRequest = { showSheet = false },
) {
    // Sheet 内容
}
```

`startAction` 与 `endAction` 可放置关闭、确认等操作。Bottom Sheet 适合承载与当前页面相关的补充内容，不应代替顶栏的常用图标菜单。Miuix 分支内容自带滚动、overscroll 与底部安全区间距；空标题与空动作不会占位。内容槽位的左右与底部边距由组件自身提供，调用侧不需要再加。

Miuix 分支为应用内 `OverlayBottomSheet`，经由 miuix Scaffold 的 popup host 渲染：`MeowBottomSheet` 必须放在某个 `MeowScaffold` 的组合子树内（内容槽位即可），与 Scaffold 平级（例如直接挂在导航层）时 Miuix 风格下不会显示。

## 内容卡片

```kotlin
// 状态卡：容器色由运行状态决定，内容色一并传入。
MeowCard(
    containerColor = MeowTheme.colors.error,
    contentColor = MeowTheme.colors.onError,
) { /* 卡片内容 */ }

// 分段拼卡：相邻卡片在 Material 下拼成一张分组卡片，Miuix 下保持独立卡片。
items.forEachIndexed { index, item ->
    MeowCard(index = index, count = items.size) { /* 条目内容 */ }
}
```

KernelSU 首页式状态卡/信息卡。默认是当前风格的普通卡面；`containerColor`/`contentColor` 给状态色调（内容色会作为 `LocalContentColor` 提供）；`onClick` 让整卡可点。`index`/`count` 适合条目数由数据决定、放进 `LazyColumn` 的列表卡——走不了 `MeowPreferenceSection` 编译期收集 DSL 的场景；组内间距由组件自动补齐。

## Snackbar

```kotlin
val snackbarState = rememberMeowSnackbarState()

MeowScaffold(
    title = "模块设置",
    snackbarState = snackbarState,
) { /* 页面内容 */ }

scope.launch {
    val result = snackbarState.show("已保存", actionLabel = "撤销")
    if (result == MeowSnackbarResult.ActionPerformed) {
        undo()
    }
}
```

`show` 挂起到 snackbar 消失并返回 `MeowSnackbarResult`；Material 与 Miuix 分别使用各自的原生 Snackbar 宿主与滑动关闭手势。`snackbarState` 也可传给 `MeowPreferencePage`。

## Tip

```kotlin
MeowTip(
    title = "设置未保存",
    message = "libxposed service 当前不可用。",
    style = MeowTipStyle.Warning,
    actionText = "重试",
    onAction = onRetry,
)
```

支持 `Info`、`Success`、`Warning`、`Error`。只有提供 `onAction` 时才应设置 `actionText`。

## 取色窗口

```kotlin
MeowColorPickerDialog(
    show = showColorPicker,
    dynamicColor = dynamicColor,
    seedColor = seedColor,
    onDynamicColorChange = { dynamicColor = it },
    onSeedColorChange = { seedColor = it },
    onDismissRequest = { showColorPicker = false },
)
```

统一的取色窗口：第一个色块表示跟随壁纸（系统动态取色，Android 12+ 显示），其余为自定义种子色，可用 `presetColors` 替换；每个色块以该种子展开后的配色绘制双色预览，行末的彩虹色块打开调色盘自选任意颜色。选择即时通过回调生效，适合与 `MeowTheme` 的 `dynamicColor` / `seedColor` 直接绑定。

## 调色盘

```kotlin
// 内嵌控件：放在任意页面/自定义对话框里
MeowColorPalette(
    color = color,
    onColorChanged = { color = it },
    mode = MeowColorPaletteMode.Sliders, // 或 Grid（HSV 网格）
    colorSpace = MeowColorPaletteSpace.Hsv, // Sliders 模式可选 Hsv/OkHsv/OkLab/OkLch
)

// 弹窗形态：确认后一次性提交
MeowColorPaletteDialog(
    show = showPalette,
    initialColor = color,
    onConfirm = {
        color = it
        showPalette = false
    },
    onDismissRequest = { showPalette = false },
)
```

自选任意颜色的调色盘控件，两种风格共用同一实现（底层为 miuix 的取色组件，纯 Canvas 绘制、无风格专属视觉），公共 API 不暴露底层类型。`Sliders` 为滑条式（支持 4 种色彩空间），`Grid` 为网格式取色盘。弹窗按风格使用原生容器；`keepAlpha` 默认 false，确认时透明度强制为 1，适合直接作种子色。主题色色票行末尾的调色盘入口就是基于它实现的。

## 外观设置页

`MeowAppearance` 集中保存 UI 风格、深浅模式、动态取色、种子色、色彩风格、色彩标准、预测性返回偏好与界面缩放。把同一份状态交给主题和外观页即可：

```kotlin
MeowTheme(appearance = appearance) {
    MeowAppearancePage(
        appearance = appearance,
        onAppearanceChange = onAppearanceChange,
        onBackClick = onBack,
    )
}
```

- **头图**：默认显示内置的 `MeowAppearancePreview` —— 一块随主题实时着色的迷你界面，按设备形态自动切换（手机为竖屏单栏、展开态折叠屏为铰链双栏、平板为横屏侧栏布局）。`showPreview = false` 可去掉头图；`previewContent` 不为 null 时用自定义内容替换，同样不需要区分 Material 与 Miuix。
- `extraContent` 可在标准选项之后加入模块自己的外观设置。
- 界面缩放范围为 80%–110%，松开 Slider 后提交；系统字体缩放比例保持不变。
- 不支持 2025 色彩标准的色彩风格只显示并使用 2021，避免无效组合。
- `amoledDarkEnabled` 为 AMOLED 纯黑深色开关（背景与 surface 容器压成纯黑，保留 surfaceBright 卡片层次），叠加在深色模式上——深色生效时（含跟随系统进入深色）即应用；仅 Material 3 Expressive 分支生效并显示该开关，Miuix 分支忽略。
- `blurEnabled`（默认开）控制顶栏与悬浮底栏的内置背景磨砂：在 `MeowScaffold` 内且设备支持 RuntimeShader 时，栏体对身后内容做模糊并叠半透明底色；关闭或设备不支持时自动回退不透明底色。该开关经 `MeowTheme(appearance = …)` 统一入口生效，外观页不显示对应选项，由应用自行决定是否暴露（sample 在界面分组给了一个开关）。
- 色票行末尾附带调色盘（miuix ColorPicker），可自选任意种子色；选中态显示当前自选颜色。调色盘弹窗的标题/确认/取消文案经 `MeowAppearanceLabels.customColor/dialogConfirm/dialogCancel`（或 `MeowColorPicker` 的同名参数）本地化。
- Miuix 风格下可通过 Monet 开关关闭取色，改用 Miuix 原生配色；关闭后取色卡与调色板、色彩标准选项一并隐藏。Material 分支忽略该开关。
- 预测性返回只保存使用者偏好，导航层需要自行读取 `predictiveBackEnabled` 决定返回行为。使用 `MeowNavHost` 时直接把该值传给同名参数即可（sample 即如此）：开启时手势拖拽弹出转场进度，关闭时退化为普通返回键出栈；Android 14 以下不会显示该选项。自带导航层的应用可参考 InstallerX-Revived 的做法，用 `NavigationBackHandler` 在关闭时拦截系统预测手势。

### 自定义外观页

宿主自带顶栏与滚动容器（例如页面本身就是 `MeowNavHost` 里的一屏）时，用 `MeowAppearanceContent` 只嵌入选项正文，参数与 `MeowAppearancePage` 一致，去掉了页面壳。

`MeowAppearancePage` 只是把公开组件按固定顺序拼装。需要不同的布局、增删设置项或改文案结构时，直接用同一批积木搭自己的页面，状态仍然是一份 `MeowAppearance`：

```kotlin
@Composable
fun MyAppearancePage(
    appearance: MeowAppearance,
    onChange: (MeowAppearance) -> Unit,
    onBack: () -> Unit,
) {
    MeowPreferencePage(title = "外观", onBackClick = onBack) {
        // 1. 头图：内置预览或任何自定义 Composable
        MeowAppearancePreview()

        // 2. 主题色：横向色票选择器（也可以改用 MeowColorPickerDialog 弹窗取色）
        MeowColorPicker(
            dynamicColor = appearance.dynamicColor,
            seedColor = appearance.seedColor,
            paletteStyle = appearance.paletteStyle,
            colorSpec = appearance.colorSpec,
            onDynamicColorChange = { onChange(appearance.copy(dynamicColor = it)) },
            onSeedColorChange = {
                onChange(appearance.copy(dynamicColor = false, seedColor = it))
            },
        )

        // 3. 深浅模式：三段 Tab
        val modes = MeowThemeMode.entries
        MeowTabRow(
            tabs = listOf("跟随系统", "浅色", "深色"),
            selectedIndex = modes.indexOf(appearance.themeMode),
            onTabSelected = { onChange(appearance.copy(themeMode = modes[it])) },
            style = MeowTabRowStyle.Contour,
        )

        // 4. 其余选项照常用偏好分组组装
        MeowPreferenceSection(title = "界面") {
            MeowPopupPreference(
                title = "调色板风格",
                value = appearance.paletteStyle,
                options = MeowPaletteStyle.entries,
                onValueChange = { onChange(appearance.copy(paletteStyle = it)) },
            )
            MeowSwitchPreference(
                title = "预测式返回",
                checked = appearance.predictiveBackEnabled,
                onCheckedChange = { onChange(appearance.copy(predictiveBackEnabled = it)) },
            )
        }
    }
}
```

要点：

- 把同一份 `MeowAppearance` 同时交给 `MeowTheme(appearance = …)` 和自定义页面，每次变更通过回调回传并持久化（sample 用 `PreferenceKey` 逐字段存取，见 `sample/MainActivity.kt` 的 `updateAppearance`）。
- 所有积木都是风格自适应的，页面代码不需要出现任何 Material 或 Miuix 类型。
- Miuix 关闭 Monet 时种子色与调色板不生效，自定义页面建议参照默认页用 `appearance.style != MeowUiStyle.Miuix || appearance.miuixMonetEnabled` 控制相关选项的显隐。
- 旧的 `MeowTheme(style = …)` 与 `MeowColorPickerDialog` 用法继续保留。

## 下拉刷新

```kotlin
MeowPullToRefresh(
    isRefreshing = refreshing,
    onRefresh = onRefresh,
) {
    RefreshableContent()
}
```

`isRefreshing` 是调用侧持有的正式状态。内容本身应可滚动；Miuix 分支会与当前顶栏滚动行为协作，调用侧不需要另建一套刷新状态机。

## Blur

顶栏与悬浮底栏在 `MeowScaffold` 内默认自带背景磨砂，由 `MeowAppearance.blurEnabled` 统一控制，无需任何额外接线；设备不支持 RuntimeShader（API < 33）时自动回退不透明底色，尺寸、间距和点击区域不变。悬浮底栏只在胶囊轮廓内取样和着色，不会出现底部矩形遮罩。

注意底层依赖声明 minSdk 33：**minSdk 低于 33 的应用（无论是否开启 Blur）都要在主 Manifest 加 `<uses-sdk tools:overrideLibrary="top.yukonga.miuix.kmp.blur" />`**。

## 库内渲染控件的 modifier

MeowUI 自己渲染的控件，调用侧通过对应的 modifier 参数触达——用于打 testTag、约束尺寸，而不必把控件搬回业务层重写。

| 组件 | 参数 | 作用于 |
| --- | --- | --- |
| `MeowTopBarAction.Icon` / `.Text` / `.Menu` | `modifier` | 该操作渲染出的按钮本体（点击与语义同一节点） |
| `MeowNavigationItem` | `modifier` | 承载点击与选中状态的导航项。Material 悬浮底栏在胶囊指示器内还会绘制一份装饰性副本，该副本不套用此 modifier，避免语义重复 |
| `MeowTopBar` / `MeowScaffold` / `MeowPreferencePage` / `MeowAppearancePage` | `navigationModifier` | 由 `onBackClick` 生成的内置返回按钮；传了自定义 `navigationIcon` 时不生效（自定义内容自带 modifier） |
| `MeowAlertDialog` / `MeowSingleChoiceDialog` / `MeowTextInputDialog` | `confirmModifier`、`cancelModifier` | 对话框自己构建的确认/取消按钮 |
| `MeowTextInputDialog` | `fieldModifier` | 对话框内的输入框 |
| `MeowTip` | `actionModifier` | Tip 右侧的操作按钮 |

`MeowAlertDialog` 另外提供两个行为参数：

- `confirmEnabled`：草稿非法时禁用确认按钮，和 `MeowTextInputDialog` 的校验行为一致。
- `onCancel`：取消按钮默认上报 `onDismissRequest`（两者语义相同时正确）。需要区分时传 `onCancel`——例如「未保存更改」提示里按钮表示放弃，而返回键或点击外部表示留在当前页。

顶栏菜单项 `MeowMenuItem` 没有 `modifier`：Miuix 原生 dropdown 不接受逐项 modifier，加一个在 Miuix 分支被静默丢弃的参数会破坏「同一组件在两种风格下行为一致」的约定。需要逐项定位时改用 `MeowPopupPreference`。

## 自适应布局

`MeowAdaptiveLayout` 在宽屏显示列表与详情双栏，在窄屏根据 `MeowCompactPane` 显示其中一栏。设置状态仍由调用侧持有，不会因布局切换而复制。
