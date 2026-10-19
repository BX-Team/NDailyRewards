import org.bxteam.quark.gradle.QuarkModule
import org.bxteam.quark.gradle.ServerPlatform
import org.bxteam.quark.gradle.ServerType

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.quark)
}

dependencies {
    api(project(":api"))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.paper)
    compileOnly(libs.placeholderapi)

    quark(libs.hikari) {
        exclude("org.slf4j", "slf4j-api")
    }
    quark(libs.mariadb)
    quark(libs.bundles.ormlite)
    quark(libs.bundles.guice) {
        exclude("com.google.guava", "guava")
    }
    quark(libs.bundles.lamp)
    quark(libs.bundles.adventure) {
        exclude("com.google.code.gson", "gson")
    }

    implementation(libs.bstats)
}

quark {
    platform = ServerPlatform.PAPER
    modules(
        QuarkModule.DEPENDENCY,
        QuarkModule.CONFIG,
        QuarkModule.CONFIG_VALIDATOR,
        QuarkModule.LOGGER,
        QuarkModule.UPDATE
    )

    quarkPackage = "org.bxteam.ndailyrewards.libs.quark"
    librariesPackage = "org.bxteam.ndailyrewards.libs"
    relocate("net.kyori", "org.bxteam.ndailyrewards.libs.kyori")

    pluginYml {
        main = "org.bxteam.ndailyrewards.NDailyRewards"
        apiVersion = "1.16"
        foliaSupported = true
        authors("NONPLAYT", "wiyba")
        website = "https://github.com/BX-Team/NDailyRewards"

        permissions {
            register("ndailyrewards.claim") { default = TRUE }
            register("ndailyrewards.help") { default = TRUE }
            register("ndailyrewards.reload") { default = OP }
            register("ndailyrewards.setday") { default = OP }
            register("ndailyrewards.version") { default = OP }
            register("ndailyrewards.update-notify") { default = OP }
        }
    }

    devServer {
        version = "26.3"
        perVersionFolder = true
        acceptEula()

        plugins {
            modrinth("luckperms", "v5.5.17-bukkit")
            hangar("PlaceholderAPI", "2.12.2")
        }
    }

    devServers {
        register("folia") {
            type = ServerType.FOLIA
            version = "26.2"
            acceptEula()
        }
        register("legacy") {
            version = "1.16.5"
            javaVersion = 17
            acceptEula()
        }
    }
}

tasks {
    java {
        disableAutoTargetJvm()
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    compileJava {
        options.release.set(17)
        options.compilerArgs.add("-parameters")
    }

    shadowJar {
        archiveBaseName = rootProject.name
        archiveClassifier = ""
        destinationDirectory = rootProject.layout.buildDirectory.dir("libs")

        from(rootProject.file("LICENSE"))
        relocate("org.bstats", "org.bxteam.ndailyrewards.dependencies.bstats")
        minimize {
            exclude(dependency("org.bxteam.quark:.*:.*"))
        }

        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.md")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")
        exclude("META-INF/**.kotlin_module")
        exclude("deprecated.properties")
        exclude("driver.properties")
        exclude("mariadb.properties")
    }

    build {
        dependsOn(shadowJar)
    }

    named<JavaExec>("runLegacyServer") {
        jvmArgs("-DPaper.IgnoreJavaVersion=true")
    }
}
