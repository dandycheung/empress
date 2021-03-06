import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    kotlin("jvm")
    id("jacoco")
}

java {
    sourceCompatibility = EmpressLib.javaCompat
    targetCompatibility = EmpressLib.javaCompat
}

dependencies {
    api(Deps.coroutinesCore)
    api(Deps.kotlinStdLib)
    testImplementation(Deps.junit)
    testImplementation(Deps.coroutinesTest)
}

tasks.withType(KotlinCompile::class).configureEach {
    kotlinOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xuse-experimental=kotlin.Experimental"
        )
        jvmTarget = EmpressLib.jvmTarget
    }
}

tasks.withType(Test::class) {
    testLogging {
        showStandardStreams = false
    }
}

tasks.withType(JacocoReport::class) {
    dependsOn(tasks.withType(Test::class))
    reports {
        html.isEnabled = true
        xml.isEnabled = true
    }
}

tasks.named("check") {
    dependsOn(tasks.withType(JacocoReport::class))
}

apply(from = "https://raw.githubusercontent.com/sky-uk/gradle-maven-plugin/${EmpressLib.mavPluginVersion}/gradle-mavenizer.gradle")
