plugins {
    id("jacoco")
    id("java-library")
    id("com.adarshr.test-logger") version "4.0.0"
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

tasks.withType<JavaCompile> {
    options.compilerArgs.add(ENABLE_PREVIEW)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    jvmArgs(ENABLE_PREVIEW)
    useJUnitPlatform()
}