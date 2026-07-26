# 双 UI 设计规范

## 目标与优先级

MeowUI 统一组件语义、状态、`PreferenceKey` 绑定和业务回调，不统一 Material 3 Expressive 与 Miuix 的外观。

- 调用侧只编写一份页面和设置逻辑。
- 风格选择在组件内部完成，同一组件在两种风格下保持一致的提交时机、启用状态、取消行为与错误结果。
- 两套实现分别使用各自的组件、颜色、形状、排版、动效和反馈。
- 不把 Miuix 做成换色 Material，也不把 Material 做成仿 MIUI。

实现时按以下顺序判断：

1. 当前设计体系的官方组件与官方 example。
2. MeowUI 已有公共组件、主题 token 与交互语义。
3. 参考项目中经过验证的页面结构、间距和交互方式。
4. 自定义实现。

参考项目不是公共 API 的来源。不得复制应用业务代码，也不得把某个应用的私有约定直接变成 MeowUI 默认行为。

## 共同页面规则

### 页面骨架与滚动

- 设置页优先使用 `MeowPreferencePage`；自定义页面使用 `MeowScaffold`，不要在业务层分别拼接两套顶栏和底栏。
- 页面只保留一个主要纵向滚动容器。内容必须使用 Scaffold 提供的 `PaddingValues`，不能靠固定顶部或底部留白猜测系统栏位置。
- 顶栏、正文和底栏共享同一滚动上下文。业务层不得重复添加会争抢手势的 `nestedScroll`、overscroll 或下拉刷新状态。
- 滚动手势区域应覆盖页面可用宽度。宽屏时收窄或居中的是内容，不是整个滚动节点，避免两侧出现无法滚动的区域。
- 页面末尾需要同时吸收底栏与系统导航栏安全区；横屏时还要处理侧边 cutout 和导航手势区域。
- 顶栏的展开、滚动和收起是一段连续状态。整个过程中背景、Blur 与正文表面必须连续，不能只在完全展开或完全收起时正确。

### 信息层级与间距

设置行从高到低使用以下信息层级：

1. 标题：描述设置本身。
2. 摘要：解释影响、限制或生效条件。
3. 当前值：显示选择结果，不代替摘要。
4. 控件：Switch、Checkbox、Slider、Dropdown 或操作图标。

- 标题、摘要和当前值不得使用同一视觉权重。
- 摘要与行内当前值必须保留可读字号；当前值较长时优先限制行数和省略，不使用过小字号硬塞。
- 标题/摘要区域与下方 Slider、按钮组或补充选项之间必须有独立间距，不能紧贴。
- 一组设置的内部节奏保持一致；组与组之间的间距应明显大于同组相邻项的连接间距。
- 颜色、字号、圆角、间距和动效优先来自当前设计体系或 MeowUI token，不在各组件内重复写相近常量。

### 列表、分组与性能

- `MeowPreferenceSection` 在绘制前收集本组最终可见项目，并使用稳定 key 计算最终位置。
- 普通设置分组可以作为一个视觉整体；包含大量或动态内容时，每个可见行应保持独立的 lazy item 粒度，再通过首段、中段和末段形状拼成连续卡片。
- 不把大量行塞进单个 `item { Column { ... } }`，避免整组同时组合、测量和重绘。
- 条件项、展开项和排序项使用稳定业务 key，不用当前索引代替长期身份。
- 动态增减时可以使用 placement 动画，但首次显示不得播放从错误数量、错误圆角或错误位置出发的动画。
- 圆角变化、chevron 旋转与行 placement 属于同一次状态变化时，时长和节奏应协调，避免其中一项瞬间跳变。

### 自适应布局

