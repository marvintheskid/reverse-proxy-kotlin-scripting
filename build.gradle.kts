import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.7.20"
val coroutinesVersion = "1.6.4"

plugins {
    id("java")
    kotlin("jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.marvin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    flatDir {
        dir("deps")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    compileOnly(":standalone-all")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-common", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-jvm", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-jvm-host", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-dependencies", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-scripting-dependencies-maven", version = kotlinVersion)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
}

tasks.withType<ShadowJar> {
    exclude("META-INF/**.txt")
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
}

tasks.withType<JavaCompile> {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<ProcessResources> {
    filteringCharset = Charsets.UTF_8.name()
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events(TestLogEvent.STANDARD_OUT)
    }
}

tasks.named<DefaultTask>("build") {
    dependsOn("shadowJar")
}