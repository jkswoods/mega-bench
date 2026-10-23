# Compatibility and multiplayer

## Supported target

**Mega Bench 0.1.0-alpha.1 candidate** targets **Hytale 0.7.4 / Java 25**. Use the exact matching development build and a backed-up test world. Other Hytale versions are not verified.

## Initial adapters

| Source | What is supported |
| --- | --- |
| Ordinary stock crafting benches | Original recipe browsing, paid capability unlocks, source tiers and native crafting |
| Emberworks Workbench | Its ordinary crafting recipes and seven stable categories |
| Foundry Floor | Its ordinary Foundry Workbench recipes and five categories |
| Processing and fuel stations | Use the original station directly |
| Builder, diagram and unknown plugin interfaces | Separate adapters required |

Current native checks covered paid stock Workbench tiers two and three, an Emberworks Supply Crate and Foundry Poured Factory Concrete. These examples do not establish every recipe or every version combination. The tested compatibility assets are recorded in the source build configuration.

Mega Bench does not provide recursive crafting, a storage network, shared team progress or universal third-party compatibility. Processing-only products retain their source machine rules; the library does not turn them into ordinary free crafts.

## Personal progress

Unlocks and tiers are intended to belong to each player within that world. The bench is an access point; replacing it does not transfer everyone's purchases into its item. Saved progress and native configuration isolation have focused checks, but an actual second client has not yet verified the multiplayer experience.

Use a dedicated test instance for any multiplayer trial. Report the game version, Mega Bench version, other installed mods and the action that failed through [GitHub issues](https://github.com/jkswoods/mega-bench/issues). Keep personal player data and credentials out of public reports.

## Acceptance still open

Player judgement of the model, icon, UI and sound balance is pending, along with real second-client checks. Broad crash recovery, adversarial packets and a full survival route are not certified. Keep matching world/player/JAR backups.
