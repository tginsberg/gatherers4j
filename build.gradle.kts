import java.io.IOException
import java.net.URI

plugins {
    id("com.adarshr.test-logger") version "4.0.0"
    id("jacoco")
    id("java-library")
    id("maven-publish")
    id("signing")
}

description = "An extra set of helpful Stream Gatherers for Java"
group = "com.ginsberg"
version = file("VERSION.txt").readLines().first()

@Suppress("PropertyName")
val ENABLE_PREVIEW = "--enable-preview"
val gitBranch = gitBranch()
val gatherers4jVersion = if(gitBranch == "main" || gitBranch.startsWith("release/")) version.toString()
                         else "${gitBranch.substringAfterLast("/")}-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") {
        because("Starting in Gradle 9.0, this needs to be an explicitly declared dependency")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")

    testImplementation("org.assertj:assertj-core:3.26.0") {
        because("These assertions are clearer than JUnit+Hamcrest")
    }
}

publishing {
    publications {
        create<MavenPublication>("gatherers4j") {
            from(components["java"])
            pom {
                name = "Gatherers4J"
                description = project.description
                version = gatherers4jVersion
                url = "https://github.com/tginsberg/gatherers4j"
                organization {
                    name = "com.ginsberg"
                    url = "https://github.com/tginsberg"
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/tginsberg/gatherers4j/issues"
                }
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "tginsberg"
                        name = "Todd Ginsberg"
                        email = "todd@ginsberg.com"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/tginsberg/gatherers4j.git"
                    developerConnection = "scm:git:https://github.com/tginsberg/gatherers4j.git"
                    url = "https://github.com/tginsberg/gatherers4j"
                }
            }
        }
    }
    repositories {
        maven {
            url = if(version.toString().endsWith("-SNAPSHOT")) URI("https://oss.sonatype.org/content/repositories/snapshots/")
                  else URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_TOKEN")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("SONATYPE_SIGNING_KEY"), System.getenv("SONATYPE_SIGNING_PASSPHRASE"))
    sign(publishing.publications["gatherers4j"])
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.add(ENABLE_PREVIEW)
    }
    jacocoTestReport {
        dependsOn(test)
    }
    jar {
        manifest {
            attributes(
                "Implementation-Title" to "Gatherers4J",
                "Implementation-Version" to archiveVersion
            )
        }
    }
    javadoc {
        (options as CoreJavadocOptions).apply {
            addStringOption("source", rootProject.java.toolchain.languageVersion.get().toString())
            addBooleanOption("-enable-preview", true)
            addStringOption("Xdoclint:none", "-quiet") // TODO: Remove this when we've documented things
        }
    }
    this.register("printVersion").configure {
        dependsOn("publish")
        doLast {
            println("Project Version: $version")
            println("Publish Version: $gatherers4jVersion")
        }
    }
    test {
        finalizedBy(jacocoTestReport)
        jvmArgs(ENABLE_PREVIEW)
        useJUnitPlatform()
    }
}

fun gitBranch(): String =
    ProcessBuilder("git rev-parse --abbrev-ref HEAD".split(" "))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
        .run {
            val error = errorStream.bufferedReader().readText()
            if (error.isNotEmpty()) throw IOException(error)
            inputStream.bufferedReader().readText().trim()
        }
