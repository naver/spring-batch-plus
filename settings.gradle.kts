rootProject.name = "spring-batch-plus"

include("spring-batch-plus")
include("spring-batch-plus-kotlin")
include("spring-boot-autoconfigure-batch-plus")
include("spring-boot-autoconfigure-batch-plus-kotlin")
include("spring-boot-starter-batch-plus")
include("spring-boot-starter-batch-plus-kotlin")

include("spring-batch-plus-sample:spring-batch-plus-kotlin-dsl")
include("spring-batch-plus-sample:spring-batch-plus-single-class-reader-processor-writer")
include("spring-batch-plus-sample:spring-batch-plus-single-class-reader-processor-writer-kotlin")
include("spring-batch-plus-sample:spring-batch-plus-clear-run-id-incrementer")
include("spring-batch-plus-sample:spring-batch-plus-clear-run-id-incrementer-kotlin")
include("spring-batch-plus-sample:spring-batch-plus-delete-meta-data-job")
include("spring-batch-plus-sample:spring-batch-plus-delete-meta-data-job-kotlin")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.spring.io/milestone/")
    }
}
