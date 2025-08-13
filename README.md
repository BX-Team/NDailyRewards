<div align="center">

### NDailyRewards
Simple and lightweight plugin that allows you to reward your players for playing on your server every day.

[![Available on Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/ndailyrewards)
[![Available on Hangar](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/hangar_vector.svg)](https://hangar.papermc.io/BX-Team/NDailyRewards)

[![Chat on Discord](https://raw.githubusercontent.com/vLuckyyy/badges/main//chat-with-us-on-discord.svg)](https://discord.gg/qNyybSSPm5)
[![Read the Docs](https://raw.githubusercontent.com/vLuckyyy/badges/main/read-the-documentation.svg)](https://bxteam.org/docs/ndailyrewards)
[![Available on BStats](https://raw.githubusercontent.com/vLuckyyy/badges/main/available-on-bstats.svg)](https://bstats.org/plugin/bukkit/NDailyRewards/13844)
</div>

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

## ⚒️ Developer API
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

## 📦 Building
To build NDailyRewards, follow these steps (Make sure you have **JDK 17 or higher**):

```shell
./gradlew build
```
- The output file will be located at `build/libs`.

## 🧾 License ![Static Badge](https://img.shields.io/badge/license-GPL_3.0-lightgreen)

NDailyRewards is licensed under the GNU General Public License v3.0. You can find the license [here](LICENSE).
