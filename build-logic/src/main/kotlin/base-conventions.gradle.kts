plugins {
    id("java-library")
    id("net.kyori.indra")
    id("net.kyori.indra.git")
}

group = rootProject.group
version = rootProject.version
description = rootProject.description

indra {
    javaVersions {
        minimumToolchain(8)
        target(8)
    }
}

repositories {
    mavenCentral()
}
