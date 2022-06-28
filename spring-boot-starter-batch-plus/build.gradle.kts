plugins {
    id("spring.batch.plus.java-library-conventions")
    id("spring.batch.plus.maven-publish-conventions")
}

dependencies {
    api(project(":spring-boot-autoconfigure-batch-plus"))
    api(project(":spring-batch-plus"))
    api(project(":spring-batch-plus-kotlin"))
}
