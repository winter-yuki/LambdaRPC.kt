plugins {
    kotlin("jvm") version "1.6.10" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    group = "io.lambdarpc"
    version = "0.0.1"
}
