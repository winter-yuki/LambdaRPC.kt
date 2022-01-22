import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.protobuf") version "0.8.18"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "io.lambdarpc"
version = "0.0.1"

ext["grpcVersion"] = "1.39.0" // need to wait for grpc kotlin to move past this
ext["grpcKotlinVersion"] = "1.2.0" // CURRENT_GRPC_KOTLIN_VERSION
ext["protobufVersion"] = "3.19.1"
ext["coroutinesVersion"] = "1.5.2"

repositories {
    mavenCentral()
    google()
}

dependencies {
    runtimeOnly("io.grpc:grpc-netty-shaded:1.43.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.ext["coroutinesVersion"]}")
    api("io.grpc:grpc-protobuf:${rootProject.ext["grpcVersion"]}")
    api("com.google.protobuf:protobuf-java-util:${rootProject.ext["protobufVersion"]}")
    api("com.google.protobuf:protobuf-kotlin:${rootProject.ext["protobufVersion"]}")
    api("io.grpc:grpc-kotlin-stub:${rootProject.ext["grpcKotlinVersion"]}")
    implementation("org.slf4j:slf4j-simple:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.register<JavaExec>("example.basic.service1") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.lambdarpc.examples.basic.service1.MainKt")
}

tasks.register<JavaExec>("example.basic.service2") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.lambdarpc.examples.basic.service2.MainKt")
}

tasks.register<JavaExec>("example.basic.client") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.lambdarpc.examples.basic.client.MainKt")
}

tasks.register<JavaExec>("example.basic.stress") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("io.lambdarpc.examples.basic.stress.MainKt")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${rootProject.ext["protobufVersion"]}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${rootProject.ext["grpcVersion"]}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${rootProject.ext["grpcKotlinVersion"]}:jdk7@jar"
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