- 列表/详情场景使用 `MeowAdaptiveLayout`，不要在业务页面重复维护宽度判断和两套状态。
- 窄屏一次显示一个 pane；宽屏显示列表与详情时，两侧仍共享同一份选择和设置状态。
- 布局切换不得重建正式设置值、丢失当前选择或重复触发写入。
- 宽屏内容可以设置最大阅读宽度，但滚动手势、窗口 inset 与焦点移动仍覆盖完整可用区域。
- 横屏矮视口中的长内容必须允许继续增长和滚动，不能用固定一屏高度裁掉底部内容。

### 动效与反馈

- Material 使用 Expressive motion，Miuix 使用 Miuix 自身动效；不要共用一套动画参数强行得到相似外观。
- 首次 composition 直接显示最终颜色、尺寸、圆角和选中状态。动画只表达首次显示之后的真实状态变化。
- 同一交互只保留一个 indication、一个点击入口和一次触觉反馈，避免闪烁、双重遮罩或重复提交。
- 静态分组不为展示效果强加 item 动画；动态插入、删除、展开或排序才使用对应动画。
- `MeowPullToRefresh` 的刷新状态由调用侧持有；刷新指示、顶栏滚动和内容 overscroll 必须协作，不得互相抢占手势。
- 轻量结果使用 `MeowTip` 或页面内状态；只有阻止继续操作且无法后台完成时才使用 Loading Dialog。

### Dialog 与 Popup

- 普通即时单选使用 `MeowPopupPreference`；需要暂存选择并在确认后提交时使用 `MeowSingleChoiceDialog`。
- Popup 适合短选项和就地操作；Bottom Sheet 适合与当前页面相关的补充内容；Dialog 适合需要明确决策或校验的任务。
- 长 Dialog 的内容区必须有高度上限并可滚动，操作按钮固定在可见底部；短内容应自然收缩，不强行撑满屏幕。
- Dialog 关闭动画完成前，不因状态重建再次出现按压态、遮罩或选中闪烁。
- Popup 的锚点、宽度、当前值和选中标记必须可读；不能用固定小宽度或过小字号掩盖布局问题。
- 顶栏主要操作使用图标；多个次要操作放入 `MeowTopBarAction.Menu`。Bottom Sheet 不是固定顶栏菜单的替代品。

### Blur 与表面层级

- Blur 是可选增强，不是信息层级或可读性的基础。
- 正文提供 backdrop 来源，顶栏、底栏和其他前景表面消费同一来源，避免每个业务页面各自拼接不同参数。
- 只有 backdrop 可用且 Blur 已启用时，前景栏才使用透明容器；其他情况使用当前设计体系的正确表面色。
- backdrop 为 `null`、系统不支持、用户关闭或预览环境不可用都是正常路径。
- Blur 回退不得改变栏高度、content padding、导航位置、点击区域或文字对比度。
- Blur 层不能遮挡系统栏 inset、Popup、Dialog 或无障碍焦点边界。

### 可访问性

- 图标按钮提供可理解的 `contentDescription`；装饰图标使用 `null`，避免重复朗读。
- Switch、Checkbox、Tab、单选项和导航项提供正确角色、启用状态与选中状态。
- 整行可点击时，由整行承担唯一点击语义，内部控件不再暴露第二个等价点击目标。
- 文字缩放后仍能读取，不能依赖固定高度或固定小字号维持布局。
- Blur、颜色和动画不能成为理解状态的唯一方式；错误、选中和禁用状态需要文字或语义补充。

## Material 3 Expressive

### 主题、形状与动效

- 使用 `MaterialExpressiveTheme`、Expressive `MotionScheme` 与 Material 3 Expressive 组件族。
- 不用普通 MD3 控件加大圆角冒充 Expressive，也不借用 Miuix squircle 改造 Material 组件。
- 自定义 Material 表面从 `MaterialTheme` 或 MeowUI Material token 取得颜色和形状，不在组件中散落 seed color 派生值。
- 首次显示不播放 shape morph；真实状态变化后才允许使用 Expressive shape 与 motion 过渡。

### Segmented list

