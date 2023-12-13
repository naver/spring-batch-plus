import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain(17) // use this version when development
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict", // enable jsr305 null-safety in kotlin
        )
        jvmTarget = "17" // make class files for this version
        languageVersion = "1.6" // code level
        apiVersion = "1.6" // runtime level
    }
}
