---
title: "Development status"
---

# Development status {#development-status}

Changes described here belong to the **Hytale 0.7 support line**. Download availability, numbered release history and the changes included in a particular file are recorded on [GitHub releases](https://github.com/jkswoods/mega-bench/releases).

## Optional storage companions

- API 1 lets a separately installed compatible companion add permitted Survival ingredient sources and a Storage view.
- The connection footer leads from Crafting to Storage and back to the selected recipe/workbench.
- Personal unlock and upgrade payments use player inventory. Creative retains native free crafting; outputs and cancellation/refunds remain native.

Paired Hytale 0.7.5 checks covered storage-backed paid crafting and normal reload. Real second-player contention and human visual/listening review remain pending. See [Storage companions](companions.md) and the selected download's release notes.

## Automatic workbench discovery {#010-alpha2-test-build}

- Discovers ordinary native crafting benches from any enabled pack, without name-prefix filters or optional mod dependencies.
- Reads the owning pack for source grouping and rescans when opening the library.
- Preserves original recipes, category IDs, unlock ingredients and tier upgrades.
- Adds an explicit one-bench hand-in when no suitable original unlock recipe exists.
- Adds strict server compatibility settings for exclusions, selected bench items and original unlock recipes.
- Displays recipes that use an output list without a primary output.

See [Other workbench support](bench-support.md). Native checks passed for unfamiliar bench discovery, original paid unlocks, tier locks/upgrades, one-physical-bench hand-in, original queue outputs and normal reload. Player review and actual second-client checks remain open.

## Native UI and asset integration {#010-alpha1-experimental-alpha}

- MIT License added for original Mega Bench code, docs and project assets.
- Category artwork is rendered through native item widgets; copied game and compatibility UI images are excluded from the public JAR.
- Native Common.ui slot styles are referenced from the installed game.
- Crafting, paid progress, inventory authority and saved formats retain the previously verified implementation.

Local build, 11 progress checks, package audit and exact source-build reproduction pass. Native checks covered native UI/category loading, stock crafting and normal save/reload. Check the selected file’s release notes and compatibility on [CurseForge](https://www.curseforge.com/hytale/mods/mega-bench) or [GitHub releases](https://github.com/jkswoods/mega-bench/releases).

## Workshop library foundations {#010-spike5r1}

Initial development source and player guide for Hytale 0.7.4 / Java 25.

- Placeable workshop library with original model, icon and generated sound families.
- Grouped crafting stations with paid personal unlocks and source tier upgrades.
- Unlock and upgrade controls beneath the left source list, central recipe browser and right recipe details.
- Original categories, search, filters, live inventory preview and quantities from 1 to 16.
- Original native crafting and cancellation/refund handling.
- Initial ordinary stock, Emberworks Workbench and Foundry Floor adapters.
- Progress retained through normal reload and bench replacement.

These foundations remain part of the support line. Human visual/listening review, actual second-client checks and broader survival/release assurance remain pending. Older development access blocks require normal replacement in a backed-up test world.
