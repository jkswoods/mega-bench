# Installation

Mega Bench is a standard **JAR mod** for **Hytale 0.7.4 / Java 25**. It does not use a custom server launcher.

## Get the matching build

A downloadable binary release has not been published. Use a matching development JAR supplied for testing, or build from the [source repository](https://github.com/jkswoods/mega-bench#build-from-source). The current filename is `MegaBench-0.1.0-spike5r1.jar`.

## Add it to a test world

1. Exit Hytale. Back up the complete world and its player files together with the JARs used by that world.
2. Put one Mega Bench JAR in Hytale's `UserData/Mods` folder. Remove older Mega Bench JAR versions from that folder.
3. Open the world's **Mods** settings and enable **MegaBench**.
4. Enable Emberworks and Foundry Floor in that world as well if you want to test their ordinary crafting adapters.
5. Load the world and follow [Your first workshop](getting-started.md).

For a dedicated server, use a separately backed-up development instance with the matching server/runtime. Actual second-client verification is still pending; this build is not recommended for an established multiplayer world.

## Updating an older development world

The current source context needs a newly placed Mega Bench. If a previous tier-one development build placed your bench, normally break and replace it in the backed-up test world. Automatic migration of those placed blocks is not implemented. Saved player unlocks are separate from the access block.

## Removing or downgrading

Exit cleanly before changing mod files. Keep your matching backup. Removing a mod can leave missing items or blocks; downgrading should restore the complete matching world/player/JAR backup rather than changing a single JAR in a newer save.

[Continue to your first workshop](getting-started.md)
