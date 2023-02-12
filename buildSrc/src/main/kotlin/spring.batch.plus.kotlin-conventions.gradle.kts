import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict", // enable jsr305 null-safety in kotlin
        )
        jvmTarget = "17"
        languageVersion = "1.5"
        apiVersion = "1.5"
    }
}
