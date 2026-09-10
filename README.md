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
    implementation("io.github.lingqiqi5211.meowui:meowui-xposed:0.1.5")
}
```

### 普通应用 / Magisk 模块管理器

```kotlin
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui:0.1.5")
}
```

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

### 源码级接入（composite build）

可以通过 composite build 接入 MeowUI 源码，Miuix 依赖从 Maven Central 解析：

```bash
git clone --recurse-submodules https://github.com/lingqiqi5211/MeowUI.git
```

然后在宿主项目的 `settings.gradle.kts` 中加入（路径按实际目录调整）：

```kotlin
includeBuild("../MeowUI")
```

依赖坐标保持不变，Gradle 会把 `io.github.lingqiqi5211.meowui:*` 替换为源码工程。只有直接构建本仓库时，Miuix 才通过 submodule 参与源码构建。

**注意事项：**

- **submodule 必须先拉出来**：已 clone 的仓库执行 `git submodule update --init --recursive`；当前配置会检查 submodule 是否存在。
- **Android SDK 定位**：MeowUI 会把自己 `local.properties` 里的 `sdk.dir` 播种给 miuix；若 MeowUI 目录下没有这份文件（如 CI 环境），请保证 `ANDROID_HOME` 已设置。
- **工具链对齐**：复合构建运行在宿主 Gradle 上。以本仓库的 `gradle/wrapper/gradle-wrapper.properties`、`.java-version` 与 `gradle/libs.versions.toml` 为准；当前验证组合为 Gradle 9.7.1、JDK 25、AGP 9.4.0、Kotlin 2.4.20，字节码目标仍为 JVM 21。
- **宿主的 Miuix 版本**：按 Maven 依赖解析规则选择；宿主另行声明版本可能改变最终解析结果，需要核对兼容性。
- **源码冷构建**：直接构建本仓库时包含 Miuix 源码编译；宿主 `includeBuild` 接入不包含这一层源码构建。
- **工程名避让**：复合构建树内的工程名不要与 `meowui`、`miuix` 只差大小写（Windows 不区分大小写，类型安全访问器的生成文件会撞名）。

### 依赖的 miuix 版本

- **本仓库根构建**：submodule 固定到上游 main 提交 `18590f7cdcbba6bed6d6ba8d21ceb0124e4c399b`，包含 TabRow 横向嵌套滚动修复和导航项颜色配置。
- **宿主 `includeBuild` 接入**：Maven 版本仍为 **0.9.4-rc01**，包含 `BreadcrumbBar` 和 `miuix-nav`。二进制接入按对应版本的 POM 解析。

两条路径使用的 Miuix 版本不同；main 的新增修复不会自动进入 Maven 依赖。

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
