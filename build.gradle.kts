plugins {
    id("org.jetbrains.kotlinx.kover")
}

// disable for root project
kover {
    isDisabled.set(true)
    engine.set(kotlinx.kover.api.DefaultIntellijEngine)
}

koverMerged {
    enable()

    filters {
        projects {
            excludes += listOf(
            //     ":spring-batch-plus-sample:spring-batch-plus-kotlin-dsl",
            //     ":spring-batch-plus-sample:spring-batch-plus-single-class-reader-processor-writer",
            //     ":spring-batch-plus-sample:spring-batch-plus-single-class-reader-processor-writer-kotlin",
                ":spring-batch-plus-sample:spring-batch-plus-clear-run-id-incrementer",
                ":spring-batch-plus-sample:spring-batch-plus-clear-run-id-incrementer-kotlin",
                ":spring-batch-plus-sample:spring-batch-plus-delete-meta-data-job",
                ":spring-batch-plus-sample:spring-batch-plus-delete-meta-data-job-kotlin"
            )
        }
    }

    xmlReport {
        onCheck.set(true)
        reportFile.set(layout.buildDirectory.file("kover/result.xml"))
    }

    htmlReport {
        onCheck.set(true)
        reportDir.set(layout.buildDirectory.dir("kover/html"))
    }

    verify {
        onCheck.set(true)
        rule {
            isEnabled = true
            name = "Test Coverage Rule"
            target =
                kotlinx.kover.api.VerificationTarget.ALL // specify by which entity the code for separate coverage evaluation will be grouped

            bound {
                minValue = 90
                maxValue = 100
                counter = kotlinx.kover.api.CounterType.INSTRUCTION
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}
