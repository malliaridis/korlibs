package korlibs.gradle

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * Configures and applies all targets currently supported by korlibs.
 *
 * @param namespace Android namespace to use.
 * @param compileSdk SDK version to use for compiling (android-specific)
 * @param minSdk Minimum supported SDK version (android-specific)
 */
fun KotlinMultiplatformExtension.applyAllTargets(
    namespace: String,
    compileSdk: Int,
    minSdk: Int,
) {
    applyDefaultHierarchyTemplate()

    // equivalent to abiValidation { ... }
    @OptIn(ExperimentalAbiValidation::class)
    extensions.configure<AbiValidationMultiplatformExtension>("abiValidation") {
        enabled.set(true)
    }

    jvm()

    // Equivalent to android { ... }
    extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
        this.namespace = namespace
        this.compileSdk = compileSdk
        this.minSdk = minSdk

        androidResources.enable = true
        withHostTest {}
    }
    js {
        browser {
            compilerOptions {
                target.set("es2015")
            }
        }
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            compilerOptions {
                target.set("es2015")
            }
        }
        nodejs()
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm64()
    watchosArm32()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    mingwX64()
    linuxX64()
    linuxArm64()
    macosArm64()

    // Equivalent to sourceSets { ... }
    extensions.configure<NamedDomainObjectContainer<KotlinSourceSet>>("sourceSets") {
        // JVM and Android main sourcesets
        val jvmAndAndroidMain by creating {
            dependsOn(commonMain.get())
        }

        jvmMain {
            dependsOn(jvmAndAndroidMain)
        }

        androidMain {
            dependsOn(jvmAndAndroidMain)
        }

        // TODO Uncomment to apply kotlin test dependency to all modules
//        commonTest.dependencies {
//            implementation(kotlin("test"))
//        }

        // JVM and Android test sourcesets
        val jvmAndAndroidTest by creating {
            dependsOn(commonTest.get())
        }

        jvmTest {
            dependsOn(jvmAndAndroidTest)
        }

        getByName("androidHostTest") {
            dependsOn(jvmAndAndroidTest)
        }
    }
}
