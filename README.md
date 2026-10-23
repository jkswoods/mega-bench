# Mega Bench

[Player guide](https://jkswoods.github.io/mega-bench/) · [Getting started](docs/getting-started.md) · [Report an issue](https://github.com/jkswoods/mega-bench/issues)

![Mega Bench — One workshop. Your crafting library.](docs/assets/brand/banner.svg)

One placeable workshop gives you a native crafting library for compatible Hytale benches. Pay a station's original recipe ingredients to unlock its capability, invest in its original tier upgrades, then search and craft its recipes from one screen. Unlocks belong to each player in that world.

- Workbench selection, unlocks and upgrades on the left.
- A searchable recipe grid with original category tabs in the centre.
- Ingredient counts, output, time and quantity controls on the right.
- Original native crafting, with batches of 1–16 and native cancellation/refunds.
- Initial ordinary crafting adapters for stock benches, Emberworks Workbench and Foundry Floor.

![Mega Bench placed in Hytale](docs/assets/screenshots/bench-three-quarter.png)

![Mega Bench's current native crafting interface](docs/assets/screenshots/recipe-browser.png)

**Development build 0.1.0-spike5r1 · Hytale 0.7.4 · Java 25.** A downloadable mod release has not been published. The source and player guide are available for development review. Use a separate backed-up world; actual second-player checks and player visual/listening review are still pending. Processing, builder and special plugin stations need separate adapters.

## Build from source

Use JDK 25, the pinned official Hytale server and asset pack, and the compatibility JARs listed in `config/target.json`. Game binaries, stock UI sprites and compatibility artwork are not included in this repository. The build reads the required UI images from your own supplied dependencies and verifies their hashes.

```sh
python3 tools/build.py \
  --java-home /path/to/jdk25 \
  --server-jar /path/to/HytaleServer.jar \
  --assets-zip /path/to/Assets.zip \
  --compat-jar /path/to/Emberworks.jar \
  --compat-jar /path/to/FoundryFloor.jar
```

The exact snapshot currently needs both compatibility packs to reproduce its complete UI resources. The `--java-home`, `--server-jar` and `--assets-zip` values may also be supplied through `MB_JAVA_HOME`, `MB_SERVER_JAR` and `MB_ASSETS_ZIP`. `JAVA_HOME` is supported as a fallback for the JDK.

The build compiles the plugin, runs the progress tests and writes `build/libs/MegaBench-0.1.0-spike5r1.jar`. With the pinned inputs its SHA256 is `6eeac06431fb3c9f87cb81d4a99eae62080be0248a6a08ddbf1437afe9bcc56c`. Checked-in native models, UI sources and original textures are editable; normal builds do not regenerate artwork or audio.

## Build the guide

```sh
python3 -m venv .venv-docs
.venv-docs/bin/python -m pip install -r requirements-docs.txt
MB_DOCS_PYTHON=.venv-docs/bin/python bash tools/build_player_guide.sh
```

The strict build writes `build/player-site/`. GitHub Pages deployment uses a manually dispatched workflow on `main`; pull requests only build the guide.

No redistribution license has been selected. See [third-party notices](THIRD_PARTY_NOTICES.md) for game dependencies, original assets and generated sound attribution.
