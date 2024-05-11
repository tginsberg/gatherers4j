plugins {
    id("com.adarshr.test-logger") version "4.0.0"
    id("jacoco")
    id("java-library")
    id("maven-publish")
}

description = "An extra set of helpful Stream Gatherers for Java"
group = "com.ginsberg"
version = "0.0.1-SNAPSHOT"

@Suppress("PropertyName")
val ENABLE_PREVIEW = "--enable-preview"

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

    testImplementation("org.assertj:assertj-core:3.25.1") {
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
    test {
        finalizedBy(jacocoTestReport)
        jvmArgs(ENABLE_PREVIEW)
        useJUnitPlatform()
    }
}