`MeowPreferenceSection` 必须在首次显示前取得最终可见项目列表与项目数，再根据最终位置计算形状：

1. 单项保留完整外侧圆角。
2. 首项保留顶部外侧圆角，底部使用连接圆角。
3. 中间项使用连接圆角。
4. 末项顶部使用连接圆角，底部保留外侧圆角。
5. 第一项上方不增加 segmented gap，后续项目使用统一连接间距。
6. 项目增减后的形状变化从上一次真实布局开始，不从 `count = 0`、`count = 1` 或错误方向开始。

Material 分支优先使用 `ListItemDefaults.segmentedShapes(index, count)`。项目数量较多时仍需保持独立 lazy item 粒度，不能为了连续外观牺牲滚动性能。

### 设置项

- Switch 使用 Material 3 Expressive Switch 包装；整行负责唯一点击、写入、语义和触觉反馈。
- Checkbox 用于次级或并列布尔选项，不与主要 Switch 混淆层级。
- Slider 的标题/摘要区域与轨道之间保留独立间距，值文本不能挤压标题。
- `MeowPopupPreference` 使用 Material Dropdown/Popup；当前值与选项文字保持 Material 正文字号层级。
- 设置列表中的 leading icon、文本和 trailing 控件使用统一对齐线，不能因某一行有摘要而改变控件垂直中心规则。

### Tab、顶栏与底栏

- Tab 使用 connected button group，不使用普通小型 `TabRow` 冒充 Expressive。
- 选中项通过容器、形状、文字权重和动效共同表达，不能只依赖颜色变化。
- 顶栏使用可滚动大标题结构；展开、过渡和收起期间使用与正文连续的容器色或统一 Blur。
- 悬浮底栏使用紧凑胶囊容器，项目包含图标和单行文字，并保留系统导航栏安全区。
- 普通底栏与悬浮底栏共享业务状态，但样式和布局不能与 Miuix 分支共用。

### Blur

Material 分支通过统一 `MeowScaffoldEffect` 接入 Blur。Blur 不可用时回退到 Material 正确表面色，顶栏滚动色与正文容器仍需连续。

## Miuix

### 主题、配色与 squircle

- 优先使用 Miuix 官方组件和 Android example 的组合方式。
- 动态取色关闭时使用 Miuix 自身 Light/Dark 配色；浅色模式保持默认蓝/白体系，Material seed color 不得覆盖。
- Miuix 原生 Card、Button、IconButton、Preference、Dialog 和 Navigation 直接使用其 squircle，不额外套第二层圆角裁剪。
- 自定义 Miuix 形状按用途选择：纯色背景使用 squircle background，需要裁剪图片时使用 squircle clip，可点击且需要裁剪反馈时使用 squircle surface。
- 不为不可见的几何差异滥用 offscreen layer；小尺寸或无需裁剪的元素优先选择成本更低的实现。

### 页面与设置列表

- 设置页使用 Miuix 原生 TopAppBar、Card/Preference、overscroll、滚动结束反馈和下拉刷新组合，不套用 Material segmented list 外观。
- 设置分组的颜色、形状、内部留白和按压反馈来自 Miuix 或 MeowUI Miuix token。
- 大型动态分组保持独立 lazy item 粒度；首段、中段和末段用 Miuix squircle 规则拼成视觉连续卡片。
- 普通单选使用 Miuix Dropdown/Popup，并在设置行右侧以可读字号显示当前值。
- Popup 展开、选择和关闭期间保持稳定 key，不因临时状态重建造成文字或整行闪烁。

### Dialog

- Alert、Single choice、Text input 与 Loading 使用 Miuix 对应容器和布局，不用 Material Dialog 换色模拟。
- 单选行由整行承担点击与 `Role.RadioButton`；内部选择控件不再绑定第二次点击。
- 不叠加第二套 indication、长按遮罩或重复颜色动画。
- 确认式单选只在 Dialog 内保存临时选择，点击确认后提交；取消不修改正式值。
- 长内容放入受限高度的滚动区，按钮固定在底部；短内容自然收缩。

