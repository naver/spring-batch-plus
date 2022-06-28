plugins {
    `kotlin-dsl` // support convension plugins in kotlin
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal() // give accees to gradle community plugins
}

// to remove following warning
// Task :buildSrc:compileKotlin
// 'compileJava' task (current target is 17) and 'compileKotlin' task (current target is 1.8) jvm target compatibility should be set to the same Java version.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20")
    implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:10.2.1")
}
