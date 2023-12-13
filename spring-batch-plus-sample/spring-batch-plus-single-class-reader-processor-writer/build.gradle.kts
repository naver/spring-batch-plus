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
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch:3.2.0")
    implementation(project(":spring-boot-starter-batch-plus"))
    implementation("io.projectreactor:reactor-core:3.5.0")
    runtimeOnly("com.h2database:h2:2.1.214")
}
