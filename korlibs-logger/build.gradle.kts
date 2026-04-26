import korlibs.gradle.applyAllTargets

plugins {
    id("org.korge.korlibs.gradle.conventions")
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    applyAllTargets(
        namespace = "org.korge.korlibs.logger",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            api(projects.korlibsPlatform)
            api(libs.kotlinx.atomicfu)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
