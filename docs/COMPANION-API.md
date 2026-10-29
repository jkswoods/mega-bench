# Companion API 1

This optional API first targets Mega Bench **0.3.0** on Hytale `>=0.7.4 <0.8.0`, with Java 25. It is currently a private development candidate. Mega Bench has no dependency on a companion and operates normally without one. Game compatibility, API compatibility and versions actually tested are separate claims.

A companion compiles against Mega Bench without bundling its classes. It declares a required runtime dependency on `MegaBench:MegaBench`. For the initial public companion line, use `>=0.3.0 <0.4.0` and register with API integer `1`. A private paired build must instead use the exact tested prerelease, currently `=0.3.0-disks.dev.1`. Do not publish a dependency on an unavailable private build. Breaking API changes require another API integer and a compatible mod version change; recheck compatibility before widening the companion's dependency range.

## Registration and scope

Register one `CompanionExtension.Provider` in plugin start with `CompanionExtension.register(1, provider)`. A second registration, null provider or mismatched API is refused. Unregister that same provider during shutdown. Unregister prevents future sessions; the companion must close its existing sessions and handlers safely during its own shutdown. Hot replacement of a running companion is not supported.

`Provider.open(Context)` receives the current player reference, entity store and Mega Bench coordinates. Return a new session scoped to that player and world, or null to retain ordinary Mega Bench operation. Never share mutable player sessions across worlds. Session callbacks run on the world's thread and must remain bounded; use no blocking I/O. Revalidate any asynchronous result on that thread before applying it.

`openBench` uses Mega Bench's existing block identity and distance checks to open a nearby bench. `itemName` resolves the native localized item name. Neither method grants progression or a crafting entitlement.

## Ingredient authority and lifecycle

`Session.inputs(recipe, quantity)` returns a non-null extra native `ItemContainer`, or `EmptyItemContainer.INSTANCE` when unavailable. It can be called repeatedly for preview, filtering, output preflight and admission; it must not debit, acquire leases or cache an assumption that the last previewed recipe is the admitted recipe. Mega Bench places player inventory before this extra source in a `CombinedItemContainer`. Never return the player's own inventory or another overlapping source. Creative uses the player's original native inventory path and does not request companion inputs.

The companion is responsible for validating ownership, world, current connection, permitted quantities, metadata and leases on the actual mutation path. A preview check alone is insufficient. Disconnected, stale or unauthorized sources must refuse mutation without changing contents. Clone/preflight operations must be isolated from persisted contents. Do not create an independent output or refund path.

After Mega Bench checks the original recipe, knowledge, paid tier, complete input batch and native player output capacity, it calls `started()` before native queue admission. This callback can acquire a source lease, or refuse by throwing `IllegalArgumentException` or `IllegalStateException`. A refusal clears the native bench context and calls `finished()`. Lease acquisition must be safe even if no companion materials are ultimately taken, including Creative and player-only crafts.

The exact native `CraftingManager` performs admission and payment. Its normal output, cancellation and refund paths remain authoritative. `finished()` releases leases after native completion, successful cancellation or context clearing, including refused admission. It may also be called during close without a preceding start. `close()` follows native bench clearing and releases UI, handlers and remaining resources. Both cleanup callbacks must be idempotent and must not throw. Keep leases until native refund/clear has finished.

Workbench unlock and tier payments continue to use player inventory only. A companion cannot unlock a station, alter recipe or source assets, bypass knowledge, or redirect native output. Existing progress serialization and block IDs are unchanged.

## UI contract

API 1 supports one storage companion. `render(view, version, commands, events)` receives either `Crafting` or `Storage`. It can append its screen under `#ExtensionCentre` and modal content under `#CompanionOverlay`, then update its own controls. It must provide `#BackToCrafting` and `#StorageClose`; Mega Bench binds their navigation. The shared footer exposes `#RackIcon`, `#RackConnection` and `#RackStatus`. The base owns `#OpenStorage`, `#ActivityBar`, global cancel/progress, and the recipe/progression controls. This UI seam is intended for the first storage companion, not arbitrary competing providers.

Companion actions use `Action: ext:<action>` plus the supplied string `Version`. An optional `@Search` string is delivered to `action(action, search)`. Mega Bench rejects stale page versions and dispatches extension actions only while Storage is selected. The companion must independently validate action names, inputs, ownership and current inventory state. Client payloads are requests, never authority.

`signature()` is a cheap read-only revision string used to refresh live state. Change it when visible companion state changes. Switching views retains the Crafting controls, search and scroll state by moving that workspace off screen; avoid clearing or modifying those controls from companion rendering. No-companion mode restores the existing Crafting dimensions and hides the storage footer.

## Verification boundaries

The build checks registration, source composition, Creative isolation, paid access/progress and the approved collision geometry. Loaded native container tests and real client crafting, refunds, reconnects, normal save/reload and concurrent players require separate verification with the exact paired files. No API declaration alone certifies those gates.
