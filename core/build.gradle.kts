import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    alias(libs.plugins.plugin.yml.bukkit)
}

dependencies {
    api(project(":api"))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.paper)
    compileOnly(libs.placeholderapi)

    implementation(libs.paperlib)
    implementation(libs.hikari)
    implementation(libs.mariadb)
    implementation(libs.bundles.ormlite)
    implementation(libs.bundles.commons)
    implementation(libs.bstats)
    implementation(libs.bundles.guice)
    implementation(libs.litecommands)
}

tasks {
    java {
        disableAutoTargetJvm()
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    compileJava {
        options.release.set(17)
    }
}

bukkit {
    name = "NDailyRewards"
    description = "Reward players for playing on your server"
    website = "https://github.com/BX-Team/NDailyRewards"
    authors = listOf("NONPLAYT", "wiyba")

    main = "org.bxteam.ndailyrewards.NDailyRewards"
    apiVersion = "1.16"

    permissions {
        register("ndailyrewards.claim") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("ndailyrewards.help") {
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
        register("ndailyrewards.reload") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("ndailyrewards.setday") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("ndailyrewards.version") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }
}