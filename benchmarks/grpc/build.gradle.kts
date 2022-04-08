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
    implementation(project(":lambdarpc-core"))
}
