plugins {
    `java-library`
    `maven-publish`
    java
}

repositories {
    mavenLocal()
    maven("https://repo.citizensnpcs.co/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://libraries.minecraft.net")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    compileOnly("net.citizensnpcs:citizensapi:2.0.31-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:3.16.29")
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.11.3")
    compileOnly("org.xerial:sqlite-jdbc:3.42.0.0")
    compileOnly("me.clip:placeholderapi:2.11.3")
}

group = project.group
version = project.version
description = "Reward players for playing on your server"
java.sourceCompatibility = JavaVersion.VERSION_16

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
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
