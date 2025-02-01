import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    // kotlin itself
    // https://kotlinlang.org/docs/gradle-configure-project.html#targeting-the-jvm
    id("org.jetbrains.kotlin.jvm")

    // for lint
    // https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint")
}


/* kotlin */

kotlin {
    jvmToolchain(17) // use this version when development
    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict", // enable jsr305 null-safety in kotlin
        )
        jvmTarget = JvmTarget.JVM_17 // make class files for this version
        languageVersion = KotlinVersion.KOTLIN_1_6 // language feature level
        apiVersion = KotlinVersion.KOTLIN_1_6 // std api level
    }
}
