plugins {
    id("spring.batch.plus.java-library-conventions")
    id("spring.batch.plus.kotlin-conventions")
    id("spring.batch.plus.maven-publish-conventions")
}

dependencies {
    compileOnly(libs.spring.batch.core)
    implementation(libs.slf4j)

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)
    testImplementation(libs.spring.batch.core)
    testRuntimeOnly(libs.log4j)
}
