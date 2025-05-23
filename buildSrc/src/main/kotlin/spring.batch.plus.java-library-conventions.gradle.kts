plugins {
    // to use 'api(...)' for transitive dependencies
    // https://docs.gradle.org/current/userguide/java_library_plugin.html
    `java-library`

    // for managing custom test suite
    // https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html
    `jvm-test-suite`

    // coverage, jacoco can cover kotlin codes
    // https://docs.gradle.org/current/userguide/jacoco_plugin.html
    jacoco

    // linting
    // https://docs.gradle.org/current/userguide/checkstyle_plugin.html
    checkstyle
}


/* java-library */

java {
    toolchain {
        // It finds version corresponding to configuration
        // Also, it automatically sets sourceCompatibility & targetCompatibility to the same version
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar() // generate source jar
    withJavadocJar() // generate javadoc file
}

tasks.jar {
    // customizing manifest file
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
            ),
        )
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.javadoc {
    options {
        (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

testing {
    suites {
        // reference to test task which is automatically created
        val test by getting(JvmTestSuite::class) {
            targets {
                all {
                    testTask.configure {
                        useJUnitPlatform()
                        maxParallelForks = Runtime.getRuntime().availableProcessors()
                    }
                }
            }
        }

        register<JvmTestSuite>("integrationTest") {
            dependencies {
                // add dependency of project itself
                implementation(project())
            }

            targets {
                all {
                    testTask.configure {
                        // run after 'test' task
                        shouldRunAfter(test)

                        useJUnitPlatform()
                        maxParallelForks = Runtime.getRuntime().availableProcessors()
                    }
                }
            }
        }
    }
}

// configure dependencies integrationTest from project itself
// see also: https://github.com/gradle/gradle/issues/19870
val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
}
val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get(), configurations.testRuntimeOnly.get())
}


/* jacoco */

jacoco {
    toolVersion = "0.8.12"
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

tasks.jacocoTestReport {
    dependsOn(tasks.withType<Test>())

    // set execution data from all `.exec` files
    executionData.setFrom(files(project.fileTree("build/jacoco").include("*.exec")))

    reports {
        html.required = true
        xml.required = true
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacoco/html")
        xml.outputLocation = layout.buildDirectory.file("jacoco/result.xml")
    }

    doLast {
        val indexFile = project.layout.buildDirectory.dir("jacoco/html").get().file("index.html")
        println("jacoco html report is generated to $indexFile")
    }
}

tasks.jacocoTestCoverageVerification {
    enabled = false // disabled in subproject
}


/* checkstyle */

tasks.withType<Checkstyle>().configureEach {
    reports {
        configFile = file("${project.rootDir}/buildSrc/config/naver-checkstyle-rules.xml")
        configProperties = mapOf(
            "suppressionFile" to file("${project.rootDir}/buildSrc/config/naver-checkstyle-suppressions.xml"),
        )
        xml.required.set(false)
        html.required.set(true)
    }
}


/* other common */

// task to show all dependencies in all subprojects
// e.g. ./gradlew allDeps
tasks.register<DependencyReportTask>("allDeps") {
}
