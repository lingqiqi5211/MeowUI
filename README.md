# MeowUI

MeowUI 是面向 **libxposed 模块设置页** 的 Compose UI 库。业务层只编写一份页面、设置 key 与回调，运行时可以在 **Material 3 Expressive** 和 **Miuix** 之间切换；两套界面共用业务语义，但分别使用各自的布局、组件、配色与动效。

- 包名：`io.github.lingqiqi5211.meowui`
- 最低系统：Android 8.0（API 26）
- Xposed：仅支持 libxposed API 102
- 发布状态：尚未发布到 Maven Central 或 JitPack

## 特点

- **一次编写，两套 UI**：调用侧不需要维护 Material 与 Miuix 两份页面。
- **设置项直接绑定 key**：常用组件只需传入 `title`、`key` 和必要选项，即可自动读取、观察并写回设置。
- **各自遵循原生体系**：Material 分支使用 Material 3 Expressive 组件与动效；Miuix 分支使用 Miuix 官方组件与配色。
- **完整设置页组件**：包含设置列表、Dialog、Popup、滚动顶栏、Tab、普通/悬浮底栏、Bottom Sheet、Tip 和下拉刷新。
- **可选模糊**：顶栏与底栏可接入 Blur；不支持的设备会自动使用普通表面，不影响布局和操作。

## 模块

| 模块 | 用途 |
| --- | --- |
| `meowui-core` | `MeowUiStyle`、`PreferenceKey`、`PreferenceStore`；不依赖 Compose 或 libxposed |
| `meowui` | 双 UI 主题与全部 Compose 组件；不直接依赖 libxposed |
| `meowui-blur` | 可选 Blur 与安全回退 |
| `meowui-libxposed` | libxposed 远程设置连接，供设置进程与 Hook 进程使用 |
| `meowui-xposed` | 设置 Activity/Compose 入口，组合 `meowui` 与 `meowui-libxposed` |
| `sample` | 两套 UI 与公开 API 的预览应用 |

## 本地引入

MeowUI 当前只能通过源码使用。假设宿主项目与 MeowUI 是相邻目录，在宿主项目的 `settings.gradle.kts` 中加入：

```kotlin
includeBuild("../MeowUI")
```

设置页模块：

```kotlin
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui-xposed:0.1.0-SNAPSHOT")
    implementation("io.github.lingqiqi5211.meowui:meowui-blur:0.1.0-SNAPSHOT") // 可选
}
```

Hook 模块只需要远程设置能力时：

```kotlin
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui-libxposed:0.1.0-SNAPSHOT")
}
```

`0.1.0-SNAPSHOT` 是当前源码工程的本地坐标，不代表已经发布到任何远程仓库。

## 最小 key 绑定示例

设置页与 Hook 进程应引用同一份 key：

```kotlin
object SettingsKeys {
    val UiStyle = PreferenceKey("ui_style", "material")
    val Enabled = PreferenceKey("enabled", false)
    val Mode = PreferenceKey("mode", "balanced")
}
```

设置 Activity 中，设置项只需传入文本与 key：

```kotlin
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setMeowXposedContent(preferenceName = "settings") {
            val styleName by rememberMeowPreferenceValue(SettingsKeys.UiStyle)
            val style = if (styleName == "miuix") {
                MeowUiStyle.Miuix
            } else {
                MeowUiStyle.MaterialExpressive
            }

            MeowTheme(style = style) {
                MeowPreferencePage(title = "模块设置") {
                    MeowPreferenceSection(title = "界面") {
                        MeowPopupPreference(
                            title = "界面风格",
                            key = SettingsKeys.UiStyle,
                            options = listOf("material", "miuix"),
                            optionLabel = {
                                if (it == "miuix") "Miuix" else "Material 3 Expressive"
                            },
                        )
                    }

                    MeowPreferenceSection(title = "功能") {
                        MeowSwitchPreference(
                            title = "启用功能",
                            key = SettingsKeys.Enabled,
                        )
                        MeowPopupPreference(
                            title = "运行模式",
                            key = SettingsKeys.Mode,
                            options = listOf("balanced", "performance"),
                        )
                    }
                }
            }
        }
    }
}
```

## 文档

- [文档目录](docs/README.md)
- [开始使用](docs/getting-started.md)
- [组件手册](docs/components.md)
- [libxposed 接入](docs/xposed.md)
- [双 UI 设计规范](docs/design-guidelines.md)

许可证见 [LICENSE](LICENSE)。
