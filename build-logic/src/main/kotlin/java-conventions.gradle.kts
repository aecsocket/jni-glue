plugins {
    id("base-conventions")
    id("java-library")
    id("net.kyori.indra")
}

indra {
    javaVersions {
        minimumToolchain(8)
        target(8)
    }
}

repositories {
    mavenCentral()
}
