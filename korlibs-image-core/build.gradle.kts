import korlibs.gradle.applyAllTargets
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation

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
        namespace = "org.korge.korlibs.image.core",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            implementation(projects.korlibsAnnotations)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.atomicfu)
        }
        jvmMain.dependencies {
            api(libs.jna.jna)
            api(libs.jna.platform)
        }
        webMain.dependencies {
            implementation(projects.korlibsWasm)
            implementation(projects.korlibsCompression)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

afterEvaluate {
    kotlin.targets.filter { it.platformType == KotlinPlatformType.native }.forEach { target ->
        if (target.name.contains("linux") || target.name.contains("mingw")) {
            target.compilations.getByName("main") {
                (this as KotlinNativeCompilation).cinterops {
                    val stb_image by creating {
                        defFile(project.file("nativeInterop/cinterop/stb_image.def"))
                    }
                }
            }
        }
    }
}
