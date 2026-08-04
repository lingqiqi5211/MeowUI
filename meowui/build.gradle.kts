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
    namespace = "io.github.lingqiqi5211.meowui"
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

    testOptions {
        unitTests {
            // Robolectric needs the library's own resources and themes to inflate a host
            // activity for compose tests.
            isIncludeAndroidResources = true
            all {
                // One instrumented android-all jar per sandbox does not fit in the default
                // test-JVM heap; without this it fails while loading the jar rather than as
                // a test failure.
                it.maxHeapSize = "2g"
            }
        }
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.ui.graphics)
    api(libs.compose.foundation)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.materialkolor)
    implementation(libs.miuix.ui)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.blur.android)
    implementation(libs.miuix.nav)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    // The library's own UI is testable in the JVM: components whose behaviour is a
    // recomposition rule (preference-group collection, sheet expansion, tip layout) are
    // regression-tested here rather than in whichever app notices the breakage.
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    debugImplementation(libs.compose.ui.test.manifest)
}
