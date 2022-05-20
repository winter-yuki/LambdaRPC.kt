import com.google.protobuf.gradle.*
import com.google.protobuf.gradle.plugins
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.10"
    id("com.google.protobuf") version "0.8.18"
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("org.jetbrains.dokka") version "1.6.10"
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xexplicit-api=warning")
        jvmTarget = "11"
    }
}

dependencies {
    runtimeOnly("io.grpc:grpc-netty-shaded:1.46.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    api("io.grpc:grpc-protobuf:1.46.0")
    api("com.google.protobuf:protobuf-java-util:3.20.1")
    api("com.google.protobuf:protobuf-kotlin:3.20.1")
    api("io.grpc:grpc-kotlin-stub:1.2.1")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    api("org.slf4j:slf4j-api:1.7.36")
    api("org.slf4j:slf4j-simple:1.7.36")
    api("io.github.microutils:kotlin-logging-jvm:2.1.21")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.20.0")

    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

sourceSets {
    main {
        proto {
            srcDirs += File("$projectDir/src/main/proto")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform {
        excludeTags("slow")
    }
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<Test>("slow") {
    useJUnitPlatform {
        includeTags("slow")
        maxHeapSize = "5000m"
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.39.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.2.0:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false // Activates all, even unstable rules
    config = files("$projectDir/config/detekt.yml")
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}
