plugins {
    id("spring.batch.plus.java-library-conventions")
    id("spring.batch.plus.kotlin-conventions")
    id("spring.batch.plus.coverage-conventions")
    id("spring.batch.plus.maven-publish-conventions")
}

dependencies {
    api(project(":spring-batch-plus"))

    compileOnly(libs.spring.batch.core)
    implementation(libs.slf4j)

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.spring.batch.core)
    testImplementation(libs.spring.jdbc)
    testImplementation(libs.reactor.core)
    testRuntimeOnly(libs.h2)
    testRuntimeOnly(libs.log4j)
}
