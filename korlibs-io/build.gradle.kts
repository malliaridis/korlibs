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
        namespace = "org.korge.korlibs.io",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            api(projects.korlibsChecksum)
            api(projects.korlibsCompression)
            api(projects.korlibsJseval)
            api(projects.korlibsIoVfs)
            api(projects.korlibsIoStream)
            api(projects.korlibsIoNetworkCore)
            api(projects.korlibsMathCore)
            api(projects.korlibsMemory)
            api(projects.korlibsFfiLegacy)
            api(projects.korlibsCrypto)
            api(projects.korlibsEncoding)
            api(projects.korlibsPlatform)
            api(projects.korlibsDatastructure)
            api(projects.korlibsNumber)
            api(projects.korlibsTime)
            api(projects.korlibsLogger)
            api(projects.korlibsDyn)
            api(projects.korlibsString)
            api(projects.korlibsSerialization)
            api(projects.korlibsIoFs)
            api(libs.kotlinx.atomicfu)
            implementation(libs.jna.jna)
        }

        val concurrentMain by creating {
            dependsOn(commonMain.get())
        }

        val posixMain by creating {
            dependsOn(nativeMain.get())
        }

        nativeMain {
            dependsOn(concurrentMain)
        }

        linuxMain {
            dependsOn(posixMain)
        }

        appleMain {
            dependsOn(posixMain)
        }

        jvmMain {
            dependsOn(concurrentMain)
        }

        androidMain {
            dependsOn(concurrentMain)
        }

        commonTest.dependencies {
            implementation(projects.korlibsTime)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
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
