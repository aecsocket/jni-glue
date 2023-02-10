plugins {
    id("publishing-conventions")
    kotlin("jvm")
}

kotlin {
    jvmToolchain(indra.javaVersions().target().get())
}

dependencies {
    implementation(projects.jniGlueAnnotations)
}
