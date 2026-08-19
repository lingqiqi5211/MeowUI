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

// miuix 是本仓库的 submodule，以复合构建方式引入。
//
// 当初改 submodule 是因为要用还没进正式版的组件（BreadcrumbBar、miuix-nav），而快照挂在
// GitHub Packages 上，公开包也要 token。这些组件已随 0.9.4-rc01 进入 Maven Central，
// submodule 现在钉在对应的发布 tag 上：本地按源码构建，发布出去的 POM 写的版本消费方
// 可以直接从 Central 解析。
//
// Gradle 会把 top.yukonga.miuix.kmp:* 这些坐标替换成 submodule 里的工程，版本目录里那几条
// 退化成「期望哪一版」的记录。
val miuixDir = file("third_party/miuix")
require(miuixDir.resolve("settings.gradle.kts").isFile) {
    """
    miuix submodule is not checked out at ${miuixDir.absolutePath}

        git submodule update --init --recursive

    (from this repository's root, or clone with `git clone --recurse-submodules`.)
    """.trimIndent()
}
// 内层构建按自己的 local.properties（或 ANDROID_HOME）找 SDK，新拉的 submodule 没有这份文件。
// 从本构建的那份播种一次，值变了也重写——SDK 挪了位置不会留下过期路径。两个文件都在 .gitignore 里。
val hostLocalProperties = file("local.properties")
if (hostLocalProperties.isFile) {
    val hostProperties = java.util.Properties()
    hostLocalProperties.inputStream().use(hostProperties::load)
    val sdkDir = hostProperties.getProperty("sdk.dir")
    val submoduleLocalProperties = miuixDir.resolve("local.properties")
    val seededSdkDir = submoduleLocalProperties
        .takeIf { it.isFile }
        ?.let { existing ->
            val properties = java.util.Properties()
            existing.inputStream().use(properties::load)
            properties.getProperty("sdk.dir")
        }
    if (sdkDir != null && sdkDir != seededSdkDir) {
        val seeded = java.util.Properties()
        seeded.setProperty("sdk.dir", sdkDir)
        submoduleLocalProperties.outputStream().use { out ->
            seeded.store(out, "Seeded from MeowUI/local.properties by settings.gradle.kts")
        }
    }
}
// 只在本仓库是根构建时替换成源码;被 includeBuild 进别人的构建时让 miuix 走 Central。
//
// 0.9.4-rc01 起 miuix 各模块用新的 KMP Android library 插件,嵌套一层后 project 依赖选不到
// 可运行的 android variant:编译期符号能看见,打进 APK 的类是空的,一进界面就
// NoClassDefFoundError。宿主侧再 includeBuild 一次也只是让编译过去,产物照样跑不起来。
//
// 两条路径钉同一个 tag,拿到的 miuix 一致。
if (gradle.parent == null) {
    includeBuild(miuixDir)
}

include(":meowui")
include(":meowui-xposed")
include(":sample")
