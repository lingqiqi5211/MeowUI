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
        // miuix snapshots, for components that have landed upstream but are not in a
        // release yet (BreadcrumbBar). GitHub Packages needs a token even for public
        // packages: put `gpr.user` / `gpr.key` (a PAT with `read:packages`) in
        // ~/.gradle/gradle.properties, or export GITHUB_ACTOR / GITHUB_TOKEN.
        //
        // While a snapshot is in the version catalogue MeowUI CANNOT be published to
        // Maven Central: the POM would point at a coordinate nobody else can resolve.
        maven {
            name = "miuixSnapshots"
            url = uri("https://maven.pkg.github.com/compose-miuix-ui/miuix")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
            mavenContent { includeGroupAndSubgroups("top.yukonga.miuix.kmp") }
        }
    }
}

rootProject.name = "MeowUI"

include(":meowui")
include(":meowui-xposed")
include(":sample")
