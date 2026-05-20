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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
    implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:14.2.0")
}
