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
        namespace = "org.korge.korlibs.io.nodejs",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            implementation(projects.korlibsPlatform)
            implementation(projects.korlibsIo)
        }
        commonTest.dependencies {
            implementation(projects.korlibsTime)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
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
