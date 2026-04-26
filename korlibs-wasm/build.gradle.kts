import com.android.build.gradle.internal.tasks.factory.dependsOn
import korlibs.gradle.applyAllTargets
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.withType
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
        namespace = "org.korge.korlibs.wasm",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            implementation(projects.korlibsPlatform)
            api(projects.korlibsMemory)
            api(projects.korlibsLogger)
            api(projects.korlibsMathCore)
            api(projects.korlibsDatastructure)
            implementation(projects.korlibsCompression)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(projects.korlibsIo)
        }

        androidMain.dependencies {
            implementation(projects.korlibsSerialization)
        }

        jvmMain.dependencies {
            implementation(libs.asm.asm)
            implementation(libs.asm.utils)
        }

        val appleIosTvosMacosMain by creating {
            dependsOn(appleMain.get())
        }

        iosMain {
            dependsOn(appleIosTvosMacosMain)
        }

        tvosMain {
            dependsOn(appleIosTvosMacosMain)
        }

        macosMain {
            dependsOn(appleIosTvosMacosMain)
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
