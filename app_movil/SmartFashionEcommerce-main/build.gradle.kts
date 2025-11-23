// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Plugins principales de Android y Kotlin
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false


    // Plugin de Serialization
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false

    // ✅ Plugin de Google Services (Firebase, Google Sign-In, etc.)
    id("com.google.gms.google-services") version "4.4.2" apply false
}

// ✅ Forma correcta en Kotlin DSL (Gradle 8+)
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}