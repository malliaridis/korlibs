plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.multiplatform.gradle.plugin)
    implementation(libs.android.library.gradle.plugin)
}

gradlePlugin {
    plugins.create(project.name) {
        // Configure plugin that makes the conventions functions available when applied
        // via `plugins { id("org.korge.korlibs.gradle.conventions") }`
        id = "org.korge.korlibs.gradle.conventions"
        implementationClass = "korlibs.gradle.GradleConventionsPlugin"
    }
}
