plugins {
    id("spring.batch.plus.java-library-conventions")
    id("spring.batch.plus.coverage-conventions")
    id("spring.batch.plus.maven-publish-conventions")
}

dependencies {
    compileOnly(libs.findbugs.jsr305)
    compileOnly(libs.spring.batch.core)
    compileOnly(libs.spring.jdbc)
    compileOnly(libs.reactor.core)
    implementation(libs.slf4j)

    testImplementation(libs.bundles.test.java)
    testImplementation(libs.spring.batch.core)
    testImplementation(libs.spring.batch.test)
    testImplementation(libs.reactor.core)
    testRuntimeOnly(libs.h2)
    testRuntimeOnly(libs.log4j)
}
