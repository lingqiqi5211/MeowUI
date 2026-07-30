plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.maven.publish) apply false
}

// group 与 version 由根目录 gradle.properties 的 GROUP / VERSION_NAME 提供，
// vanniktech maven-publish 插件会读取同一份值作为发布坐标。
allprojects {
    group = property("GROUP") as String
    version = property("VERSION_NAME") as String
}
