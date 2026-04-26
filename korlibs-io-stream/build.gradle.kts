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
        namespace = "org.korge.korlibs.io.stream",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            api(projects.korlibsPlatform)
            api(projects.korlibsMemory)
            api(projects.korlibsDatastructureCore)
            api(projects.korlibsString)
            api(projects.korlibsMathCore)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(projects.korlibsTime)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
