---
title: "Installation"
---

# Installation {#installation}

Mega Bench is a standard **JAR mod** for **Hytale 0.7.4 / Java 25**. It does not use a custom server launcher.

## Get the matching build {#get-the-matching-build}

The experimental **0.1.0-alpha.1** JAR is submitted to [CurseForge](https://www.curseforge.com/hytale/mods/mega-bench) and is **under review**. Public download availability depends on moderator approval. Once approved, use the project website: Alpha files do not appear in the CurseForge client. The store's **0.7** tag means **Hytale 0.7.4 only** for this build.

The filename is `MegaBench-0.1.0-alpha.1.jar`. Its SHA256 is `dff77ac3f030d58f8c440ab34682fd54f16796e30a8f0807c04fd5ea7970bdf0`. You can also build the matching [source](https://github.com/jkswoods/mega-bench#build-from-source).

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
