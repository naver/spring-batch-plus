plugins {
    id("spring.batch.plus.java-library-conventions")
    id("spring.batch.plus.kotlin-conventions")
    id("spring.batch.plus.maven-publish-conventions")
}

dependencies {
    compileOnly(project(":spring-batch-plus"))
    compileOnly(project(":spring-batch-plus-kotlin"))

    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.batch.core)

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)
    testImplementation(libs.spring.boot.autoconfigure)
    testImplementation(libs.spring.boot.test)
    testImplementation(libs.spring.batch.core)
}
