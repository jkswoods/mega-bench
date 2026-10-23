# Third-party notices

Hytale is a separately licensed runtime and build dependency. This repository does not include the Hytale server, client, stock artwork, sounds or toolchains. Hytale UI sprite paths and hashes are recorded in `config/external-ui.json`; the build reads the matching images from a separately installed asset pack. Those game images are excluded from Git.

Optional Emberworks and Foundry Floor category artwork is likewise read from separately supplied compatibility JARs during the build. The files are not vendored here. Their presence does not imply a license grant for either mod.

Mega Bench's workshop geometry, pixel palette, item icon and guide identity are project assets. The item icon was made using OpenAI image generation, then exported at native icon sizes. Selected runtime sound families were generated through the connected ElevenLabs Sound Effects workflow. Sanitized mod-relevant generation and export details are in `assets/ART-PROVENANCE.json` and `assets/SOURCE-AUDIO.json`; raw recordings and private development records are excluded. Generated recordings remain subject to the generating account's applicable ElevenLabs terms.

No redistribution license has been selected for Mega Bench's source and original assets. Publishing this repository does not grant rights beyond applicable law or the relevant third-party terms.

The generated player guide uses MkDocs, its bundled Read the Docs theme and search assets. See [the guide notices](docs/notices.md) for their licenses. Mega Bench is an independent mod and is not an official Hytale product.
