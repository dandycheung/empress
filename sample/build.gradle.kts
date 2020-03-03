import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.nofrills.multimodule.apk")
    kotlin("android.extensions")
}

android {
    defaultConfig {
        applicationId = "io.nofrills.empress.sample"
    }
}

androidExtensions {
    isExperimental = true // for `@Parcelize` annotation
}

dependencies {
    implementation(project(":empress_android"))
    implementation(project(":empress_builder"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation(Deps.appCompat)
    implementation(Deps.constraintLayout)
    implementation(Deps.fragment)
    implementation(Deps.kotlinStdLib)

    testImplementation(Deps.coroutinesTest)
    testImplementation(Deps.junit)
}

tasks.withType(KotlinCompile::class).configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}
