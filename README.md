<div align="center">

![readme-banner.png](/assets/readme-banner.png)

[![Available on Modrinth](https://raw.githubusercontent.com/vLuckyyy/badges/main/avaiable-on-modrinth.svg)](https://modrinth.com/plugin/ndailyrewards)

[![Chat on Discord](https://raw.githubusercontent.com/vLuckyyy/badges/main//chat-with-us-on-discord.svg)](https://discord.gg/p7cxhw7E2M)
[![Read the Docs](https://raw.githubusercontent.com/vLuckyyy/badges/main/read-the-documentation.svg)](https://docs.bx-team.space/documentation/ndailyrewards/about)
[![Available on BStats](https://raw.githubusercontent.com/vLuckyyy/badges/main/available-on-bstats.svg)](https://bstats.org/plugin/bukkit/NDailyRewards/13844)
</div>

# Welcome to NDailyRewards
NDailyRewards is a simple and lightweight plugin that allows you to reward your players for playing on your server every day.

## ℹ️ Information
- NDailyRewards fully supports Minecraft from `1.16` through `1.21`.
- Requires **Java 17 or later** to work properly. For older versions of Java, this may affect the functionality of the plugin.

## ✨ Features
- 📇 MariaDB and SQLite support
- ⚙️ High adjustable and simple configuration
- 🌈 HEX Colors with gradient support
- ⌨️ Various action types for rewards
- 📄 PlaceholderAPI Support
- 📝 Customizable and Translatable Messages
- 😎 Custom model data support
- 🔁 Auto claim rewards
- 🔔 Join notifications and auto-GUI opening
- ...and more!

## 🛠️ Development Builds
Get the latest development builds from our [GitHub Actions](https://github.com/BX-Team/NDailyRewards/actions/workflows/gradle.yml?query=branch%3Amaster).

## 👷 Developer API
To use NDailyRewards API, you first need to add NDailyRewards to your project. To do that follow these steps:

### Add repository:

For Gradle projects use:
```groovy
repositories {
    maven("https://repo.bx-team.space/releases")
}
```

For Maven projects use:
```xml
<repository>
    <id>bx-team-releases</id>
    <url>https://repo.bx-team.space/releases</url>
</repository>
```

### Add dependency:

For Gradle projects use:
```groovy
dependencies {
    compileOnly("space.bxteam.ndailyrewards:VERSION")
}
```

For Maven projects use:
```xml
<dependency>
    <groupId>space.bxteam</groupId>
    <artifactId>ndailyrewards</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

You can check the latest version number [here](https://github.com/BX-Team/NDailyRewards/releases/latest).

## 🏗️ Building
To build NDailyRewards, follow these steps (Make sure you have **JDK 17 or higher**):

```shell
./gradlew shadowJar
```
- The output file will be located at `build/libs`.

## 📚 Contributing
If you want to contribute to NDailyRewards, see [CONTRIBUTING.md](https://github.com/BX-Team/NDailyRewards/blob/master/.github/CONTRIBUTING.md) to find out more.

## 📑 Dependencies & License
For NDailyRewards to work properly, we use the following dependencies:
- [HikariCP](https://github.com/brettwooldridge/HikariCP)
- [MariaDB](https://github.com/mariadb-corporation/mariadb-connector-j)
- [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)
- [Paper API](https://docs.papermc.io/paper/dev/api)
- [bStats](https://bstats.org)
- [mc-publish](https://github.com/Kir-Antipov/mc-publish)

NDailyRewards is licensed under the [GPL-3.0 License](https://github.com/BX-Team/NDailyRewards/blob/master/LICENSE).
