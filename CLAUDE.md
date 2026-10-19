# NDailyRewards

A Paper plugin that gives players a reward for each consecutive day they log in.
Java on Gradle (Kotlin DSL), three modules, and everything is wired with Guice —
there is no static `getInstance()` anywhere, so a new collaborator is a
constructor parameter and a binding, not a singleton lookup.

## Architecture

`NDailyRewards#onEnable` builds one `Injector` from three modules and then pulls
the managers out of it in a fixed order — database open, integrations, messages,
rewards, menus, listeners, commands. `onDisable` tears the same things down in
reverse. That ordering is load-bearing: `RewardManager`'s constructor reads the
config, so the database has to be open before anything asks the injector for it.

`MenuManager` and `RewardManager` refer to each other, which is why
`RewardManager` takes a `Provider<MenuManager>` rather than the instance — an
eager injection there is a Guice circular-dependency error at boot.

| Module / package | Responsibility |
| ---------------- | -------------- |
| root project | Aggregator only, no code. |
| `api/` | The public event classes, and the only thing published to Maven. Anything here is someone else's compile dependency. |
| `core/` | The plugin itself. Its `shadowJar` writes `build/libs/NDailyRewards-<version>.jar` in the root; the Quark Gradle plugin and the dev servers live here. Everything below is inside it. |
| `NDailyRewardsModule` | Bindings for the Bukkit objects, the Quark scheduler and logger, both configs and the update checker. |
| `database/` | Picks SQLite or MariaDB from `database.type` and binds the ORMLite repository behind `RewardRepository`. |
| `manager/reward/` | The domain: what a reward is, who has claimed what, when the next one unlocks, and the actions a claim runs. |
| `manager/menu/` | The claim GUI. |
| `manager/CommandManager` | Registers the Lamp commands; each command class is itself a Guice instance. |
| `messaging/MessageService` | Renders every outgoing message. |
| `configuration/` | `PluginConfig` (`config.yml`) and `Messages` (`lang.yml`), Quark config classes. |
| `integration/` | Optional soft-depend hooks, currently PlaceholderAPI. |

Quark 2 (`org.bxteam.quark`, our own framework) supplies the library loader, the
configs, the scheduler (`PaperScheduler`), the logger (`QuarkLogger` behind
`DebugSwitch`) and the update checker. Prefer what it already has over a local
reimplementation. Quark is relocated to `org.bxteam.ndailyrewards.libs.quark`;
`quarkPackage` is set explicitly because the default, `<group>.libs.quark`, would be
shared with every other `org.bxteam` plugin.

### Decisions that are settled

- **Runtime dependency loading, not a fat jar.** Anything declared with
  `quark(...)` in `core/build.gradle.kts` is downloaded at server start by
  `LibraryManager#loadFromGradle()` in `onLoad()` — HikariCP, MariaDB, ORMLite,
  Guice, Lamp and Adventure are all in that set (snakeyaml-engine for the configs is
  fetched the same way) and are **not** in the jar. Adventure is relocated to
  `org.bxteam.ndailyrewards.libs.kyori` by `quark { relocate(...) }`, in our classes
  and in the downloaded jars alike, because Paper ships an older Adventure without
  MiniMessage before 1.18.2. Only `implementation` dependencies are shaded — today
  just bStats, relocated under `org.bxteam.ndailyrewards.dependencies`. Picking the wrong
  keyword fails in one of two ways and neither shows up at compile time: an
  `implementation` that should have been `quark` silently doubles the jar, and a
  `quark` that should have been `implementation` is a `NoClassDefFoundError` on a
  live server.
- **The compatibility floor is Paper 1.16, and it is a product decision.** That
  is why `paper-api` is pinned to `1.16.5-R0.1-SNAPSHOT` in `libs.versions.toml`
  and disabled in `renovate.json`, why `apiVersion` is `1.16`, and why the
  toolchain is Java 21 but `options.release` is 17 with `disableAutoTargetJvm()`.
  The compiler will happily accept a 1.20-only API — the failure is a
  `NoSuchMethodError` on someone's 1.16 server.
- **The scheduler comes from Quark, never from Bukkit.** Inject `PaperScheduler`:
  it uses the region schedulers where they exist and the Bukkit scheduler on old
  Paper. The plugin advertises `foliaSupported = true`, so a direct
  `Bukkit.getScheduler()` call is a crash on Folia, not a style problem. Anything
  touching a player (inventories, sounds, titles) goes through
  `sync(player, ...)`/`later(player, ...)`; plain `sync(...)` is the global region
  on Folia and is only for console commands and plugin state.

