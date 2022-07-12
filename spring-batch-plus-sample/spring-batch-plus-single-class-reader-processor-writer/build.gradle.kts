buildscript {
    repositories {
        gradlePluginPortal() // give accees to gradle community plugins
    }
}

plugins {
    java
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring") version "1.7.10"
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict") // enable jsr305 null-safety in kotlin
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch:2.7.1")
    implementation(project(":spring-boot-starter-batch-plus"))
    // implementation("com.navercorp.spring:spring-boot-starter-batch-plus:0.1.0-SNAPSHOT")
    implementation("io.projectreactor:reactor-core:3.4.19")
    runtimeOnly("com.h2database:h2:2.1.214")
}
