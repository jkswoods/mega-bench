# Third-party notices

Hytale is a separately licensed runtime and build dependency. This repository and the public Alpha JAR do not include the Hytale server, client, game artwork, stock sounds or toolchains. The UI references the installed native Common.ui styles and asks the client to render loaded item icons.

Emberworks and Foundry Floor compatibility reads recipes and item definitions from those separately installed mods at runtime. Their binaries and artwork are not bundled in Mega Bench. Each mod retains its own terms.

Mega Bench's workshop geometry, pixel palette, item icon and guide identity are project assets. The item icon was made using OpenAI image generation, then exported at native icon sizes. Selected runtime sound families were generated through the connected ElevenLabs Sound Effects workflow. Sanitized mod-relevant generation and export details are in `assets/ART-PROVENANCE.json` and `assets/SOURCE-AUDIO.json`; raw recordings and private development records are excluded. Generated recordings remain subject to the generating account's applicable ElevenLabs terms.

Mega Bench's original code, documentation and project assets are available under the MIT License in LICENSE. That license does not relicense Hytale, game content depicted in native screenshots, or separately identified third-party components.

The generated player guide uses MkDocs, its bundled Read the Docs theme and search assets. See [the guide notices](docs/notices.md) for their licenses. Mega Bench is an independent mod and is not an official Hytale product.
