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
        namespace = "org.korge.korlibs.audio.core",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            implementation(projects.korlibsMathCore)
            implementation(projects.korlibsConcurrent)
            api(projects.korlibsMathVector)
            implementation(projects.korlibsFfiLegacy)
            implementation(projects.korlibsPlatform)
            implementation(projects.korlibsIoFs)
            implementation(projects.korlibsTime)
            implementation(projects.korlibsDatastructure)
            implementation(libs.jna.jna)
        }

        val appleIosTvosMain by creating {
            dependsOn(appleMain.get())
        }

        val appleIosTvosWatchosMain by creating {
            dependsOn(appleMain.get())
        }

        val appleNonWatchosMain by creating {
            dependsOn(appleMain.get())
        }

        macosMain.get().dependsOn(appleNonWatchosMain)

        iosMain {
            dependsOn(appleIosTvosMain)
            dependsOn(appleIosTvosWatchosMain)
            dependsOn(appleNonWatchosMain)
        }

        tvosMain {
            dependsOn(appleIosTvosMain)
            dependsOn(appleIosTvosWatchosMain)
            dependsOn(appleNonWatchosMain)
        }

        watchosMain.get().dependsOn(appleIosTvosWatchosMain)
    }
}
