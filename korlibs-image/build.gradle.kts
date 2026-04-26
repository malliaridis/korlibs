import com.android.build.gradle.internal.tasks.factory.dependsOn
import korlibs.gradle.applyAllTargets
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable

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
        namespace = "org.korge.korlibs.image",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            api(projects.korlibsIo)
            implementation(projects.korlibsImageCore)
            api(projects.korlibsIoVfs)
            api(projects.korlibsMath)
            api(projects.korlibsFfiLegacy)
            api(projects.korlibsString)
            api(projects.korlibsPlatform)
            implementation(projects.korlibsCompression)
            implementation(projects.korlibsDyn)
            implementation(projects.korlibsMathCore)
            implementation(projects.korlibsEncoding)
            implementation(projects.korlibsSerialization)
            implementation(projects.korlibsWasm)
            implementation(projects.korlibsChecksum)
            api(libs.kotlinx.atomicfu)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        val appleIosTvosMain by creating {
            dependsOn(appleMain.get())
        }

        iosMain {
            dependsOn(appleIosTvosMain)
        }

        tvosMain {
            dependsOn(appleIosTvosMain)
        }
    }
}

// Configuration that links resources for native targets manually
kotlin.targets
    .withType<KotlinNativeTarget>()
    .configureEach {
        binaries.withType<TestExecutable>()
            .configureEach {
                val copyResources = tasks.register<Copy>("copy${target.name.capitalized()}TestResources") {
                    from("src/commonTest/resources")
                    into(outputDirectory)
                }
                linkTaskProvider.dependsOn(copyResources)
            }
    }
