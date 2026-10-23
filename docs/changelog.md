# Development status

## 0.1.0-alpha.2 — test build

- Discovers ordinary native crafting benches from any enabled pack, without name-prefix filters or optional mod dependencies.
- Reads the owning pack for source grouping and rescans when opening the library.
- Preserves original recipes, category IDs, unlock ingredients and tier upgrades.
- Adds an explicit one-bench hand-in when no suitable original unlock recipe exists.
- Adds strict server compatibility settings for exclusions, selected bench items and original unlock recipes.
- Displays recipes that use an output list without a primary output.

See [Other workbench support](bench-support.md). This test build has not been uploaded to CurseForge. Native checks passed for unfamiliar bench discovery, original paid unlocks, tier locks/upgrades, one-physical-bench hand-in, original queue outputs and normal reload. Player review and actual second-client checks remain open.

## 0.1.0-alpha.1 — experimental Alpha

- MIT License added for original Mega Bench code, docs and project assets.
- Category artwork is rendered through native item widgets; copied game and compatibility UI images are excluded from the public JAR.
- Native Common.ui slot styles are referenced from the installed game.
- Crafting, paid progress, inventory authority and saved formats retain the previously verified implementation.

Local build, 11 progress checks, package audit and exact source-build reproduction pass. The exact Alpha passed native UI/category loading, stock crafting and normal save/reload. Submitted to [CurseForge](https://www.curseforge.com/hytale/mods/mega-bench) as Alpha; **under review**, with public availability awaiting approval. Hytale **0.7.4 only**; the store's **0.7** tag does not widen compatibility.

## 0.1.0-spike5r1

Initial development source and player guide for Hytale 0.7.4 / Java 25.

- Placeable workshop library with original model, icon and generated sound families.
- Grouped crafting stations with paid personal unlocks and source tier upgrades.
- Unlock and upgrade controls beneath the left source list, central recipe browser and right recipe details.
- Original categories, search, filters, live inventory preview and quantities from 1 to 16.
- Original native crafting and cancellation/refund handling.
- Initial ordinary stock, Emberworks Workbench and Foundry Floor adapters.
- Progress retained through normal reload and bench replacement.

This was the earlier source/guide development build. Human visual/listening review, actual second-client checks and broader survival/release assurance remain pending. Older development access blocks require normal replacement in a backed-up test world.
