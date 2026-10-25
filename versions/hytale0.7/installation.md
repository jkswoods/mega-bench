---
title: "Installation"
---

# Installation {#installation}

This guide follows the **Hytale 0.7 support line**. Published files can lag behind branch changes; read the chosen file’s release notes for the features it includes.

Mega Bench is a standard **JAR mod** for **Hytale 0.7.4 / Java 25**. It does not use a custom server launcher.

## Get the matching build {#get-the-matching-build}

Choose a file marked for **Hytale 0.7** from [CurseForge](https://www.curseforge.com/hytale/mods/mega-bench) or [GitHub releases](https://github.com/jkswoods/mega-bench/releases). Check its declared game-patch requirements and release notes before installing. The store’s coarse `0.7` tag alone does not establish compatibility with every patch.

If no compatible file is available, build the [`hytale0.7` source branch](https://github.com/jkswoods/mega-bench/tree/hytale0.7) or wait for a compatible release. Use the selected release or build output for its filename and checksum.

## Add it to a test world {#add-it-to-a-test-world}

1. Exit Hytale. Back up the complete world and its player files together with the JARs used by that world.
2. Put one Mega Bench JAR in Hytale's `UserData/Mods` folder. Remove older Mega Bench JAR versions from that folder.
3. Open the world's **Mods** settings and enable **MegaBench**.
4. Enable Emberworks and Foundry Floor in that world as well if you want to test their ordinary crafting adapters.
5. Load the world and follow [Your first workshop](getting-started.md).

For a dedicated server, use a separately backed-up development instance with the matching server/runtime. Actual second-client verification is still pending; this build is not recommended for an established multiplayer world.

## Updating an older development world {#updating-an-older-development-world}

The current source context needs a newly placed Mega Bench. If a previous tier-one development build placed your bench, normally break and replace it in the backed-up test world. Automatic migration of those placed blocks is not implemented. Saved player unlocks are separate from the access block.

## Removing or downgrading {#removing-or-downgrading}

Exit cleanly before changing mod files. Keep your matching backup. Removing a mod can leave missing items or blocks; downgrading should restore the complete matching world/player/JAR backup rather than changing a single JAR in a newer save.

[Continue to your first workshop](getting-started.md)
