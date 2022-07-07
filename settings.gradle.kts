rootProject.name = "spring-batch-plus"

include("spring-batch-plus")
include("spring-batch-plus-kotlin")
include("spring-boot-autoconfigure-batch-plus")
include("spring-boot-starter-batch-plus")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val springBootVersion = "2.7.1"
            val springBatchVersion = "4.3.6"

            library(
                "spring-boot-autoconfigure",
                "org.springframework.boot:spring-boot-autoconfigure:$springBootVersion"
            )
            library(
                "spring-batch-core",
                "org.springframework.batch:spring-batch-core:$springBatchVersion"
            )
            library(
                "slf4j",
                "org.slf4j:slf4j-api:1.7.36"
            )

            // test only
            library(
                "junit",
                "org.junit.jupiter:junit-jupiter:5.+"
            )
            library(
                "assertj",
                "org.assertj:assertj-core:3.+"
            )
            library(
                "mockito",
                "org.mockito:mockito-core:3.+"
            )
            library(
                "mockito-kotlin",
                "org.mockito.kotlin:mockito-kotlin:4.+"
            )
            library(
                "spring-boot-test",
                "org.springframework.boot:spring-boot-test:$springBootVersion"
            )
            library(
                "spring-jdbc",
                "org.springframework:spring-jdbc:5.3.21"
            )
            library(
                "h2",
                "com.h2database:h2:2.1.214"
            )
            library(
                "log4j",
                "org.apache.logging.log4j:log4j-slf4j-impl:2.17.2"
            )
        }
    }
}
