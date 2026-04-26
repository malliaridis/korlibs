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
        namespace = "org.korge.korlibs.datastructure",
        compileSdk = libs.versions.compileSdk.get().toInt(),
        minSdk = libs.versions.minSdk.get().toInt(),
    )

    sourceSets {
        commonMain.dependencies {
            api(projects.korlibsConcurrent)
            api(projects.korlibsTimeCore)
            api(projects.korlibsMathVector)
            api(projects.korlibsDatastructureCore)
        }
        commonTest.dependencies {
            implementation(projects.korlibsPlatform)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        val concurrentTest by creating {
            dependsOn(commonTest.get())
        }

        jvmTest {
            dependsOn(concurrentTest)
        }
        getByName("androidHostTest") {
            dependsOn(concurrentTest)
        }
        linuxTest {
            dependsOn(concurrentTest)
        }
        tvosTest {
            dependsOn(concurrentTest)
        }
        macosTest {
            dependsOn(concurrentTest)
        }
        iosTest {
            dependsOn(concurrentTest)
        }
        watchosTest {
            dependsOn(concurrentTest)
        }
        mingwTest {
            dependsOn(concurrentTest)
        }
    }
}
