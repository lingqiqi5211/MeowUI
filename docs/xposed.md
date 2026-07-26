# libxposed 接入

MeowUI **只支持 libxposed API 102**，本文所有示例均采用 API 102。

## 模块关系

```text
共享 PreferenceKey
├─ 设置进程：meowui-xposed → XposedServicePreferenceStore → libxposed service
└─ Hook 进程：meowui-libxposed → XposedModulePreferenceStore → remote preferences
```

- `meowui-core`：定义 key、连接状态、写入结果与存储接口。
- `meowui-libxposed`：连接 libxposed 远程设置，不包含 Compose UI。
- `meowui-xposed`：提供设置 Activity/Compose 入口，并自动管理设置进程的存储生命周期。

## 依赖

本项目尚未发布，以下坐标只用于本地 composite build：

```kotlin
// 设置页模块
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui-xposed:0.1.0-SNAPSHOT")
}

// Hook 模块
dependencies {
    implementation("io.github.lingqiqi5211.meowui:meowui-libxposed:0.1.0-SNAPSHOT")
}
```

宿主项目仍应按自己的 libxposed API 102 模块结构完成入口和清单配置；MeowUI 不替代 libxposed 本身的模块声明。

## 共享 key 与 preference name

设置进程和 Hook 进程必须引用同一份 key，并使用完全相同的非空 `preferenceName`：

```kotlin
const val PreferenceName = "settings"

object SettingsKeys {
    val Enabled = PreferenceKey("enabled", false)
    val Mode = PreferenceKey("mode", "balanced")
    val Threshold = PreferenceKey("threshold", 0.5f)
}
```

要求：

- 同名 key 在所有进程中保持同一类型。
- 默认值必须能作为远程设置不可用时的安全行为。
- 不在设置页和 Hook 模块分别复制字符串常量。
- 已发布 key 不随意改名；需要变更时创建新 key 并执行明确迁移。

## 设置进程

### 推荐：`setMeowXposedContent`

```kotlin
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setMeowXposedContent(
            preferenceName = PreferenceName,
            onWriteResult = ::handleWriteResult,
        ) {
            MeowTheme(style = MeowUiStyle.MaterialExpressive) {
                SettingsPage()
            }
        }
    }

    private fun handleWriteResult(result: PreferenceWriteResult) {
        when (result) {
            PreferenceWriteResult.Success -> Unit
            is PreferenceWriteResult.NotConnected -> {
                // 更新页面状态并显示连接提示
            }
            is PreferenceWriteResult.Failure -> {
                // 显示保存失败提示，并记录 result.cause
            }
        }
    }
}
```

该入口会创建 `XposedServicePreferenceStore`、提供 `MeowPreferenceProvider`，并在持有者销毁时关闭连接。

### 基类：`MeowXposedActivity`

```kotlin
class SettingsActivity : MeowXposedActivity() {
    override val preferenceName = PreferenceName

    @Composable
    override fun Content() {
        MeowTheme(style = MeowUiStyle.MaterialExpressive) {
            SettingsPage()
        }
    }

    override fun onWriteResult(result: PreferenceWriteResult) {
        // 显示连接或保存失败提示
    }
}
```

已经有其他 Activity 基类时使用 `setMeowXposedContent`；否则可以使用 `MeowXposedActivity`。

### 直接绑定 key

页面位于上述入口内后，设置项只需传入文本和 key：

```kotlin
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
```

组件会读取、观察并写回当前 `PreferenceStore`。附加回调用于业务通知，不要在回调中再次写入同一个 key。

## 连接状态

```kotlin
val connectionState by rememberMeowPreferenceConnectionState()
```

状态包括：

- `PreferenceConnectionState.Connecting`
- `PreferenceConnectionState.Connected`
- `PreferenceConnectionState.Disconnected(cause)`

连接未建立或已经断开时，key 绑定的写入型设置项会自动不可操作。建议使用 `MeowTip` 显示状态，不要用无法关闭的 Loading Dialog 长期阻塞页面。

## Hook 进程

在已有的 `XposedModule` 实例中创建一次长期存储：

```kotlin
class ModuleSettings(module: XposedModule) : AutoCloseable {
    private val store = module.createPreferenceStore(PreferenceName)

    fun isEnabled(): Boolean = store.read(SettingsKeys.Enabled)

    fun mode(): String = store.read(SettingsKeys.Mode)

    override fun close() {
        store.close()
    }
}
```

`createPreferenceStore` 返回 `XposedModulePreferenceStore`。不要在每次 Hook 回调中重复创建存储；在对应模块或进程持有者结束时调用 `close()`。

远程设置不可用时，读取会回退到 key 默认值。Hook 逻辑必须保证只得到默认值时也能安全运行，不得让目标应用崩溃。

## 观察设置变化

只在确实需要运行时更新时长期观察：

```kotlin
scope.launch {
    store.observe(SettingsKeys.Enabled).collect { enabled ->
        updateRuntimeState(enabled)
    }
}
```

- `observe` 会先发出当前值，再发出后续变化。
- 使用与目标进程生命周期一致的 `CoroutineScope`。
- 不在每次 Hook callback 中重复启动 collector。
- 只需要一次判断时直接使用 `read`。

## 手动管理设置进程存储

需要自行控制生命周期时：

```kotlin
val store = remember { XposedServicePreferenceStore(PreferenceName) }

DisposableEffect(store) {
    onDispose { store.close() }
}

MeowPreferenceProvider(
    store = store,
    onWriteResult = ::handleWriteResult,
) {
    SettingsPage()
}
```

一般优先使用 `setMeowXposedContent` 或 `MeowXposedActivity`，避免遗漏关闭连接。

## 写入结果

| 结果 | 含义 | 页面处理 |
| --- | --- | --- |
| `PreferenceWriteResult.Success` | 已提交 | 通常无需提示 |
| `PreferenceWriteResult.NotConnected` | service 当前不可用 | 显示连接提示并保留安全状态 |
| `PreferenceWriteResult.Failure` | 写入失败 | 显示错误 Tip，并保留原始异常供日志记录 |

不得吞掉失败，也不得把失败显示成保存成功。

## key 类型与迁移

错误示例：

```kotlin
// 已发布版本
val Mode = PreferenceKey("mode", "balanced")

// 错误：同名 key 改成 Int
val Mode = PreferenceKey("mode", 0)
```

正确做法是使用新名称，并由模块版本明确处理旧值：

```kotlin
val ModeV2 = PreferenceKey("mode_v2", 0)
```

## 排查

### 设置项全部不可操作

检查：

1. 页面是否位于 `setMeowXposedContent`、`MeowXposedActivity` 或 `MeowXposedPreferenceHost` 内。
2. `rememberMeowPreferenceConnectionState()` 是否为 `Connected`。
3. libxposed service 是否可用。

### 设置页已修改，Hook 仍读取默认值

检查：

1. 两端是否引用同一个 `PreferenceKey` 定义。
2. `preferenceName` 是否完全一致。
3. 同名 key 类型是否一致。
4. Hook 逻辑是否把旧结果长期缓存，而没有重新读取或观察。

### 写入失败但界面短暂变化

以 `PreferenceWriteResult` 为准。失败时显示明确提示，不要仅依据组件的短暂交互状态判断已经保存。
