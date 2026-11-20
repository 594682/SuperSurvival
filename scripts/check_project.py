#!/usr/bin/env python3
"""Offline structural checks; a successful Forge build still requires Gradle."""
import json
from pathlib import Path
import zipfile

root = Path(__file__).resolve().parents[1]
resource = root / "src/main/resources"
java = root / "src/main/java/com/example/supersurvival"
checked = 0
for path in resource.rglob("*.json"):
    json.loads(path.read_text(encoding="utf-8"))
    checked += 1

for name in ["SuperSurvival.java","ClientControls.java","Trades.java"]:
    code = (java/name).read_text(encoding="utf-8")
    assert code.count("{") == code.count("}"), f"Mismatched braces in {name}"
    assert code.count("(") == code.count(")"), f"Mismatched parentheses in {name}"
    assert "SavedData.Factory" not in code, "Wrong API for Minecraft 1.20.1"

toml = (resource/"META-INF/mods.toml").read_text(encoding="utf-8")
assert 'modId="supersurvival"' in toml
assert 'logoFile="logo.png"' in toml
assert 'version="2.1.0"' in toml
assert (resource/"logo.png").is_file()

workflow = (root/".github/workflows/build.yml").read_text(encoding="utf-8")
assert "gradle --no-daemon clean build" in workflow
assert "actions/upload-artifact@v4" in workflow

print(f"PASS: {checked} valid JSON files, 3 Java files structurally checked, icon + GitHub workflow found")
print("NOTE: This is NOT a Java/Forge compilation test.")
