<div align="center">

<h1>
<picture>
  <source media="(prefers-color-scheme: dark)" srcset=".github/branding/logo-dark.svg">
  <img src=".github/branding/logo-light.svg" height="36" align="top" alt="">
</picture>
NDailyRewards
</h1>

NDailyRewards rewards players for coming back to your server every day. Each day of the streak has its own reward and its own slot in a configurable menu, a missed day resets the streak, and every reward is a list of actions: console and player commands, messages, titles, sounds and chance-based drops.

[![Available on Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/ndailyrewards)
[![Available on Hangar](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/hangar_vector.svg)](https://hangar.papermc.io/BX-Team/NDailyRewards)

[![Chat on Discord](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-plural_vector.svg)](https://discord.gg/qNyybSSPm5)
[![Read the Docs](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/generic_vector.svg)](https://bxteam.org/docs/ndailyrewards)
[![Available on BStats](https://raw.githubusercontent.com/NONPLAYT/badges/refs/heads/master/available-on-bstats.svg)](https://bstats.org/plugin/bukkit/NDailyRewards/13844)
</div>

## ⚙️ Features

- **📅 Daily streaks** — a reward per day, a cooldown in hours or an unlock at midnight in the timezone you choose, and a streak that resets when a player stays away too long.
- **⌨️ Reward actions** — `[console]`, `[player]`, `[message]`, `[actionbar]`, `[title]`, `[sound]`, `[permission]`, `[luck]` and `[close]`, with PlaceholderAPI placeholders in every line.
- **🧰 Configurable menu** — slot, item and lore for every state of a day (available, claimed, next, locked), a live countdown, filler and custom buttons. Items support custom model data, item models (1.21.4+) and custom heads.
- **🔔 Join events** — auto-claim after a delay, open the menu or remind the player when a reward is waiting.
- **🌈 MiniMessage** — every message supports MiniMessage tags and legacy `&` color codes, side by side.
- **📝 Self-updating configs** — new options appear in your `config.yml` and `lang.yml` after an update, and invalid values are rejected with a clear message instead of breaking a reload.
- **📇 SQLite and MariaDB** — local storage out of the box, a shared database for networks.
- **🧵 Paper and Folia** — one jar for Paper 1.16 and newer, including Folia.

*...and more!*

## 📥 Downloading & Installing

Download the latest release from [Modrinth](https://modrinth.com/plugin/ndailyrewards) or [Hangar](https://hangar.papermc.io/BX-Team/NDailyRewards) and put it into your server's `plugins` folder. The server needs Java 17 or newer. On the first start NDailyRewards downloads the libraries it needs into `plugins/NDailyRewards/libs` and writes its `config.yml` and `lang.yml`.

Every option is explained in the [documentation](https://bxteam.org/docs/ndailyrewards).

## 🧪 API

NDailyRewards fires events when a player claims a reward, gets one automatically and receives a reminder. Add the repository and the API to your project:

```kotlin
repositories {
    maven("https://repo.bxteam.org/releases")
}

dependencies {
    compileOnly("org.bxteam:ndailyrewards:3.4.0")
}
```

```xml
<repositories>
    <repository>
        <id>bx-team-releases</id>
        <url>https://repo.bxteam.org/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.bxteam</groupId>
        <artifactId>ndailyrewards</artifactId>
        <version>3.4.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Then listen to them like any other Bukkit event:

```java
@EventHandler
public void onClaim(PlayerClaimRewardEvent event) {
    event.getPlayer().sendMessage("You claimed the reward of day " + event.getDay());
}
```

## 🔨 Build from source

NDailyRewards needs JDK 17 or newer to build.

```bash
git clone https://github.com/BX-Team/NDailyRewards.git
cd NDailyRewards
./gradlew build             # the plugin jar lands in build/libs
./gradlew runServer         # start a Paper test server with the plugin, LuckPerms and PlaceholderAPI
./gradlew runFoliaServer    # the same on Folia
./gradlew runLegacyServer   # Paper 1.16.5, the oldest supported version
```

## 🤝 Contributing

Read the [contributing guidelines](https://github.com/BX-Team/.github/blob/master/CONTRIBUTING.md) first. Every pull request runs `./gradlew build`, and the build is the only automated check, so test your change on a real server before opening one.

## ⚖️ License

This project is licensed under the GPL-3.0 License — see the [LICENSE](LICENSE) file for details.

## 💛 Credits

- [BX-Team/Quark](https://github.com/BX-Team/Quark): Runtime libraries, configs, the scheduler and update checks.
- [Revxrsal/Lamp](https://github.com/Revxrsal/Lamp): The command framework.
- [j256/ormlite-core](https://github.com/j256/ormlite-core) and [brettwooldridge/HikariCP](https://github.com/brettwooldridge/HikariCP): Storage and connection pooling.
- [PaperMC/adventure](https://github.com/PaperMC/adventure): Text components and MiniMessage.
- [deanveloper/SkullCreator](https://github.com/deanveloper/SkullCreator): The basis of the custom head support.
