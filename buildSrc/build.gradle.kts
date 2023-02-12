plugins {
    `kotlin-dsl` // support convension plugins in kotlin
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal() // give accees to gradle community plugins
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:10.3.0")
    implementation("org.jetbrains.kotlinx:kover:0.6.1")
}
