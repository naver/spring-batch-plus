import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    repositories {
        gradlePluginPortal() // give accees to gradle community plugins
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict") // enable jsr305 null-safety in kotlin
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.8")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch-jdbc")
    implementation(project(":spring-boot-starter-batch-plus-kotlin"))
    implementation("io.projectreactor:reactor-core:3.8.7")
    runtimeOnly("com.h2database:h2:2.4.240")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
