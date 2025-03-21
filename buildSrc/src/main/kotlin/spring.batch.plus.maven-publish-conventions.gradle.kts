plugins {
    // to publish to maven repository
    // https://docs.gradle.org/current/userguide/publishing_maven.html
    `maven-publish`

    // to make signature to artifact
    // https://docs.gradle.org/current/userguide/signing_plugin.html
    signing
}


/* maven-publish */

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set("Add useful features to spring batch")
                url.set("https://github.com/naver/spring-batch-plus")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("acktsap")
                        name.set("Taeik Lim")
                        email.set("sibera21@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/naver/spring-batch-plus.git")
                    developerConnection.set("scm:git:ssh://github.com/naver/spring-batch-plus.git")
                    url.set("https://github.com/naver/spring-batch-plus")
                }
            }
        }
    }

    repositories {
        maven {
            credentials {
                // in '.envrc'
                // export MAVEN_USER=xxx
                // export MAVEN_PASSWORD=xxx
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_PASSWORD")
            }

            url = uri(
                // in '.envrc'
                // export REPO_URL=https://some.url
                // export REPO_SNAPSHOT_URL=https://some.url
                if (!version.toString().endsWith("SNAPSHOT")) {
                    System.getenv("REPO_URL") ?: "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                } else {
                    System.getenv("REPO_SNAPSHOT_URL") ?: "https://oss.sonatype.org/content/repositories/snapshots/"
                },
            )
        }
    }
}

tasks.register("install") {
    dependsOn("publishToMavenLocal")
}

tasks.withType<PublishToMavenRepository> {
    doFirst {
        println("publishing to ${repository.url}")
    }
}


/* signing */

signing {
    // enable signing only when publishing to maven central
    // return true to enable for all
    setRequired {
        gradle.taskGraph.allTasks.any { it is PublishToMavenRepository }
    }

    // in '.envrc'
    // export SIGNING_KEY_ID=xxx
    // export SIGNING_KEY=xxx
    // export SIGNING_PASSWORD=xxx
    // note that we don't use official one
    // (ORG_GRADLE_PROJECT_signingKeyId, ORG_GRADLE_PROJECT_signingKey, ORG_GRADLE_PROJECT_signingPassword)
    // since the github secret doesn't support lower case environment variable
    val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
    val signingKey: String? = System.getenv("SIGNING_KEY")
    val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications["maven"])
}

