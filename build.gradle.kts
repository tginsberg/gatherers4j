/*
 * Copyright 2024-2026 Todd Ginsberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import java.io.IOException

plugins {
    id("com.adarshr.test-logger") version "4.0.0"
    id("info.solidsoft.pitest") version "1.19.0"
    id("jacoco")
    id("java-library")
    id("maven-publish")
    id("me.champeau.jmh") version "0.7.3"
    id("net.ltgt.errorprone") version "5.1.1"
}

description = "An extra set of helpful Stream Gatherers for Java"
group = "com.ginsberg"
version = file("VERSION.txt").readLines().first()

val javaVersion = findProperty("javaVersion")?.toString()?.toInt() ?: 25
val jUnitVersion = "6.1.3"

val gitBranch = gitBranch()
val gatherers4jVersion = if (gitBranch == "main" || gitBranch.startsWith("release/")) version.toString()
else "${gitBranch.substringAfterLast("/")}-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

// Compile-only call sites that check the facade's nullness annotations the way a NullAway user would see them.
// Not part of the test source set, because tests run without NullAway. Keep "test" out of the name for the same reason.
val nullnessUsage: SourceSet = sourceSets.create("nullnessUsage") {
    compileClasspath += sourceSets.main.get().output + configurations.compileClasspath.get()
}

dependencies {
    api("org.jspecify:jspecify:1.0.1") {
        because("Annotating with JSpecify makes static analysis more accurate")
    }

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$jUnitVersion") {
        because("Starting in Gradle 9.0, this needs to be an explicitly declared dependency")
    }

    testImplementation("org.apache.commons:commons-statistics-inference:1.3") {
        because("We use this to measure if random sampling methods actually work")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:$jUnitVersion") {
        because("We need this to run tests")
    }
    testImplementation("org.assertj:assertj-core:3.27.7") {
        because("These assertions are clearer than JUnit+Hamcrest")
    }

    errorprone("com.google.errorprone:error_prone_core:2.50.0") {
        because("This helps us eliminate bugs during the development cycle")
    }
    errorprone("com.uber.nullaway:nullaway:0.14.1") {
        because("It helps us find nullability issues, along with JSpecify")
    }
}

pitest {
    coverageThreshold = 80
    features = listOf("+auto_threads")
    historyInputLocation = layout.buildDirectory.file("pitHistory").get().asFile
    historyOutputLocation = layout.buildDirectory.file("pitHistory").get().asFile
    junit5PluginVersion = "1.2.3"
    mutationThreshold = 75
    mutators = listOf("DEFAULTS")
    outputFormats = listOf("HTML", "XML")
    targetClasses = listOf("com.ginsberg.gatherers4j.*")
    targetTests = listOf("com.ginsberg.gatherers4j.*")
    threads = Runtime.getRuntime().availableProcessors()
    timestampedReports = false
    verbose = false
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

tasks {
    withType<JavaCompile> {
        options.errorprone {
            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", "com.ginsberg.gatherers4j")
            option("NullAway:JSpecifyMode", "true")
        }
        if (name.lowercase().contains("test")) {
            options.errorprone {
                disable("NullAway")
            }
        }
    }

    check {
        dependsOn(nullnessUsage.classesTaskName)
    }

    jacoco {
        toolVersion = "0.8.15"
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
            // Every check except missing comments, which the public internals (State classes, overrides) don't have
            addStringOption("Xdoclint:all,-missing", "-quiet")
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
