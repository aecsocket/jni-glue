plugins {
    id("base-conventions")
    id("net.kyori.indra.publishing")
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    if (signingKey != null) {
        println("Signing with in-memory key from environment")
        useInMemoryPgpKeys(signingKey, signingPassword)
    } else {
        // fall back to gradle properties
        println("Signing with Gradle property keys")
    }
}

indra {
    github("aecsocket", "jni-glue")
    mitLicense()

    configurePublications {
        pom {
            developers {
                developer {
                    name.set("aecsocket")
                    email.set("aecsocket@tutanota.com")
                    url.set("https://github.com/aecsocket")
                }
            }
        }
    }
}
