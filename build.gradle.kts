import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import java.io.IOException

plugins {
    id("com.adarshr.test-logger") version "4.0.0"
    id("jacoco")
    id("java-library")
    id("org.jreleaser") version "1.20.0"
    id("maven-publish")
    id("net.ltgt.errorprone") version "4.3.0"
    id("signing")
}

description = "An extra set of helpful Stream Gatherers for Java"
group = "com.ginsberg"
version = file("VERSION.txt").readLines().first()

val gitBranch = gitBranch()
val gatherers4jVersion = if (gitBranch == "main" || gitBranch.startsWith("release/")) version.toString()
else "${gitBranch.substringAfterLast("/")}-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.jspecify:jspecify:1.0.0") {
        because("Annotating with JSpecify makes static analysis more accurate")
    }

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4") {
        because("Starting in Gradle 9.0, this needs to be an explicitly declared dependency")
    }

    testImplementation("org.apache.commons:commons-statistics-inference:1.1") {
        because("We use this to measure if random sampling methods actually work")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4") {
        because("We need this to run tests")
    }
    testImplementation("org.assertj:assertj-core:3.27.4") {
        because("These assertions are clearer than JUnit+Hamcrest")
    }

    errorprone("com.google.errorprone:error_prone_core:2.41.0") {
        because("This helps us eliminate bugs during the development cycle")
    }
    errorprone("com.uber.nullaway:nullaway:0.12.9") {
        because("It helps us find nullability issues, along with JSpecify")
    }
}

jreleaser {
    project {
        name.set("gatherers4j")
        authors.add("Todd Ginsberg")
        license.set("Apache-2.0")

        links {
            homepage.set("https://github.com/tginsberg/gatherers4j")
        }
    }

    signing {
        active.set(org.jreleaser.model.Active.NEVER)
        armored.set(true)
    }

    deploy {
        maven {
            mavenCentral {
                create("release-deploy") {
                    active.set(org.jreleaser.model.Active.RELEASE)
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                    sign = false
                    applyMavenCentralRules = true
                }
            }
            nexus2 {
                create("snapshot-deploy") {
                    active.set(org.jreleaser.model.Active.SNAPSHOT)
                    snapshotUrl = "https://central.sonatype.com/repository/maven-snapshots"
                    url = "https://central.sonatype.com/repository/maven-snapshots"
                    sign = false
                    applyMavenCentralRules = true
                    snapshotSupported = true
                    closeRepository = false
                    releaseRepository = false
                    stagingRepository("build/staging-deploy")
                }
            }
        }

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
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
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
            url = uri(layout.buildDirectory.dir("staging-deploy").get().asFile)
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("SONATYPE_SIGNING_KEY"),
        System.getenv("SONATYPE_SIGNING_PASSPHRASE")
    )
    sign(publishing.publications["gatherers4j"])
}

tasks {
    withType<JavaCompile> {
        options.errorprone {
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", "com.ginsberg.gatherers4j")
        }
        if (name.lowercase().contains("test")) {
            options.errorprone {
                disable("NullAway")
            }
        }
    }

    jacoco {
        toolVersion = "0.8.13"
    }
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required = true
        }
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
    publish {
        doLast {
            println("Project Version: $version")
            println("Publish Version: $gatherers4jVersion")
        }
    }
    test {
        finalizedBy(jacocoTestReport)
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
