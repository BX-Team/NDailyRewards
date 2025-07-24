plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.8"
}

group = project.property("group") as String
version = project.property("version") as String
description = project.property("description") as String

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://repo.bxteam.org/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${project.property("paper_api")}")

    implementation("com.zaxxer:HikariCP:${project.property("hikari_cp")}")
    implementation("org.mariadb.jdbc:mariadb-java-client:${project.property("mariadb")}")
    implementation("org.bxteam:commons:${project.property("commons")}")
    compileOnly("me.clip:placeholderapi:${project.property("placeholder_api")}")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    jar {
        enabled = false
    }

    build {
        dependsOn("shadowJar")
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("${project.name}-${project.version}.jar")
        from(file("LICENSE"))

        dependencies {
            include(dependency("com.zaxxer:HikariCP:${project.property("hikari_cp")}"))
            include(dependency("org.mariadb.jdbc:mariadb-java-client:${project.property("mariadb")}"))
            include(dependency("org.bxteam:commons:${project.property("commons")}"))

            exclude("META-INF/NOTICE")
            exclude("META-INF/maven/**")
            exclude("META-INF/versions/**")
            exclude("META-INF/**.kotlin_module")
        }
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
