dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.spring.io/milestone/")
    }
}

rootProject.name = "spring-batch-plus"

include(
    "spring-batch-plus",
    "spring-batch-plus-kotlin",
    "spring-boot-autoconfigure-batch-plus",
    "spring-boot-autoconfigure-batch-plus-kotlin",
    "spring-boot-starter-batch-plus",
    "spring-boot-starter-batch-plus-kotlin",

    // sample projects
    "spring-batch-plus-sample:clear-run-id-incrementer-sample",
    "spring-batch-plus-sample:clear-run-id-incrementer-kotlin-sample",
    "spring-batch-plus-sample:delete-meta-data-job-sample",
    "spring-batch-plus-sample:delete-meta-data-job-kotlin-sample",
    "spring-batch-plus-sample:kotlin-dsl-sample",
    "spring-batch-plus-sample:single-class-reader-processor-writer-sample",
    "spring-batch-plus-sample:single-class-reader-processor-writer-kotlin-sample",
)
