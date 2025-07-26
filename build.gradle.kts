import org.bxteam.runserver.ServerType

plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight) apply false
    alias(libs.plugins.run.server)
}

allprojects {
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://jitpack.io/")
        maven("https://repo.bxteam.org/releases")
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
    }
}

dependencies {
    api(project(":core"))
}

tasks {
    shadowJar {
        archiveClassifier = ""
        from(file("LICENSE"))
        minimize()
        manifest {
            attributes["paperweight-mappings-namespace"] = io.papermc.paperweight.util.constants.SPIGOT_NAMESPACE
        }
        dependencies {
            exclude("META-INF/NOTICE")
            exclude("META-INF/maven/**")
            exclude("META-INF/versions/**")
            exclude("META-INF/**.kotlin_module")
        }
    }

    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.release = 17
    }

    processResources {
        from("resources")
    }

    runServer {
        serverType(ServerType.PAPER)
        serverVersion("1.21.8")
        noGui(true)
        acceptMojangEula()

        downloadPlugins {
            modrinth("luckperms", "v5.5.0-bukkit")
            hangar("PlaceholderAPI", "2.11.6")
        }
    }
}
