# MeowUI Agent Guide

`AGENTS.md` 只保留日常修改必须遵守的规则。组件用法、libxposed 接入和完整视觉要求分别放在 `docs/components.md`、`docs/xposed.md` 与 `docs/design-guidelines.md`；新增长期有效的约束时，应更新对应文档，不在本文件重复堆叠。

## 沟通

- 使用与使用者相同的语系，专有名词保留 English。
- 只报告结论、实际改动、原因和验证结果；未验证内容必须明确说明。
- 不写流水账，不用“先……再……”或“落地、推进”等工程汇报措辞。
- 多个方案并存时列出优缺点，并明确推荐项与原因。

## 快速参考

| 项目 | 约定 |
| --- | --- |
| 定位 | 面向 libxposed 模块设置页的 Compose UI 库 |
| 包名根路径 | `io.github.lingqiqi5211.meowui` |
| 最低系统 | Android 8.0（API 26） |
| Xposed | 仅 libxposed API 102，不兼容经典 API 82 |
| UI | Material 3 Expressive 与 Miuix，业务层只写一份页面 |
| JDK | 25；本机使用 `D:\qiqi\Codes\Java\jdk-25` |
| 字节码目标 | JVM 21，不因 JDK 25 改为 25 |

版本唯一来源：

- AGP、Kotlin、Compose、Miuix、libxposed 与 SDK：`gradle/libs.versions.toml`
- Gradle：`gradle/wrapper/gradle-wrapper.properties`
- JDK 主版本：`.java-version`

除非使用者明确要求，不升级、降级或替换工具链，也不把本机 JDK 路径写进构建脚本。

## 绝对规则

1. **一份业务 API，两套原生实现。** 统一组件语义、状态、key 绑定和回调；布局、控件、配色、形状、动效与反馈分别遵循 Material 3 Expressive 和 Miuix。
2. **风格分支留在库内部。** 调用侧不维护两份页面，公共 API 不暴露 Material 或 Miuix 私有类型。
3. **不要把 Miuix 做成换色 Material，也不要把 Material 做成仿 MIUI。** 优先使用各自官方组件和交互结构。
4. **首次显示必须正确。** 不得从错误圆角、数量、尺寸、颜色或选中态开始播放动画。
5. **Blur 是可选增强。** 关闭、不支持或 backdrop 不可用时，布局、点击区域和内容可读性必须保持不变。
6. **设置契约以 `PreferenceKey` 为准。** 设置页与 Hook 进程共享同一 key、类型、默认值和 `preferenceName`。
7. **先复用再新增。** 修改前检查现有组件、主题 token、设置绑定与存储 API；能够组合时不得重复实现。
8. **保持兼容。** 不轻易修改公开包名、函数名、参数顺序或默认值语义；确需修改时同步 sample、KDoc 和文档。

## 模块与依赖边界

| 模块 | 职责 | 不得包含 |
| --- | --- | --- |
| `meowui-core` | `MeowUiStyle`、`PreferenceKey`、`PreferenceStore` 与内存实现 | Compose、Activity、libxposed |
| `meowui` | 双 UI 主题、设置项、Dialog、Popup、Scaffold、导航、反馈与自适应布局 | libxposed 连接实现 |
| `meowui-blur` | 双风格 Blur 与安全回退 | 基础组件的必需依赖、业务页面 |
| `meowui-libxposed` | libxposed 远程设置连接与存储适配 | Compose UI、页面逻辑 |
| `meowui-xposed` | 组合 `meowui` 与 `meowui-libxposed` 的 Activity/Compose 入口 | 重复实现组件或存储逻辑 |
| `sample` | 展示公开 API 和两套 UI 的真实效果 | 库核心实现、只供示例使用的私有捷径 |

允许的主要依赖方向：

```text
meowui ---------> meowui-core
meowui-blur ----> meowui
meowui-libxposed -> meowui-core
meowui-xposed --> meowui + meowui-libxposed
sample ---------> 公开库模块
```

不得为了调用方便反转依赖，也不得让 `meowui-core`、`meowui-libxposed` 感知 Compose 页面生命周期。

## 公共组件与状态

- 新增可复用 Composable 时提供 `modifier: Modifier = Modifier`，作为第一个可选参数并应用到最外层可布局节点；不得为调整现有 API 而破坏 source compatibility。
- 公开参数描述业务含义，例如 `selected`、`onSelected`、`show`、`onDismissRequest`，不要暴露底层库专属状态对象。
- 受控状态由调用侧持有。Dialog 可持有临时输入或临时选择，但只在确认后提交。
- 同一个组件在两种风格下保持相同的提交时机、启用状态、取消行为和错误结果。
- 列表、Dialog 选项、Popup 项目、Tab 页面与动态导航项使用稳定 key。
- 整行可点击时只保留一个点击语义，内部 Switch、Checkbox 或 Radio 控件不得再次触发同一操作。
- 图标按钮必须提供 `contentDescription`；选择、开关与导航组件提供正确角色和选中状态。
- 颜色、间距、形状和动效优先来自当前设计体系或 MeowUI token，禁止在组件内散落相近的魔法值。

