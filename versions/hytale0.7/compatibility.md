---
title: "Compatibility and multiplayer"
---

# Compatibility and multiplayer {#compatibility-and-multiplayer}

## Supported target {#supported-target}

**Mega Bench 0.1.0-alpha.1** targets **Hytale 0.7.4 / Java 25**. Use the exact matching Alpha build and a backed-up test world. Other Hytale versions are not verified.

## Other workbenches {#other-workbenches}

The **0.1.0-alpha.2 test build** adds [automatic discovery and server compatibility settings](bench-support.md) for ordinary native crafting benches from any enabled pack. It has not been uploaded to CurseForge. The Alpha1 download remains the version described below.

## Alpha1 adapters {#alpha1-adapters}

| Source | What is supported |
| --- | --- |
| Ordinary stock crafting benches | Original recipe browsing, paid capability unlocks, source tiers and native crafting |
| Emberworks Workbench | Its ordinary crafting recipes and seven stable categories |
| Foundry Floor | Its ordinary Foundry Workbench recipes and five categories |
| Processing and fuel stations | Use the original station directly |
| Builder, diagram and unknown plugin interfaces | Separate adapters required |

Current native checks covered paid stock Workbench tiers two and three, an Emberworks Supply Crate and Foundry Poured Factory Concrete. These examples do not establish every recipe or every version combination. The tested compatibility assets are recorded in the source build configuration. The final Alpha also passed native UI loading, all seven Emberworks tabs, Foundry Floor browsing, one exact 3-Iron Bear Trap craft and a normal save/reload with paid tiers intact.

Mega Bench does not provide recursive crafting, a storage network, shared team progress or universal third-party compatibility. Processing-only products retain their source machine rules; the library does not turn them into ordinary free crafts.

## Personal progress {#personal-progress}

Unlocks and tiers are intended to belong to each player within that world. The bench is an access point; replacing it does not transfer everyone's purchases into its item. Saved progress and native configuration isolation have focused checks, but an actual second client has not yet verified the multiplayer experience.

Use a dedicated test instance for any multiplayer trial. Report the game version, Mega Bench version, other installed mods and the action that failed through [GitHub issues](https://github.com/jkswoods/mega-bench/issues). Keep personal player data and credentials out of public reports.

## Acceptance still open {#acceptance-still-open}

Player judgement of the model, icon, UI and sound balance is pending, along with real second-client checks. Broad crash recovery, adversarial packets and a full survival route are not certified. Keep matching world/player/JAR backups.
