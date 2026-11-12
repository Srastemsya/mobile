import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(21)
    androidLibrary {
        namespace = "ru.srastemsya.uikit"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = 23
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
        androidResources.enable = true
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SrastemsyaUIKit"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.compose.foundation)
            api(libs.compose.material3)
            implementation(libs.compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        }
        androidMain.dependencies {
            api(libs.compose.liquid.glass)
            implementation("com.caverock:androidsvg-aar:1.4")
        }
    }
}

compose.resources {
    packageOfResClass = "ru.srastemsya.uikit.resources"
}
