#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
"${MB_DOCS_PYTHON:-python3}" -m mkdocs build --strict
