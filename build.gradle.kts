plugins {
    kotlin("jvm") version "1.6.21" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    group = "io.lambdarpc"
    version = "0.0.1-SNAPSHOT"
}
