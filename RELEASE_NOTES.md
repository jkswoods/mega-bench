## Optional companion support

Alpha for Hytale 0.7, adding Companion API 1 for compatible storage addons.

- Adds API 1 for optional companion mods to supply permitted Survival crafting ingredients and a storage screen.
- A connected companion can show storage status below Crafting and return to the selected recipe and workbench.
- Creative retains native free crafting. Personal unlocks and upgrades still use the player's inventory, and native crafting retains control of recipe checks, outputs and cancellation/refunds.
- Retains the player-approved 0.2.1 collision shape, existing saved progression, models and sounds.

Paid companion-backed crafting and normal reload were checked in Hytale 0.7.5. Creative input isolation and cancellation/refund order have native engine checks. Human visual/listening review and actual second-player ownership, contention and bridge protection checks remain pending. Use a backed-up test world for this Alpha.

Mega Bench works without a companion installed. Companion mods are separate downloads and must declare a compatible Mega Bench dependency. See the [storage guide](https://jkswoods.github.io/mega-bench/hytale0.7/companions/) and [API contract](https://github.com/jkswoods/mega-bench/blob/hytale0.7/docs/COMPANION-API.md).

Requires Hytale >=0.7.4 <0.8.0 and Java 25. With the game/server stopped, replace the previous Mega Bench JAR and ensure that only one copy exists across the main Mods directory and the save's mods directory. Keep a complete world/player backup before updating.
