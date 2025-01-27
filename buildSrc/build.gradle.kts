plugins {
    `kotlin-dsl` // support convension plugins in kotlin
}

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal() // give access to gradle community plugins
}

dependencies {
    // see also (compatibility matrix) : https://docs.gradle.org/current/userguide/compatibility.html
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:11.6.1")
    implementation("org.jetbrains.kotlinx:kover:0.6.1")
}
