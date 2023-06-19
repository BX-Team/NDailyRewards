plugins {
    `java-library`
    `maven-publish`
    java
}

val paperAPIVersion = project.properties["mcVersion"]!!

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.citizensnpcs.co/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${paperAPIVersion}") // Paper API

    // Plugin dependencies
    compileOnly("net.citizensnpcs:citizensapi:2.0.32-SNAPSHOT")
    compileOnly("com.mojang:authlib:3.16.29")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.11.3")
    compileOnly("org.xerial:sqlite-jdbc:3.42.0.0")
    compileOnly("me.clip:placeholderapi:2.11.3")
}

group = project.group
version = project.version
description = "Reward players for playing on your server"
java.sourceCompatibility = JavaVersion.VERSION_17

java {
    withSourcesJar()
    withJavadocJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    inputs.property("version", project.version)
    outputs.upToDateWhen { false }
    filesMatching("**/plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "ndailyrewards"
            groupId = project.group.toString()
            version = project.version.toString()
            from(components["java"])
            pom {
                packaging = "jar"
                name.set("NDailyRewards")
                url.set("https://github.com/turbomates/super-project")
                description.set("Some description")

                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                    }
                }

                scm {
                    connection.set("scm:https://github.com/BX-Team/NDailyRewards.git")
                    developerConnection.set("scm:git@github.com:BX-Team/NDailyRewards.git")
                    url.set("https://github.com/BX-Team/NDailyRewards")
                }

                developers {
                    developer {
                        id.set("NONPLAY")
                        name.set("Artem NONPLAY")
                        email.set("admin@bxteam.gq")
                        timezone.set("UTC+3")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}
