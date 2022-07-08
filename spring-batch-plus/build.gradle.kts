plugins {
    id("spring.batch.plus.java-library-conventions")
    id("spring.batch.plus.maven-publish-conventions")
}

dependencies {
    compileOnly(libs.spring.batch.core)
    compileOnly(libs.reactor.core)
    implementation(libs.slf4j)

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)
    testImplementation(libs.spring.batch.core)
    testImplementation(libs.spring.batch.test)
    testImplementation(libs.reactor.core)
    testRuntimeOnly(libs.h2)
    testRuntimeOnly(libs.log4j)
}
