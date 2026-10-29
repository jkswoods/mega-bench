## Optional companion support

Private development candidate for Mega Bench 0.3.0. Publication and final native checks are pending.

- Adds API 1 for optional companion mods to supply permitted Survival crafting ingredients and a storage screen.
- A connected companion can show storage status below Crafting and return to the selected recipe and workbench.
- Creative retains native free crafting. Personal unlocks and upgrades still use the player's inventory, and native crafting retains control of recipe checks, outputs and cancellation/refunds.
- Retains the player-approved 0.2.1 collision shape, existing saved progression, models and sounds.

Mega Bench works without a companion installed. Companion mods are separate downloads and must declare a compatible Mega Bench dependency. See [the API contract](docs/COMPANION-API.md).

Requires Hytale >=0.7.4 <0.8.0 and Java 25. With the game/server stopped, replace the previous Mega Bench JAR and ensure that only one copy exists across the main Mods directory and the save's mods directory. Keep a complete world/player backup before updating.
