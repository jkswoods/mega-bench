#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 tools/build.py --server-jar "$HYTALE_SERVER_JAR" --java-home "$JAVA_HOME"
