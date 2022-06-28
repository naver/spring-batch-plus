plugins {
    id("spring.batch.plus.java-library-conventions")
    id("spring.batch.plus.maven-publish-conventions")
    id("spring.batch.plus.spring-conventions")
}

dependencies {
    implementation(project(":spring-batch-plus"))
    implementation(project(":spring-batch-plus-kotlin"))

    compileOnly("org.springframework.boot:spring-boot-autoconfigure")

    testRuntimeOnly("org.springframework.boot:spring-boot-autoconfigure")
    testImplementation("org.springframework.boot:spring-boot-test")
}