## Commands

```bash
./gradlew build          # → build/libs/NDailyRewards-<version>.jar (shadowJar runs as part of it)
./gradlew runServer      # boots Paper 26.3 with LuckPerms and PlaceholderAPI for manual testing
./gradlew runFoliaServer # Folia
./gradlew runLegacyServer # Paper 1.16.5 on Java 17, the compatibility floor
./gradlew api:publish    # publishes the api module; needs REPO_USERNAME / REPO_PASSWORD
```

CI runs `./gradlew build --stacktrace` plus the Gradle wrapper validation on
every push and pull request, and attaches the jar to the run. There is nothing
else to remember — but note that the build is the *only* automated check, so a
change that compiles is a change that passes. Test on a real server.

## Code Guidelines

### Comments

- NO file-header banners and NO divider comments (`// --- helpers ---`). Group
  code with methods, not comment art.
- Add an inline comment only where the code is genuinely non-obvious — a real
  footgun, a version-boundary quirk, a reason a thing is done backwards. Then
  keep it to a line or two.
- Don't narrate the obvious. If a comment restates the next line, delete it.
- Javadoc on public API is fine and should say *why*, in one or two sentences.
  `api/` in particular is read by people who cannot see the implementation.

### Style

- Constructor injection with `@Inject`, and `@Singleton` on anything that holds
  state. Lombok's `@RequiredArgsConstructor(onConstructor = @__(@Inject))` is the
  usual shorthand; write the constructor out by hand when it also has to do work,
  as `RewardManager` does.
- New collaborators are bound in a Guice module, not fetched from a static field.
- Config and messages are read through the injected `PluginConfig` and
  `Messages`, never through `getConfig()` or by opening the YAML yourself. Read the
  field when you need it: a reload copies new values into the same objects but
  replaces the nested ones, so don't keep `config.rewards` around.

### User-facing strings

Every message is a field of `Messages` — a literal string sent to a player is a
bug. Adding one is one field with its default; Quark writes missing keys into
existing files on the next load, so upgraded servers get it too.

Both YAML files are regenerated from the classes on every load: key order is field
order, comments come from `@Comment`/`@Header`, and comments written by hand are
lost. Document options with annotations. Renaming or moving an existing key needs
a `Migration` in `PluginConfig.create(...)` and a bump of `@ConfigVersion`, or the
server owner's value is dropped. Invalid values (`@Min`, `@Check`, ...) fail the
load: on start the plugin disables itself with the list of violations, on
`/reward reload` the old values stay.

`MessageService` accepts both legacy `&a` codes and MiniMessage tags, converting
ampersands before parsing, and falls back to the legacy serializer if MiniMessage
throws. PlaceholderAPI is applied where it is installed. Write new strings as
MiniMessage.

### Paper/Bukkit gotchas

- **Anything scheduled goes through the injected `Scheduler`.** A
  `Bukkit.getScheduler()` call throws on Folia — see the settled decisions above.
- **`reload()` is partial by design.** It re-reads `config.yml` and `lang.yml`,
  sets the `DebugSwitch` and calls `RewardManager.reload()`. It does **not** rebuild
  the injector, `MenuManager` or `MessageService` — so state you add that is read
  once in a constructor will not pick up a reload. Either read it lazily or add it
  to `RewardManager.reload()`.
- **`playerDataCache` is a `ConcurrentHashMap` and the rewards map is not.**
  Player data is touched from async database callbacks; the reward definitions are
  only written during load and reload. Keep that split.
- **Integrations are soft-depends.** `IntegrationRegistry` only enables one when
  `pluginManager.isPluginEnabled(...)` says so. Never import an integration's
  classes outside its own package — the class load is what crashes a server that
  does not have it.

### Testing

There are no tests today, and the build is the only automated check. When adding
the first ones, put them in `core/src/test/java` and start with the parts that
have already gone wrong and are pure logic: the day/cooldown arithmetic in
`RewardManager`, the ampersand-to-MiniMessage conversion in `MessageService`.
One test per real trap, not one per method.

## Bash Guidelines

- Don't pipe output through `head`/`tail`/`less` to truncate — use tool-native
  flags (`git log -n 10`, `./gradlew build --console=plain`). Read the full output.
- Don't create scratch files (scripts, notes) unless asked.
- When given failures, just fix them — don't argue about who introduced them.
