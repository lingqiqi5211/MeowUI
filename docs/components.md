# 组件手册

MeowUI 的公共组件只暴露一套业务 API。`MeowTheme` 根据 `MeowUiStyle.MaterialExpressive` 或 `MeowUiStyle.Miuix` 选择对应实现，调用侧不应编写两份页面。

## 组件总览

| 类别 | API |
| --- | --- |
| 页面 | `MeowPreferencePage`、`MeowPreferenceScreen`、`MeowPreferenceSection`、`MeowScaffold` |
| 设置项 | `MeowSwitchPreference`、`MeowCheckboxPreference`、`MeowSliderPreference`、`MeowPopupPreference`、`MeowTextInputPreference`、`MeowActionPreference`、`MeowButton` |
| Dialog | `MeowAlertDialog`、`MeowSingleChoiceDialog`、`MeowTextInputDialog`、`MeowLoadingDialog` |
| 顶栏 | `MeowTopBar`、`MeowTopBarAction`、`MeowMenuItem` |
| 导航 | `MeowTabRow`、`MeowNavigationBar`、`MeowNavigationItem` |
| 容器 | `MeowBottomSheet`、`MeowAdaptiveLayout` |
| 提示与刷新 | `MeowTip`、`MeowPullToRefresh`、`rememberMeowSnackbarState` |
| 取色 | `MeowColorPickerDialog`、`MeowColorPickerDefaults` |
| 效果 | `MeowScaffoldEffect`、`rememberMeowBlurScaffoldEffect` |

## 设置分组

`MeowPreferenceSection` 会在首次显示前收集本组最终可见的全部项目，取得完整项目数后再统一绘制。Material 3 Expressive 因此能从第一帧就按最终位置计算单项、首项、中间项和末项圆角，不会先以错误数量或错误方向显示，再二次修正。

动态条件项建议使用稳定 key。绑定 `PreferenceKey` 的设置项会使用对应 key 保持身份稳定；自定义内容通过 `item(key = ...)` 显式提供稳定 key。

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

当前 key 必须是 `PreferenceKey<Float>`。可设置范围、步数和显示文本。

```kotlin
MeowSliderPreference(
    title = "动画强度",
    summary = "调整界面动效幅度",
    key = SettingsKeys.Intensity,
    valueRange = 0f..1f,
    steps = 9,
    valueText = { "${(it * 100).toInt()}%" },
)
```

### Popup / Dropdown / Spinner

普通单选设置默认使用 `MeowPopupPreference`，表达 Dropdown/Spinner 的即时选择语义。Material 分支使用 Expressive 分组菜单并标记当前选项；Miuix 分支使用原生 `WindowSpinnerPreference`，由组件负责右侧当前值、箭头、弹窗位置与选中样式。两套样式都只在选中时提交一次新值。

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

### Button

`MeowButton` 是独立按钮，不代表设置值。放入设置分组时使用 `item` 包装；页面底部操作也可以直接使用。

```kotlin
MeowButton(
    text = "立即应用",
    onClick = onApply,
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

`startAction` 与 `endAction` 可放置关闭、确认等操作。Bottom Sheet 适合承载与当前页面相关的补充内容，不应代替顶栏的常用图标菜单。Miuix 分支内容自带滚动、overscroll 与底部安全区间距；空标题与空动作不会占位。

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

统一的取色窗口：第一个色块表示跟随壁纸（系统动态取色，Android 12+ 显示），其余为自定义种子色，可用 `presetColors` 替换；每个色块以该种子展开后的配色绘制双色预览。选择即时通过回调生效，适合与 `MeowTheme` 的 `dynamicColor` / `seedColor` 直接绑定。

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

Blur 位于可选模块 `meowui-blur`。设置页最简单的接法：

```kotlin
val effect = rememberMeowBlurScaffoldEffect(enabled = blurEnabled)

MeowPreferencePage(
    title = "模块设置",
    effect = effect,
) {
    // 页面内容
}
```

支持时，正文提供 Blur 来源，顶栏与普通底栏使用连续的矩形表面；悬浮底栏只在胶囊轮廓内取样和着色，不会出现底部矩形遮罩。Material 悬浮底栏默认使用较轻的半透明 `surfaceContainer` 覆层。不支持、关闭或预览环境不可用时，会回退为同样轮廓的普通表面，尺寸、间距和点击区域不变。

需要自定义前景时可以组合：

- `rememberMeowBlurBackdrop`
- `Modifier.meowBlurSource`
- `Modifier.meowBlurSurface`
- `isMeowBlurSupported`

## 自适应布局

`MeowAdaptiveLayout` 在宽屏显示列表与详情双栏，在窄屏根据 `MeowCompactPane` 显示其中一栏。设置状态仍由调用侧持有，不会因布局切换而复制。
