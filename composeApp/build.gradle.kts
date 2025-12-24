import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.JavaVersion

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

val ktorVersion = "3.3.0"
val datetimeVersion = "0.6.1"
val kamelVersion = "1.0.8"

kotlin {
    // ✅ ONE SOURCE OF TRUTH: JVM toolchain 17 for ALL Kotlin/JVM tasks
    jvmToolchain(17)

    // ✅ REQUIRED because Android plugin is applied
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // ✅ Desktop target (Compose Desktop)
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        // ---------------- COMMON ----------------
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // AndroidX lifecycle (ok in commonMain for Compose MPP template)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Coroutines + Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            // Datetime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")

            // Ktor (shared)
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-client-logging:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

            // Kamel (image preview - cross platform)
            implementation("media.kamel:kamel-image-default:$kamelVersion")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // ---------------- ANDROID ----------------
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Ktor engine (Android)
            implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        }

        // ---------------- DESKTOP (JVM) ----------------
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)

                // Ktor engine (Desktop/JVM)
                implementation("io.ktor:ktor-client-java:$ktorVersion")
            }
        }
    }
}

android {
    namespace = "com.example.gulf_coast_hazard_briefs_kmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.example.gulf_coast_hazard_briefs_kmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    // ✅ match toolchain 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        // ✅ Because you renamed main.kt -> Main.kt
        mainClass = "com.example.gulf_coast_hazard_briefs_kmp.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.example.gulf_coast_hazard_briefs_kmp"
            packageVersion = "1.0.0"
        }
    }
}

// ✅ HARD GUARANTEE: pin versions (no drift)
configurations.all {
    resolutionStrategy.force(
        "org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion",
        "io.ktor:ktor-client-core:$ktorVersion",
        "io.ktor:ktor-client-content-negotiation:$ktorVersion",
        "io.ktor:ktor-client-logging:$ktorVersion",
        "io.ktor:ktor-serialization-kotlinx-json:$ktorVersion",
        "io.ktor:ktor-client-okhttp:$ktorVersion",
        "io.ktor:ktor-client-java:$ktorVersion"
    )
}