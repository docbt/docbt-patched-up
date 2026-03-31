# Project Instructions

## Pull Requests — IMMER Squash Merge

**Jeder PR muss als Squash Merge zusammengeführt werden**, damit Claude nicht als Contributor erscheint.
Den Hinweis ⚠️ immer in die PR-Beschreibung schreiben.

---

## Kleinanzeigen Versionsupdate

Wenn eine neue Kleinanzeigen-Version unterstützt werden soll (z.B. `2026.14.0`):

### Dateien die geändert werden müssen

1. `versions.json` → `"kleinanzeigen": "NEUE_VERSION"`
2. `patches/src/main/kotlin/app/docbt/patched_up/kleinanzeigen/ads/HideAdsPatch.kt` → `compatibleWith(...)`
3. `patches/src/main/kotlin/app/docbt/patched_up/kleinanzeigen/hidepur/HidePurPatch.kt` → `compatibleWith(...)`
4. `patches-list.json` → beide Kleinanzeigen-Patches auf neue Version
5. `CHANGELOG.md` → neuen Eintrag hinzufügen (semantic-release macht das auch automatisch, aber als Trigger)

**Nicht manuell ändern:** `gradle.properties` und `patches-bundle.json` — diese werden von semantic-release automatisch verwaltet.

### Vorgehen

1. Neuen Branch von `main` erstellen
2. Dateien ändern (nur unterstützte Version, keine Fingerprints anpassen wenn nicht nötig)
3. Commit + Push + PR erstellen
4. User merged als **Squash Merge** → CI baut automatisch und erstellt neues Release

### Wenn semantic-release stecken bleibt (immer gleiche Version)

Das passiert wenn der git-Tag durch einen force-push orphaned wurde.

**Fix:**
```bash
# Aktuellen HEAD-SHA herausfinden
git log --oneline origin/main -1

# Tag löschen und neu auf aktuellen HEAD setzen
git push origin :refs/tags/vX.Y.Z
git tag -f vX.Y.Z <SHA>
git push origin vX.Y.Z
```

Danach einen neuen kleinen `fix:` Commit via PR mergen (z.B. CHANGELOG bereinigen) — semantic-release erkennt dann den neuen Commit nach dem Tag und erstellt die nächste Version.

---

## CI / Release-Prozess

- CI läuft automatisch bei jedem Push auf `main`
- `gradle-semantic-release-plugin` bestimmt die Version anhand von Conventional Commits
- `fix:` → Patch-Bump (z.B. 1.3.2 → 1.3.3)
- `feat:` → Minor-Bump (z.B. 1.3.x → 1.4.0)
- Das `.mpp` wird neu gebaut und auf GitHub Releases hochgeladen
- `patches-bundle.json` wird automatisch mit neuer `download_url` aktualisiert
- Morphe erkennt die neue Version anhand der `version`-Nummer in `patches-bundle.json`
