# MeowUI

MeowUI 是一套 **双风格 Compose UI 样式库**：业务层只编写一份页面、状态与回调，运行时即可在 **Material 3 Expressive** 与 **Miuix** 之间切换。两套界面共享同一套业务语义，但分别使用各自的原生布局、组件、配色与动效。

它的目标是给工具类应用——尤其是 **Xposed 模块**——提供一套开箱即用的成套 UI：主题体系、外观设置页、设置项组件、对话框与导航都已备好，模块作者不必从零搭界面。libxposed 集成是可选模块；**普通应用、Magisk 模块管理器等任何 Compose 应用同样可以使用**，只需提供一个 `PreferenceStore` 实现（或干脆不用偏好绑定，全部走受控状态）。`sample` 只是展示公开 API 的预览应用，库本身不依赖它。

## 特点

- **一次编写，两套 UI**：调用侧不维护 Material 与 Miuix 两份页面，风格分支全部封装在库内部，公共 API 不暴露任何 Material 或 Miuix 类型。
- **成套外观设置页**：内置 `MeowAppearancePage`，包含主题预览头图（按手机/折叠屏/平板自适应，可关闭或自定义）、色票取色、深浅模式、调色板风格与色彩标准、界面风格切换、预测式返回偏好与界面缩放；也可以用同一批积木（`MeowAppearancePreview`、`MeowColorPicker`、`MeowTabRow`、偏好分组）搭建完全自定义的外观页。
- **主题可定制**：种子色经 tonal palette 展开为完整 MD3 配色，Miuix 分支经 Monet 引擎生成同源配色（可整体关闭 Monet 回到 Miuix 原生配色）；支持多种调色板风格、2021/2025 色彩标准、系统动态取色与统一取色窗口；浅深色与配色切换在两个分支都做逐色平滑过渡，系统栏亮暗自动跟随主题。
- **设置项直接绑定 key**：常用组件只需传入 `title`、`key` 和必要选项，即可自动读取、观察并写回设置；写入结果通过 `PreferenceWriteResult` 统一上报。也全部提供受控（`value` + 回调）版本。
- **各自遵循原生体系**：Material 分支使用 Material 3 Expressive 组件、segmented 圆角与 Expressive 动效（悬浮底栏为滑动胶囊指示器）；Miuix 分支使用 Miuix 官方组件、squircle 与滚动反馈。
- **完整设置页组件**：设置分组与列表、Dialog、Popup、可滚动大标题顶栏（含返回键与图标菜单）、Tab、普通/悬浮底栏、Bottom Sheet、Tip 和下拉刷新。
- **可选模糊**：顶栏与底栏可接入 Blur；系统不支持时自动回退为普通表面，不影响布局和操作。

## 模块

| 构件 | 用途 |
| --- | --- |
| `meowui` | 双 UI 主题、全部 Compose 组件、外观设置页、偏好绑定与 Blur；**不依赖 libxposed，任何 Compose 应用可直接使用** |
| `meowui-xposed` | libxposed 集成：设置 Activity/Compose 入口 + Hook 进程的远程设置连接 |
| `sample` | 两套 UI 与公开 API 的预览应用（仅演示用，不发布） |

## 引入

### Xposed 模块（设置页与 Hook 进程同一构件）

```kotlin
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui-xposed:0.1.0")
}
```

### 普通应用 / Magisk 模块管理器

```kotlin
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui:0.1.0")
}
```

尚未发布到远程仓库前，可用源码接入：假设宿主项目与 MeowUI 是相邻目录，在宿主项目的 `settings.gradle.kts` 中加入 `includeBuild("../MeowUI")`，依赖坐标不变。

用 `MeowPreferenceProvider` 提供一个 `PreferenceStore` 实现（内存版 `InMemoryPreferenceStore` 可直接用；持久化版按需包装 SharedPreferences、DataStore 或你自己的配置通道，比如 Magisk 管理器的守护进程配置），其余用法与 Xposed 场景完全一致：

```kotlin
setMeowContent {
    MeowPreferenceProvider(store) {
        MeowTheme(appearance = appearance) {
            // MeowPreferencePage / MeowAppearancePage / ...
        }
    }
}
```

## 最小示例

设置页与 Hook 进程引用同一份 key：

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

            MeowTheme(
                style = style,
                seedColor = Color(0xFF7B4DFF),
                paletteStyle = MeowPaletteStyle.TonalSpot,
            ) {
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

分组内容是 `@Composable` DSL，行参数可以直接使用 `stringResource` 等资源调用；子页面顶栏通过 `MeowPreferencePage(onBackClick = ...)` 获得风格原生的返回按钮。

完整的外观设置能力（统一状态 `MeowAppearance` + 默认页 `MeowAppearancePage` + 自定义外观页指引）见[组件手册的「外观设置页」章节](docs/components.md#外观设置页)。

## 文档

- [文档目录](docs/README.md)
- [开始使用](docs/getting-started.md)
- [组件手册](docs/components.md)
- [libxposed 接入](docs/xposed.md)
- [双 UI 设计规范](docs/design-guidelines.md)

运行 `sample` 模块可在同一部设备上对比两套风格的真实效果（`./gradlew :sample:assembleDebug`）。

许可证见 [LICENSE](LICENSE)。
