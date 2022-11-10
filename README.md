<h1 align="center">
  NDailyRewards
</h1>

<p align="center">
    Reward players for playing on your server.
</p>

<div align="center">
    <a href="https://discord.gg/p7cxhw7E2M"><img src="https://img.shields.io/discord/931595732752953375?logo=discord&style=for-the-badge" alt="Discord"/></a>
    <br>
    <img src="https://img.shields.io/github/last-commit/BX-Team/NDailyRewards?style=for-the-badge" alt="GitHub last commit"/>
    <img src="https://img.shields.io/github/commit-activity/m/BX-Team/NDailyRewards?style=for-the-badge" alt="GitHub commit activity"/>
    <br>
    <img src="https://img.shields.io/github/languages/code-size/BX-Team/NDailyRewards?style=for-the-badge" alt="GitHub code size in bytes"/>
    <img src="https://img.shields.io/github/workflow/status/NONPLAYT/NDailyRewards/Java%20CI%20with%20Maven?style=for-the-badge" alt="GitHub workflow status"/>
    <img src="https://img.shields.io/maven-central/v/gq.bxteam/ndailyrewards?style=for-the-badge" href="https://s01.oss.sonatype.org/content/repositories/staging/gq/bxteam/ndailyrewards/"/>
</div>

### ❓ About
This plugin gives you the ability to give players daily rewards for playing on your server!
You can change the amount of days in a row from 1 to 54!

**To run plugin, you need `Java 8+`, `Spigot 1.13+ (or Purpur, Paper and etc.)`**

### 🤖 Featrues
- MySQL and SQLite support
- Completely automated
- High performance!
- Unlimited amount of rewards
- Set clickable NPC's **(Requires Citizens plugin)**
- Fully customizable!
- Unlock rewards after the midnight (Optional)
- Define the time between the rewards!
- Execute commands and send messages in rewards!
- GUI Fully customizable
- Custom model data support!
- You can change amount of days in a row to reward
- Auto opens on join (toggleable)
- Autosaves player data every X mins
- Dynamic GUI

### ⌨ Commands / Permissions

**Command**: /ndailyrewards - Opens rewards GUI

**Permission**: ndailyrewards.user

**Command**: /ndailyrewards help - List of commands

**Permission**: ndailyrewards.user

**Command**: /ndailyrewards reload - Reload configuration

**Permission**: ndailyrewards.admin


### 📡 Metrics
View plugin metrics at [bStats.org](https://bstats.org/plugin/bukkit/NDailyRewards/13844)

![bstats](https://bstats.org/signatures/bukkit/NDailyRewards.svg)


### 🔨 Builds
You can download all plugin builds from our Jenkins server [here](https://ci.bxteam.gq/job/NDailyRewards/).

### 👩‍💻 For developers
If you want to download and use NDailyRewards in your project, get it on maven and import to `pom.xml` like this:
```xml
<dependency>
  <groupId>gq.bxteam</groupId>
  <artifactId>ndailyrewards</artifactId>
  <version>PLUGIN_VERSION</version>
</dependency>
```
**NOTE: If you will ask where is repo url, I say it is already in maven central, so you are ready to use dependency**
