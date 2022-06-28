plugins {
    id("spring.batch.plus.java-library-conventions")
}

val springBootVersion = "2.7.1"
val springBatchVersion = "4.3.6"

dependencies {
    constraints {
        compileOnly("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
        compileOnly("org.springframework.batch:spring-batch-core:$springBatchVersion")

        testRuntimeOnly("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
        testImplementation("org.springframework.boot:spring-boot-test:$springBootVersion")
    }
}
