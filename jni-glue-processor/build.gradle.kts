plugins {
    id("publishing-conventions")
    kotlin("jvm") version "1.8.0"
}

kotlin {
    jvmToolchain(indra.javaVersions().target().get())
}

dependencies {
    implementation(projects.jniGlueAnnotations)
}
