plugins {
    `maven-publish`
}

group = project.group
version = project.version

dependencies {
    compileOnly(libs.paper)
}

tasks {
    java {
        withSourcesJar()
        withJavadocJar()
    }

    javadoc {
        val options = options as StandardJavadocDocletOptions
        options.encoding = Charsets.UTF_8.name()
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
            url = uri("https://repo.bxteam.org/releases/")

            if (version.toString().endsWith("-SNAPSHOT")) {
                url = uri("https://repo.bxteam.org/snapshots/")
            }

            credentials.username = System.getenv("REPO_USERNAME")
            credentials.password = System.getenv("REPO_PASSWORD")
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "ndailyrewards"
            version = version.toString()
            from(components["java"])
        }
    }
}
