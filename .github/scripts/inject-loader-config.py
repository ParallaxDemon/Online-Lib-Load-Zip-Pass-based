"""Fill the temporary Java build copy from GitHub Actions secrets, without logging values."""

import os
from pathlib import Path

source = Path("build-src/Project/app/src/main/java/com/Parallax/Papa/ParallaxConfig.java")
content = source.read_text(encoding="utf-8")
for key in ("PARALLAX_URL_HEX", "PARALLAX_PASSWORD_HEX"):
    value = os.environ.get(key, "").strip()
    if len(value) < 8 or len(value) % 2 or any(c not in "0123456789abcdefABCDEF" for c in value):
        raise SystemExit(f"Configure GitHub Actions secret {key} with the encoded HEX value")
    token = f"@{key}@"
    if content.count(token) != 1:
        raise SystemExit(f"Expected exactly one {key} token in the Java source")
    content = content.replace(token, value)
source.write_text(content, encoding="utf-8")
