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
    "spring-batch-plus-sample:spring-batch-plus-kotlin-dsl",
    "spring-batch-plus-sample:spring-batch-plus-single-class-reader-processor-writer",
    "spring-batch-plus-sample:spring-batch-plus-single-class-reader-processor-writer-kotlin",
    "spring-batch-plus-sample:spring-batch-plus-clear-run-id-incrementer",
    "spring-batch-plus-sample:spring-batch-plus-clear-run-id-incrementer-kotlin",
    "spring-batch-plus-sample:spring-batch-plus-delete-meta-data-job",
    "spring-batch-plus-sample:spring-batch-plus-delete-meta-data-job-kotlin",
)
