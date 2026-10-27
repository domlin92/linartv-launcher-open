"""Check all DE/ES/IT UI and phone-upload messages without Android or network."""
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
literal = r'"(?:\\.|[^"\\])*"'
expression = literal + r'(?:\s*\+\s*' + literal + r')*'
calls = re.compile(r'\btr\(\s*(' + expression + r')\s*,\s*(' + expression + r')\s*\)', re.S)
required = {"England", "France", "Spain", "Germany", "Italy"}
for source in (root / "app/src/main/java").rglob("*.java"):
    for _, english in calls.findall(source.read_text(encoding="utf-8-sig")):
        required.add("".join(json.loads(item) for item in re.findall(literal, english)))


def unique_pairs(pairs):
    result = {}
    for key, value in pairs:
        assert key not in result, f"Duplicate translation key: {key!r}"
        result[key] = value
    return result


key_set = None
for code in ("de", "es", "it"):
    entries = json.loads((root / f"app/src/main/assets/i18n/{code}.json").read_text(encoding="utf-8"),
                         object_pairs_hook=unique_pairs)
    missing = required - entries.keys()
    assert not missing, f"{code}: missing {sorted(missing)!r}"
    assert all(isinstance(value, str) and value.strip() for value in entries.values()), code
    assert all("\ufffd" not in value for value in entries.values()), f"{code}: encoding damage"
    assert key_set is None or set(entries) == key_set, "Language catalogues differ"
    key_set = set(entries)
    for key, value in entries.items():
        if key.endswith(" "):
            assert value.endswith(" "), f"{code}: missing separator space in {key!r}"
    print(f"OK: {code}: {len(entries)} translations; all {len(required)} required messages covered")
