<div align="center">

![Banner](/assets/readme-banner.png)
### NDailyRewards
Simple and lightweight plugin that allows you to reward your players for playing on your server every day.

[![Available on Modrinth](https://raw.githubusercontent.com/vLuckyyy/badges/main/avaiable-on-modrinth.svg)](https://modrinth.com/plugin/ndailyrewards)

[![Chat on Discord](https://raw.githubusercontent.com/vLuckyyy/badges/main//chat-with-us-on-discord.svg)](https://discord.gg/qNyybSSPm5)
[![Read the Docs](https://raw.githubusercontent.com/vLuckyyy/badges/main/read-the-documentation.svg)](https://docs.bxteam.org/documentation/ndailyrewards/about)
[![Available on BStats](https://raw.githubusercontent.com/vLuckyyy/badges/main/available-on-bstats.svg)](https://bstats.org/plugin/bukkit/NDailyRewards/13844)
</div>

## About
NDailyRewards is a simple and lightweight plugin that allows you to reward your players for playing on your server every day. It supports MySQL, MariaDB, and SQLite databases, and has a lot of features that you can customize to your liking.

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

## Installation

### Stable Builds
You can download the latest stable builds from our [Modrinth page](https://modrinth.com/plugin/ndailyrewards).

### Development Builds
Get the latest development builds from our [GitHub Actions](https://github.com/BX-Team/NDailyRewards/actions/workflows/gradle.yml?query=branch%3Amaster).

## Developer API
To use NDailyRewards API, you first need to add NDailyRewards to your project. To do that follow these steps:

### Add repository:

For Gradle projects use:
```groovy
repositories {
    maven("https://repo.bxteam.org/releases")
}
```

For Maven projects use:
```xml
<repository>
    <id>bx-team-releases</id>
    <url>https://repo.bxteam.org/releases</url>
</repository>
```

### Add dependency:

For Gradle projects use:
```groovy
dependencies {
    compileOnly("org.bxteam.ndailyrewards:VERSION")
}
```

For Maven projects use:
```xml
<dependency>
    <groupId>org.bxteam</groupId>
    <artifactId>ndailyrewards</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

You can check the latest version number [here](https://github.com/BX-Team/NDailyRewards/releases/latest).

## Building
To build NDailyRewards, follow these steps (Make sure you have **JDK 17 or higher**):

```shell
./gradlew shadowJar
```
- The output file will be located at `build/libs`.

## Contributing
If you want to contribute to NDailyRewards, see [CONTRIBUTING.md](https://github.com/BX-Team/NDailyRewards/blob/master/.github/CONTRIBUTING.md) to find out more.
