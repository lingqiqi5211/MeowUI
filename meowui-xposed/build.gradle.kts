plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}

android {
    namespace = "io.github.lingqiqi5211.meowui.xposed"
    compileSdk = libs.versions.compileSdk.get().toInt()
    compileSdkMinor = 0

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":meowui"))
    api(libs.androidx.activity.compose)

    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)

    testImplementation(libs.junit)
}

