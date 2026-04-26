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
        namespace = "org.korge.korlibs.math",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            api(projects.korlibsMathCore)
            api(projects.korlibsMathVector)
            api(projects.korlibsPlatform)
            api(projects.korlibsDatastructure)
            api(projects.korlibsNumber)
        }
        commonTest.dependencies {
            implementation(projects.korlibsPlatform)
            implementation(libs.kotlin.test)
        }
    }
}