完整视觉与交互规则见 [`docs/design-guidelines.md`](docs/design-guidelines.md)。

## 设置与 libxposed

- `PreferenceKey` 支持 `Boolean`、`Int`、`Long`、`Float`、`String`、`Set<String>`；同名 key 不得以不同类型重复声明。
- key 默认值必须是远程设置不可用时的安全行为。已发布 key 不轻易改名；迁移时使用新 key 并提供迁移说明。
- key 绑定组件自动读取、观察和写入。`onCheckedChange`、`onValueChange` 等附加回调只通知业务，不重复写入同一个 key。
- 写入失败必须通过 `PreferenceWriteResult` 暴露，不得吞掉或伪装成功。
- 设置页使用 `XposedServicePreferenceStore`、`setMeowXposedContent` 或 `MeowXposedActivity`；Hook 进程使用 `XposedModulePreferenceStore` 或 `XposedModule.createPreferenceStore`。
- `preferenceName` 必须非空，设置进程与 Hook 进程使用同一个名称。
- Hook callback 不依赖 Compose、Activity 或设置页生命周期，不重复创建长期 Flow collector。
- service 不可用、读取失败或写入失败时不得让目标应用崩溃；持有者销毁时关闭 `XposedServicePreferenceStore`。

详细接入方式见 [`docs/xposed.md`](docs/xposed.md)。

## 双 UI 实现摘要

### Material 3 Expressive

- 优先使用 Expressive 组件、`MotionScheme`、segmented shapes 与 connected button group，不用普通 MD3 控件加圆角冒充。
- Switch、segmented list、可滚动大标题顶栏、Popup 和悬浮底栏必须符合 Expressive 结构。
- `MeowPreferenceSection` 在绘制前取得最终可见项列表，再计算单项、首项、中间项与末项形状。
- 顶栏展开、滚动与收起期间的背景必须与正文连续。

### Miuix

- 优先使用 Miuix 官方组件、squircle、滚动反馈、Popup、Dialog、Tab、Bottom Sheet 与 Blur 组合方式。
- 关闭动态取色时恢复 Miuix 自身 Light/Dark 配色；Material seed color 不得覆盖默认蓝/白体系。
- Dialog 单选项不得出现重复 indication、异常长按遮罩或打开/关闭闪烁。
- Popup 当前值、Tab、顶栏和悬浮底栏按 Miuix 标准尺寸与反馈实现，不复用 Material 外观。

## 文档

| 文件 | 内容 |
| --- | --- |
| [`README.md`](README.md) | 项目定位、模块选择、引入方式、最小示例与文档入口 |
| [`docs/getting-started.md`](docs/getting-started.md) | 创建页面、主题切换与基础接入 |
| [`docs/components.md`](docs/components.md) | 公开组件与参数语义 |
| [`docs/xposed.md`](docs/xposed.md) | libxposed 设置页与 Hook 进程接入 |
| [`docs/design-guidelines.md`](docs/design-guidelines.md) | 双 UI 视觉、布局、动效、Blur 与检查清单 |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | 贡献与提交要求 |

- README 不写开发流水账；详细内容放进 `docs/`。
- 未发布 Maven/JitPack 前，不填写看似可用的远程坐标，也不宣称依赖已经发布。
- 修改公开 API 时同步检查 KDoc、sample、README 与组件文档。
- 计划功能必须标明计划状态，完成并验证后才能写成“已提供”。

## 验证

开始修改前定义完成标准，交付前按改动范围检查。默认只做必要验证；耗时构建、安装 APK、联网发布或真机操作必须由使用者明确要求。

| 改动范围 | 最低检查 |
| --- | --- |
| 仅文档 | `git diff --check`、相对链接、代码围栏、API 名称、模块关系、发布状态 |
| `meowui-core` | 静态检查；获准构建时运行 `:meowui-core:testDebugUnitTest` |
| `meowui-libxposed` | 静态检查；获准构建时运行 `:meowui-libxposed:testDebugUnitTest` 与对应编译任务 |
| Compose 组件 | 静态检查；获准构建时至少编译 `:meowui` |
| sample 视觉 | 同时检查两种风格、浅色、深色、动态取色关闭、首次显示、状态切换与滚动；获准时构建 `:sample:assembleDebug` |
| Blur | 同时检查开启、关闭、系统不支持与 backdrop 为 `null` 的路径 |

没有执行的验证必须在回复中明确说明，不能写成已通过。

## Git 与工作区

- 工作区可能同时存在使用者或其他代理的改动，只修改任务允许的文件。
- 不还原、格式化、移动或删除无关文件，不覆盖他人的并行修改。
- 修改前后检查 `git status`；提交时只包含本次任务相关文件。
- 未经当前请求明确授权，不执行 `git add`、`git commit`、`git push`、发布、安装 APK 或创建 release。
- 不执行 `git reset --hard`、`git checkout --`、强制推送或其他破坏性操作。
- 不提交密钥、`local.properties`、构建产物或临时文件，不在日志和示例中泄露敏感数据。
