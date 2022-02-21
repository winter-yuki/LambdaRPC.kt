import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
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
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        jvmTarget = "11"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    runtimeOnly("io.grpc:grpc-netty-shaded:1.44.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    api("io.grpc:grpc-protobuf:1.44.0")
    api("com.google.protobuf:protobuf-java-util:3.19.4")
    api("com.google.protobuf:protobuf-kotlin:3.19.4")
    api("io.grpc:grpc-kotlin-stub:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.slf4j:slf4j-simple:1.7.35")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.19.0")

    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
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
