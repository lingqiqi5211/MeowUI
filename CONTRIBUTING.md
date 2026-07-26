# 贡献说明

感谢参与 MeowUI。项目目标是让 libxposed 设置页只编写一份业务代码，同时获得符合各自规范的 Material 3 Expressive 与 Miuix 界面。

## 开始修改前

- 先阅读 [组件手册](docs/components.md) 与 [双 UI 设计规范](docs/design-guidelines.md)。
- 确认现有模块、组件、主题 token、`PreferenceKey` 绑定或存储 API 是否已经覆盖需求。
- 只修改任务需要的文件；工作区可能同时存在其他人的改动，不要还原或整理无关内容。
- 不自行升级 AGP、Gradle、Kotlin、Compose、Miuix、libxposed 或 Android SDK 版本。

## 模块选择

| 改动内容 | 所属模块 |
| --- | --- |
| 类型化 key、存储接口、无 UI 状态 | `meowui-core` |
| 双 UI 主题与 Compose 组件 | `meowui` |
| 模糊与无模糊回退 | `meowui-blur` |
| libxposed 远程设置连接 | `meowui-libxposed` |
| 设置 Activity 与 Compose 生命周期组合 | `meowui-xposed` |
| 公开 API 的实际预览 | `sample` |

不要让 `meowui-core` 依赖 Compose 或 libxposed，不要让 `meowui` 直接依赖 libxposed，也不要让基础 UI 反向依赖 `meowui-blur`。

## 组件贡献要求

- 业务层只调用一套 API，风格分支放在组件内部。
- Material 3 Expressive 与 Miuix 分别使用符合自身体系的组件、形状、间距、颜色和动效。
- 两套实现必须保持相同的状态所有权、提交时机、禁用逻辑和回调含义。
- 设置项优先提供 `PreferenceKey` 绑定入口；能复用现有 writer/provider 时不要另建保存逻辑。
- 首次显示直接使用正确圆角、尺寸、颜色和选中态，不从错误状态开始动画。
- Popup、Dialog、Bottom Sheet、Tip 和导航组件需要检查触控区域、字体放大、RTL 与无障碍语义。
- 模糊必须有正常的关闭和不支持设备回退，不能成为内容可读的前提。

新增公开 API 时，同时提供：

1. 清楚的参数与默认值。
2. Material 3 Expressive 和 Miuix 两套实现。
3. sample 中可操作的示例。
4. `docs/components.md` 中与源码一致的调用示例。

## libxposed 与安全

- 设置页与 Hook 进程共享同一份 `PreferenceKey` 和 `preferenceName`。
- Hook 逻辑在远程设置不可用时必须使用安全默认值，不得让目标应用崩溃。
- 不在 Hook callback 中依赖 Compose、Activity 或设置页生命周期。
- 不记录令牌、用户输入、私有路径或其他敏感值。
- 不引入经典 Xposed API 82 兼容实现；MeowUI 只面向 libxposed API 102。

## 文档

- README 只保留项目介绍、模块选择、引入方式、最小 key 绑定示例和文档入口。
- 详细接入、组件与设计说明写入 `docs/`。
- 文档中的函数名、类型名、参数名和模块关系必须与当前源码一致。
- MeowUI 尚未发布 Maven Central 或可确认的 JitPack 坐标；发布前不得写入虚构依赖。
- 不写开发流水账，也不把未验证能力描述为可用。

## 验证

按改动范围完成最小验证：

- 文档：检查相对链接、代码围栏、API 名称、过时描述和改动文件范围。
- `meowui-core` / `meowui-libxposed`：检查类型、连接状态、默认值与失败路径。
- Compose 组件：检查 Material 3 Expressive 与 Miuix 的浅色、深色、动态取色关闭、首次显示和交互状态。
- 模糊：同时检查启用、关闭和不支持设备的回退。
- sample：确认示例只调用公开 API。

除非任务明确要求，不执行无关的全量构建、联网、安装 APK、发布或额外报告。

## Git

- 不使用 `git reset --hard`、`git checkout --`、强制推送或其他会覆盖现有工作的命令。
- 不修改、移动或格式化任务范围外的文件。
- 提交前只暂存本次修改，并确认没有密钥、本机绝对路径、构建产物或临时文件。
- 未经明确要求，不提交、不推送、不创建 release。
