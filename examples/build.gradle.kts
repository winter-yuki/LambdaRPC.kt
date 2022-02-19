import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.10"
    application
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        jvmTarget = "11"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":lambdarpc"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
}

val basic = "io.lambdarpc.examples.basic"

tasks.register<JavaExec>("basic.service1") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$basic.service1.MainKt")
}

tasks.register<JavaExec>("basic.service2") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$basic.service2.MainKt")
}

tasks.register<JavaExec>("basic.client") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$basic.client.MainKt")
}

tasks.register<JavaExec>("basic.stress") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$basic.stress.MainKt")
}

val lazy = "io.lambdarpc.examples.lazy"

tasks.register<JavaExec>("lazy.client") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$lazy.client.ClientKt")
}

tasks.register<JavaExec>("example.lazy.service") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$lazy.service.ServiceKt")
}
