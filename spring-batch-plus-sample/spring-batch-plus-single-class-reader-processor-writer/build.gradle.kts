buildscript {
    repositories {
        gradlePluginPortal() // give accees to gradle community plugins
    }
}

plugins {
    java
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

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch:2.7.1")
    implementation(project(":spring-boot-starter-batch-plus"))
    // implementation("com.navercorp.spring:spring-boot-starter-batch-plus:0.1.0-SNAPSHOT")
    implementation("io.projectreactor:reactor-core:3.4.19")
    runtimeOnly("com.h2database:h2:2.1.214")
}