### Tab、顶栏与底栏

- `Standard` Tab 使用 Miuix `TabRow`，`Contour` 使用 `TabRowWithContour`，尺寸、形状和内边距由 Miuix 标准决定。
- Tab 不因与 Material 共用业务 API 而缩小一圈，也不套 connected button group 外观。
- 顶栏使用 Miuix 原生滚动行为，并与页面 overscroll、下拉刷新和 Blur 共享滚动上下文。
- 顶栏菜单使用 Miuix Popup，不用 Material Dropdown 模拟。
- 悬浮底栏使用 Miuix 自身颜色、squircle、反馈和安全区；与 Material 分支只共享选中状态和回调。

### Blur

- Miuix 内容提供 backdrop，顶栏和底栏通过统一 Scaffold effect 消费。
- Blur 激活时前景栏使用适合 Miuix texture blur 的透明表面；回退时恢复 Miuix 正确表面色。
- backdrop 为 `null` 是正常情况，不能因此改变布局或禁用基础导航。

## 组件选择

| 场景 | 推荐组件 | 不推荐 |
| --- | --- | --- |
| 普通单选设置 | `MeowPopupPreference` | 为短选项打开确认式 Dialog |
| 需要暂存并确认 | `MeowSingleChoiceDialog` | 点击选项立即写入正式值 |
| 文本需要校验 | `MeowTextInputPreference` / `MeowTextInputDialog` | 无限制的普通 Popup |
| 多个次要顶栏操作 | `MeowTopBarAction.Menu` | 固定放置 Bottom Sheet 按钮 |
| 页面补充内容 | `MeowBottomSheet` | 用 Dialog 承载可继续浏览的内容 |
| 轻量状态或错误提示 | `MeowTip` | 长时间阻塞页面的 Loading Dialog |
| 用户主动刷新 | `MeowPullToRefresh` | 另建一套与顶栏冲突的手势状态 |
| 列表/详情宽屏布局 | `MeowAdaptiveLayout` | 页面内复制两套状态与断点判断 |

## 视觉检查清单

修改列表、Popup、Dialog、Tab、顶栏、底栏、下拉刷新或 Blur 后，至少检查：

- Material 3 Expressive 与 Miuix 两种风格。
- 浅色、深色，以及动态取色关闭后的默认配色。
- 首次显示、数据为空、单项、多项、动态增减和重新排序。
- 长标题、长摘要、长当前值、较大字体和禁用状态。
- Popup 打开/选择/关闭，Dialog 确认/取消/系统返回和长内容滚动。
- 顶栏展开、滚动中、完全收起，以及普通底栏和悬浮底栏。
- Blur 开启、关闭、系统不支持和 backdrop 为 `null`。
- 窄屏、宽屏、横屏、系统栏、cutout 与导航手势区。
- 点击语义、选中语义、`contentDescription` 和重复点击目标。

没有实际检查的状态不得写成已通过。

## 参考方向

- [Material 3 Button groups](https://m3.material.io/components/button-groups/overview)
- [Miuix 官方 Android example](https://github.com/compose-miuix-ui/miuix/tree/main/example/android)
- [InstallerX-Revived](https://github.com/wxxsfxyzm/InstallerX-Revived)
- [KernelSU](https://github.com/tiann/KernelSU)
- [Mishka AGENTS.md](https://github.com/YuKongA/Mishka/blob/main/AGENTS.md)
- [Mishka CLAUDE.md](https://github.com/YuKongA/Mishka/blob/main/CLAUDE.md)

这些项目用于校验页面骨架、滚动、宽屏、分组卡片、squircle、Dialog、Blur 与导航体验。MeowUI 仍以双 UI 公共语义和自身模块边界为准。
