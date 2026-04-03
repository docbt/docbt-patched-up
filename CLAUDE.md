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

### Backmerge dev-Branch

Nach jedem Release versucht semantic-release, `main` in den `dev`-Branch zurückzumergen.
Wenn das fehlschlägt (non-fast-forward), manuell fixen:
```bash
git checkout -B dev origin/dev
git merge origin/main --no-edit -m "chore: merge main into dev after vX.Y.Z release"
# Bei Konflikten: main-Versionen nehmen für CHANGELOG.md, gradle.properties, patches-bundle.json, patches-list.json
git checkout origin/main -- CHANGELOG.md gradle.properties patches-bundle.json patches-list.json
git add . && git commit && git push origin dev
```

---

## Morphe Patcher API

### Aktuelle Version: 1.3.3

Seit v1.3.1 wurde der `use`-Parameter aus `bytecodePatch` / `resourcePatch` entfernt.
`use = false` einfach weglassen — kein Ersatz nötig.

### Compatibility-Objekt (neu ab 1.3.x)

Statt `compatibleWith("pkg" to setOf("version"))` jetzt:
```kotlin
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

private val COMPAT = Compatibility(
    name = "App Name",
    packageName = "com.example.app",
    appIconColor = 0x2EAD33,  // WICHTIG: Int-Format 0xRRGGBB, KEIN String "#2EAD33"!
    targets = listOf(
        AppTarget(version = "2.0.0"),
        AppTarget(version = "1.0.0"),
    ),
)

val myPatch = bytecodePatch(name = "...", description = "...") {
    compatibleWith(COMPAT)
    // ...
}
```

**Aktuelle App-Farben:**
- Kleinanzeigen: `0x2EAD33`
- Google News: `0x4285F4`

---

## Aktueller Stand (nach v1.4.0)

### Unterstützte Versionen
- Google News: `5.155.0.885456612`, `5.155.0.892577434`
- Kleinanzeigen: `2026.14.0`, `2026.14.2`

### Morphe Patcher
- `gradle/libs.versions.toml`: `morphe-patcher = "1.3.3"`
- Alle Patches nutzen den neuen `Compatibility`-Stil

### Google News Fingerprints (v5.155.0.x)
Alle IGET_BOOLEAN-Stellen die `Ladwy;->j:Z` lesen (CustomTabs-Flag):

| Fingerprint | Klasse | DEX | Methode |
|---|---|---|---|
| `LajdkFingerprint` | `Laixy;` | classes.dex | `access$1301` |
| `LaedzFingerprint` | `Ladyo;` | classes3.dex | `<init>` |
| `LajdrFingerprint` | `Laiyf;` | classes3.dex | `a` |
| `LajgzFingerprint` | `Lajbr;` | classes3.dex | `<init>` |
| `LajhkFingerprint` | `Lajby;` | classes3.dex | `<init>` |
| `CustomTabsTrampolineFingerprint` | `Lcom/.../CustomTabsTrampolineActivity;` | classes3.dex | `onCreate` |

**DEX-Analyse-Script** (Python3) um neue Fingerprints nach Obfuscation-Änderung zu finden:
```python
import struct, sys

def find_iget_boolean_field_refs(dex_path):
    data = open(dex_path, 'rb').read()
    # Field IDs section
    field_ids_off = struct.unpack_from('<I', data, 52)[0]
    field_ids_size = struct.unpack_from('<I', data, 56)[0]
    # Find field index for Ladwy;->j:Z
    target_field_idx = None
    for i in range(field_ids_size):
        off = field_ids_off + i * 8
        class_idx, type_idx, name_idx = struct.unpack_from('<HHI', data, off)
        # ... (inspect class/type/name string pools to match Ladwy;->j:Z)
    # Scan for IGET_BOOLEAN (0x55) with target field
    # opcode at even offsets, field ref at +1 (16-bit instruction)
```

Für neue Versionen: DEX-Datei aus APK extrahieren und alle IGET_BOOLEAN-Stellen
mit dem relevanten Field-Index scannen. Neue Klassen dann in `Fingerprints.kt` eintragen.

---

## Keine Session-Links in Commits

Niemals `https://claude.ai/...` in Commit-Messages oder PR-Beschreibungen einfügen.
Aus Sicherheitsgründen verboten.
