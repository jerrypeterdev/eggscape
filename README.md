# EggScape – Pet Collection

Play RuneScape. Hatch eggs. Collect tiny bosses.

EggScape adds a boss collection game to your RuneLite sidebar, with 104 collection entries and 58 egg designs. Earn hatch progress while you play, reveal the boss inside your egg, then spin the egg roulette to discover what comes next.

## Getting started

Enable EggScape and open its sidebar. Click the mystery egg to receive your first Jerry Egg. This introductory egg takes 15 minutes of active gameplay; later eggs take one hour.

Progress is based on time rather than raw XP, so different skills and account levels use the same basic hatch rate. Ongoing AFK activities count as active play when the plugin detects training or combat. Standing idle while logged in earns progress at half speed. Offline time does not count.

When your egg is ready, it shakes and the progress bar becomes a Hatch button. Click it to reveal your boss, then spin the vertical roulette to choose your next egg. Progress keeps accumulating while an egg is waiting to hatch.

## Build your collection

- Collect individual bosses and variants in a personal collection log.
- Shared eggs cover raids and boss families, including Barrows, Perilous Moons, Dagannoth Kings, Royal Titans, Hydra and Hunllef.
- Defeating bosses from your current egg's family can award extra progress based on active encounter time.
- Newly completed quests can add bonus progress.
- Enable the optional Jerry Sound Pack.

Your egg, collection and progress are saved per character through RuneLite's configuration system. These are EggScape collectibles, separate from your actual in-game pets and collection log.

## Development

Build with Java 21 and Gradle 8.10 using `gradle build`. On the existing portable Windows setup, use `build.bat build --offline`.

`start-test.bat` launches the local development client with testing controls. `start-release-test.bat` previews the normal player interface. The three test controls are absent and their actions are blocked in ordinary RuneLite launches.

Test tools require the explicit JVM property `eggscape.developerTools=true`; it is enabled only by the local development task. The test launcher and tests are excluded from the runtime JAR.

## More information

- [Boss bonuses](docs/boss-bonuses.md).
- [Audio credits](docs/audio.md).

EggScape is a community plugin and is not affiliated with Jagex. RuneScape names and boss sprites belong to their respective owners.

## Licence

The plugin source code is offered under the [BSD 2-Clause licence](LICENSE). Third-party sounds and RuneScape assets retain their respective rights and terms.
