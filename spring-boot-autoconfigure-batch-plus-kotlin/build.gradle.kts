plugins {
    id("spring.batch.plus.java-library-conventions")
    id("spring.batch.plus.kotlin-conventions")
    id("spring.batch.plus.coverage-conventions")
    id("spring.batch.plus.maven-publish-conventions")
}

dependencies {
    compileOnly(project(":spring-batch-plus-kotlin"))

    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.batch.core)

    testImplementation(project(":spring-batch-plus-kotlin"))
    testImplementation(libs.bundles.test.kotlin)
    testImplementation(libs.spring.boot.autoconfigure)
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.spring.batch.core)

    testRuntimeOnly(libs.h2)
}
