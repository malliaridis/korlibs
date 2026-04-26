import korlibs.gradle.applyAllTargets
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget

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
        namespace = "org.korge.korlibs.datastructure.core",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.atomicfu)
        }

        val nonJsMain by creating {
            dependsOn(commonMain.get())
        }

        targets
            .filter { it.platformType != KotlinPlatformType.js && it !is KotlinMetadataTarget }
            .forEach { target ->
                getByName("${target.name}Main").dependsOn(nonJsMain)
            }
    }
}
