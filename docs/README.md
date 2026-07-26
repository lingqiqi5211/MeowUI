# MeowUI 文档

MeowUI 面向 libxposed 模块设置页，提供一套业务 API 和两套独立视觉实现：Material 3 Expressive 与 Miuix。

## 阅读顺序

| 文档 | 内容 |
| --- | --- |
| [开始使用](getting-started.md) | 本地 composite build、模块选择、共享 key、设置页入口与风格切换 |
| [组件手册](components.md) | 设置项、Dialog、Popup、页面、导航、Bottom Sheet、Tip、下拉刷新与 Blur |
| [libxposed 接入](xposed.md) | libxposed API 102 的设置进程、Hook 进程、连接状态与错误处理 |
| [双 UI 设计规范](design-guidelines.md) | Material 3 Expressive 与 Miuix 的视觉边界、列表圆角、动效和模糊规则 |

## 快速选择

- 需要完整 libxposed 设置页入口：使用 `meowui-xposed`。
- Hook 进程只读取共享设置：使用 `meowui-libxposed`。
- 普通 Compose 页面只使用双 UI：使用 `meowui`。
- 需要顶栏或底栏模糊：额外使用 `meowui-blur`。

## 当前状态

项目尚未发布到 Maven Central 或 JitPack。文档中的 `0.1.0-SNAPSHOT` 仅用于本地 composite build，不是远程发布版本。
