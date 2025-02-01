plugins {
    // to use basic tasks
    // https://docs.gradle.org/current/userguide/base_plugin.html
    base

    // to merge jacoco report of subprojects
    // https://docs.gradle.org/current/userguide/jacoco_report_aggregation_plugin.html
    `jacoco-report-aggregation`
}

// filter out non-target projects
val targetProjects = rootProject.subprojects
    // includes projects which have build.gradle.kts
    .filter { subproject -> subproject.projectDir.resolve("build.gradle.kts").exists() }
    // filter out sample projects
    .filter { subproject -> !subproject.name.endsWith("sample") }

// exclude classes by pattern
val exclusions: List<String> = listOf(
    // e.g.
    // "**/com/example/excluded/**",  // Exclude all classes in a specific package
    // "**/com/example/utils/*.class", // Exclude all classes in a specific folder
    // "**/MyClass.class"              // Exclude a specific class
)

tasks.register<JacocoReport>("mergedJacocoTestReport") {
    dependsOn(targetProjects.map { it.tasks.findByName("jacocoTestReport") })

    // set execution data from all `.exec` files
    executionData.setFrom(files(targetProjects.map { it.fileTree("build/jacoco").include("*.exec") }))

    // set source and class directories for all target projects
    sourceDirectories.setFrom(files(targetProjects.map { it.sourceSets["main"].allSource.srcDirs }))
    classDirectories.setFrom(files(targetProjects.map { it.sourceSets["main"].output.classesDirs }))

    reports {
        html.required = true
        xml.required = true
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacoco/html")
        xml.outputLocation = layout.buildDirectory.file("jacoco/result.xml")
    }

    // exclude specific classes or packages
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(exclusions)
                }
            },
        ),
    )

    doLast {
        val indexFile = project.layout.buildDirectory.dir("jacoco/html").get().file("index.html")
        println("Merged jacoco html report is generated to $indexFile")
    }

    // trigger verification
    finalizedBy("mergedJacocoTestCoverageVerification")
}

tasks.register<JacocoCoverageVerification>("mergedJacocoTestCoverageVerification") {
    // set execution data from all `.exec` files
    executionData.setFrom(files(targetProjects.map { it.fileTree("build/jacoco").include("*.exec") }))

    // set source and class directories for all target projects
    sourceDirectories.setFrom(files(targetProjects.map { it.sourceSets["main"].allSource.srcDirs }))
    classDirectories.setFrom(files(targetProjects.map { it.sourceSets["main"].output.classesDirs }))

    // to see supported types, check following links
    // element : https://www.eclemma.org/jacoco/trunk/doc/api/org/jacoco/core/analysis/ICoverageNode.ElementType.html
    // counter : http://www.eclemma.org/jacoco/trunk/doc/api/org/jacoco/core/analysis/ICoverageNode.CounterEntity.html
    // value : http://www.eclemma.org/jacoco/trunk/doc/api/org/jacoco/core/analysis/ICounter.CounterValue.html
    violationRules {
        rule {
            element = "BUNDLE"
            enabled = true

            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = 0.95.toBigDecimal()
            }

            limit {
                counter = "METHOD"
                value = "COVEREDRATIO"
                minimum = 0.95.toBigDecimal()
            }
        }
    }

    // exclude specific classes or packages
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(exclusions)
                }
            },
        ),
    )
}

tasks.check {
    dependsOn(tasks.named("mergedJacocoTestReport"))
}
