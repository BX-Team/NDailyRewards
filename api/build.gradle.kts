plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly(libs.paper)
}

tasks {
    javadoc {
        val options = options as StandardJavadocDocletOptions
        options.encoding = Charsets.UTF_8.name()
        options.overview = "src/main/javadoc/overview.html"
        options.use()
        options.tags("apiNote:a:API Note:")
    }

    compileJava {
        options.release.set(17)
    }
}

publishing {
    repositories {
        maven {
            name = "ndailyrewards"
            url = uri(
                if (project.version.toString().endsWith("-SNAPSHOT")) {
                    "https://repo.bxteam.org/snapshots"
                } else {
                    "https://repo.bxteam.org/releases"
                }
            )
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.bxteam"
            artifactId = "ndailyrewards"
            version = project.version.toString()
            from(components["java"])
        }
    }
}
