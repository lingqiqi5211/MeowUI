# 开始使用

## 基本要求

- Android 8.0（API 26）及以上
- Compose 项目
- libxposed API/service（仅 Xposed 场景，版本见 `gradle/libs.versions.toml`）
- MeowUI 当前版本：`0.1.2`

发布到 Maven Central 后可直接添加依赖；尚未发布前，也可以按下文用本地源码接入，依赖坐标一致。

## 使用 composite build

目录示例：

```text
Projects/
├─ MeowUI/
└─ MyXposedModule/
```

在宿主项目的 `settings.gradle.kts` 中加入：

```kotlin
includeBuild("../MeowUI")
```

路径按实际目录调整。

### 设置页模块

```kotlin
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui-xposed:0.1.2")
}
```

Blur 能力已内置在 `meowui` 的顶栏与悬浮底栏中（由 `MeowAppearance.blurEnabled` 统一控制），其底层依赖 `miuix-blur-android` 声明 minSdk 33。**只要应用的 minSdk 低于 33（无论是否开启 Blur），主 Manifest 都必须包含**下面的 override（运行时低版本会自动回退为不模糊，不会崩溃）：

```xml
<manifest xmlns:tools="http://schemas.android.com/tools">
    <uses-sdk tools:overrideLibrary="top.yukonga.miuix.kmp.blur" />
</manifest>
```

### Hook 模块 / 共享 key 模块

Hook 进程的远程设置连接与 `PreferenceKey` 等定义也在 `meowui-xposed` 中（`io.github.lingqiqi5211.meowui.libxposed` 与 `io.github.lingqiqi5211.meowui.core.preference` 包）：

```kotlin
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui-xposed:0.1.2")
}
```

普通应用（不涉及 Xposed）只需要 UI 库本体：

```kotlin
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui:0.1.2")
}
```

## 定义共享 key

`PreferenceKey` 同时包含名称、类型和默认值。设置页与 Hook 进程必须使用同一份定义：

```kotlin
object SettingsKeys {
    val UiStyle = PreferenceKey("ui_style", "material")
    val Enabled = PreferenceKey("enabled", false)
    val ShowDetails = PreferenceKey("show_details", false)
    val Intensity = PreferenceKey("intensity", 0.5f)
    val Mode = PreferenceKey("mode", "balanced")
    val Nickname = PreferenceKey("nickname", "Meow")
}
```

支持的值类型：

- `Boolean`
- `Int`
- `Long`
- `Float`
- `String`
- `Set<String>`

同名 key 不得在不同进程中声明为不同类型。默认值应保证远程设置不可用时仍是安全行为。

## 创建设置 Activity

推荐使用 `setMeowXposedContent`。它会创建并管理远程设置连接，并把 `PreferenceStore` 提供给页面内的 key 绑定组件。

```kotlin
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setMeowXposedContent(
            preferenceName = "settings",
            onWriteResult = ::handleWriteResult,
        ) {
            SettingsContent()
        }
    }

    private fun handleWriteResult(result: PreferenceWriteResult) {
        when (result) {
            PreferenceWriteResult.Success -> Unit
            is PreferenceWriteResult.NotConnected -> Unit
            is PreferenceWriteResult.Failure -> Unit
        }
    }
}
```

也可以继承 `MeowXposedActivity`：

```kotlin
class SettingsActivity : MeowXposedActivity() {
    override val preferenceName = "settings"

    @Composable
    override fun Content() {
        SettingsContent()
    }
}
```

## 一次编写，两套 UI

```kotlin
@Composable
private fun SettingsContent() {
    val styleName by rememberMeowPreferenceValue(SettingsKeys.UiStyle)
    val style = if (styleName == "miuix") {
        MeowUiStyle.Miuix
    } else {
        MeowUiStyle.MaterialExpressive
    }

    MeowTheme(
        style = style,
        dynamicColor = false,
    ) {
        SettingsPage()
    }
}
```

Material 3 Expressive 分支的配色始终由种子色展开为完整的 MD3 tonal palette：

- `dynamicColor = true` 且系统为 Android 12+ 时，以系统主色为种子。
- 否则以 `seedColor` 为种子。
- `paletteStyle`（`MeowPaletteStyle`，默认 `TonalSpot`）决定种子色的展开风格，
  例如 `Expressive`、`Vibrant`、`Monochrome`；支持 2025 color spec 的风格会自动启用。
- 浅深色或种子切换时所有色 role 平滑过渡。

Miuix 分支经 Miuix Monet 引擎生成同源配色：`dynamicColor = true` 时以系统主色为种子，
关闭时同样使用 `seedColor` 与 `paletteStyle`，两种风格的主题色保持一致。

## 传入文本与 key

```kotlin
@Composable
private fun SettingsPage() {
    MeowPreferencePage(title = "模块设置") {
        MeowPreferenceSection(title = "功能") {
            MeowSwitchPreference(
                title = "启用功能",
                key = SettingsKeys.Enabled,
            )

            MeowCheckboxPreference(
                title = "显示详细信息",
                key = SettingsKeys.ShowDetails,
            )

            MeowSliderPreference(
                title = "强度",
                key = SettingsKeys.Intensity,
                valueRange = 0f..1f,
                valueText = { "${(it * 100).toInt()}%" },
            )

            MeowPopupPreference(
                title = "运行模式",
                key = SettingsKeys.Mode,
                options = listOf("balanced", "performance", "battery"),
            )

            MeowTextInputPreference(
                title = "昵称",
                key = SettingsKeys.Nickname,
                allowBlank = false,
            )
        }
    }
}
```

这些组件会自动读取、观察并写回最近的 `PreferenceStore`。连接不可用时，写入型组件会进入不可操作状态，写入结果由 `onWriteResult` 统一接收。

## Popup 与 Dialog 的选择

普通设置选项优先使用 `MeowPopupPreference`，用户可以直接从 Dropdown/Popup 中选择并写回。

只有以下情况才使用 `MeowSingleChoiceDialog`：

- 需要先暂存选择，再按“确认”提交。
- 选项需要更多说明或更复杂内容。
- 操作本身需要明确的确认步骤。

## 受控组件

组件也提供不绑定 key 的受控版本，适合临时状态或非持久化内容：

```kotlin
var enabled by remember { mutableStateOf(false) }

MeowPreferenceSection(title = "临时状态") {
    MeowSwitchPreference(
        title = "本次会话启用",
        checked = enabled,
        onCheckedChange = { enabled = it },
    )
}
```

两种写法只应选择一种。已经传入 key 时，不要在附加回调里再次写入同一个 key。

## 下一步

- 查看 [组件手册](components.md)
- 查看 [libxposed 接入](xposed.md)
- 查看 [双 UI 设计规范](design-guidelines.md)
- 运行 `sample` 预览两套界面
