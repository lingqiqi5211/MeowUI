pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// 不叫 MeowUI：miuix 以复合构建引入后，类型安全工程访问器对整个构建树生效，根项目与
// :meowui 只差大小写，会撞生成文件名（Windows 上不区分大小写）。
rootProject.name = "meowui-project"

// 根构建用 miuix submodule；宿主 includeBuild 接入使用版本目录中的 Maven 版本。
// 避免嵌套复合构建中的 KMP Android variant 打包问题，宿主侧不再包含 Miuix 源码。
// 两条路径的版本说明见 README。
if (gradle.parent == null) {
    val miuixDir = file("third_party/miuix")
    require(miuixDir.resolve("settings.gradle.kts").isFile) {
        """
        miuix submodule is not checked out at ${miuixDir.absolutePath}

            git submodule update --init --recursive

        (from this repository's root, or clone with `git clone --recurse-submodules`.)
        """.trimIndent()
    }
    // Miuix 源码构建需要自己的 SDK 定位；仅同步 sdk.dir，保留其他本地属性。
    val hostLocalProperties = file("local.properties")
    if (hostLocalProperties.isFile) {
        val hostProperties = java.util.Properties()
        hostLocalProperties.inputStream().use(hostProperties::load)
        val sdkDir = hostProperties.getProperty("sdk.dir")
        val submoduleLocalProperties = miuixDir.resolve("local.properties")
        val submoduleProperties = java.util.Properties()
        if (submoduleLocalProperties.isFile) {
            submoduleLocalProperties.inputStream().use(submoduleProperties::load)
        }
        if (sdkDir != null && sdkDir != submoduleProperties.getProperty("sdk.dir")) {
            submoduleProperties.setProperty("sdk.dir", sdkDir)
            submoduleLocalProperties.outputStream().use { out ->
                submoduleProperties.store(out, "SDK synced from MeowUI/local.properties")
            }
        }
    }
    includeBuild(miuixDir)
}

include(":meowui")
include(":meowui-xposed")
include(":sample")
