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

val lazy = "io.lambdarpc.examples.lazy"

tasks.register<JavaExec>("lazy.service") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$lazy.service.ServiceKt")
}

tasks.register<JavaExec>("lazy.client") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$lazy.client.ClientKt")
}

val ml = "io.lambdarpc.examples.ml"

tasks.register<JavaExec>("ml.dataservice") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$ml.dataservice.ServiceKt")
}

tasks.register<JavaExec>("ml.mlservice") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$ml.mlservice.ServiceKt")
}

tasks.register<JavaExec>("ml.client") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("$ml.client.ClientKt")
}
