"""Fill the temporary Java build copy without logging configuration values."""

import os
import secrets
import sys
from pathlib import Path

source = Path("build-src/Project/app/src/main/java/com/Parallax/Papa/ParallaxConfig.java")
content = source.read_text(encoding="utf-8")
if "@PARALLAX_URL_HEX@" not in content and "@PARALLAX_PASSWORD_HEX@" not in content:
    print("Using built-in Java loader configuration")
    sys.exit(0)
values = {key: os.environ.get(key, "").strip() for key in ("PARALLAX_URL_HEX", "PARALLAX_PASSWORD_HEX")}
test_config = not any(values.values())
if test_config:
    values = {"PARALLAX_URL_HEX": secrets.token_hex(32).upper(),
              "PARALLAX_PASSWORD_HEX": secrets.token_hex(8).upper()}
    print("WARNING: Building with random test HEX. This loader cannot download an archive.")
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as output:
            output.write("⚠️ Loader built with random test HEX; download and load will not work.\n")
    step_output = os.environ.get("GITHUB_OUTPUT")
    if step_output:
        with open(step_output, "a", encoding="utf-8") as output:
            output.write("suffix=-TEST-CONFIG\n")
for key in ("PARALLAX_URL_HEX", "PARALLAX_PASSWORD_HEX"):
    value = values[key]
    if len(value) < 8 or len(value) % 2 or any(c not in "0123456789abcdefABCDEF" for c in value):
        raise SystemExit(f"Configure GitHub Actions secret {key} with the encoded HEX value")
    token = f"@{key}@"
    if content.count(token) != 1:
        raise SystemExit(f"Expected exactly one {key} token in the Java source")
    content = content.replace(token, value)
source.write_text(content, encoding="utf-8")
