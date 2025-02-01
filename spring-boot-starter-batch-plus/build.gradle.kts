plugins {
    id("spring.batch.plus.java-library-conventions")
    id("spring.batch.plus.maven-publish-conventions")
}

dependencies {
    api(project(":spring-batch-plus"))
}
