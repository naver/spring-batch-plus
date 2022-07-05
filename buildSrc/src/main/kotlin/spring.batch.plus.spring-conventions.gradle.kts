plugins {
    id("spring.batch.plus.java-library-conventions")
}

val springBootVersion = "2.7.1"
val springBatchVersion = "4.3.6"

dependencies {
    constraints {
        compileOnly("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
        compileOnly("org.springframework.batch:spring-batch-core:$springBatchVersion")

        testImplementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
        testImplementation("org.springframework.boot:spring-boot-test:$springBootVersion")
        testImplementation("org.springframework.batch:spring-batch-core:$springBatchVersion")
        testImplementation("org.springframework:spring-jdbc:5.3.21")
        testRuntimeOnly("com.h2database:h2:2.1.214")
    }
}
