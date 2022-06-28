plugins {
    `java-library`
    jacoco
    checkstyle
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
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
                "Implementation-Version" to project.version
            )
        )
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        configFile = file("${project.rootDir}/src/checkstyle/naver-checkstyle-rules.xml")
        configProperties = mapOf(
            "suppressionFile" to file("${project.rootDir}/src/checkstyle/naver-checkstyle-suppressions.xml")
        )
        xml.required.set(false)
        html.required.set(true)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generate
}

tasks.jacocoTestReport {
    dependsOn(tasks.named<Test>("test")) // tests are required to run before generating the report

    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

dependencies {
    constraints {
        implementation("org.slf4j:slf4j-api:1.7.36")
        testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.assertj:assertj-core:3.23.1")
}
