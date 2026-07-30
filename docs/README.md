# MeowUI 文档

MeowUI 是双风格 Compose UI 样式库（Material 3 Expressive 与 Miuix），提供一套业务 API 和两套独立视觉实现；对 libxposed 模块提供开箱集成，普通 Compose 应用同样可用。

## 阅读顺序

| 文档 | 内容 |
| --- | --- |
| [开始使用](getting-started.md) | 依赖引入、模块选择、共享 key、设置页入口与风格切换 |
| [组件手册](components.md) | 设置项、Dialog、Popup、页面、外观设置页、导航、Bottom Sheet、Tip、下拉刷新与 Blur |
| [libxposed 接入](xposed.md) | libxposed API 的设置进程、Hook 进程、连接状态与错误处理 |
| [双 UI 设计规范](design-guidelines.md) | Material 3 Expressive 与 Miuix 的视觉边界、列表圆角、动效和模糊规则 |

## 快速选择

- 普通 Compose 应用 / Magisk 管理器等：使用 `meowui`（含主题、全部组件与 Blur）。
- libxposed 模块（设置页与 Hook 进程）：使用 `meowui-xposed`。
