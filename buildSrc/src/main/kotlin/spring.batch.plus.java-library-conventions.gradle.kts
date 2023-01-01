plugins {
    `java-library`
    checkstyle
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
        configFile = file("${project.rootDir}/buildSrc/config/naver-checkstyle-rules.xml")
        configProperties = mapOf(
            "suppressionFile" to file("${project.rootDir}/buildSrc/config/naver-checkstyle-suppressions.xml")
        )
        xml.required.set(false)
        html.required.set(true)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}
