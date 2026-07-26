plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    group = "io.github.lingqiqi5211.meowui"
    version = "0.1.0-SNAPSHOT"
}
