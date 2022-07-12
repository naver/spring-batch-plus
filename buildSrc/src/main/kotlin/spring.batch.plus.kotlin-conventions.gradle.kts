import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

val targetVersion = "1.5"

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict", // enable jsr305 null-safety in kotlin
            "-language-version=$targetVersion",
            "-api-version=$targetVersion",
        )
        jvmTarget = "1.8"
    }
}
