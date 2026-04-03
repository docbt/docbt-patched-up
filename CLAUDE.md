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

## Google News Fingerprint-Update (bei neuer APK-Version)

### Wichtig: NIEMALS einfach ignorieren oder auf methodOrNull setzen!
Jede Fingerprint die nicht matcht muss durch die richtige neue Klasse ersetzt werden.
Fehlende Fingerprints bedeuten ungepatchte Code-Pfade → CustomTabs funktioniert nicht überall.

### Dateien die geändert werden müssen
1. `patches/src/main/kotlin/app/docbt/patched_up/googlenews/customtabs/Fingerprints.kt`
2. `patches/src/main/kotlin/app/docbt/patched_up/googlenews/customtabs/EnableCustomTabsPatch.kt` → `compatibleWith(...)` + `ref.definingClass`
3. `patches/src/main/kotlin/app/docbt/patched_up/googlenews/gms/GmsCoreSupportPatch.kt` → `compatibleWith(...)`
4. `patches-list.json` → beide Google News Patches auf neue Version
5. `versions.json` → `"googleNews": "NEUE_VERSION"`

### APK decompilieren
```bash
/tmp/jadx/bin/jadx -d /tmp/gn_decompiled <APK_DATEI>
# DEX-Dateien extrahieren:
mkdir /tmp/apk_dex && unzip <APK_DATEI> '*.dex' -d /tmp/apk_dex
```

### Klassen-Mapping Strategie
Alle obfuskierten Klassennamen ändern sich pro Version. Vorgehen:
1. `CustomTabsTrampolineActivity` → bleibt immer gleich (voller Klassenname)
2. Anker: `CustomTabsTrampolineActivity` hat Feld `public adwy a` → `adwy` = neue `Laecj`
3. `adwy` implementiert kein Interface. Felder: `public boolean j = false;` (das zu patchende Feld)
4. Browser-Factory (`Laecr`): Klasse mit `aqhn.a.get().g()` Ternary-Return → neue `LaecrFingerprint`
5. Disabled/Enabled Browser-Selector (`Laecu`/`Laecx`): Klassen mit `private final Context a` die `adxh` implementieren

### DEX-Bytecode Analyse für schwierige Klassen
Wenn eine Klasse in der Java-Decompilierung nicht gefunden wird, DEX direkt parsen:
```python
# Findet alle Methoden mit iget-boolean auf Ladwy;->j:Z in classes.dex
python3 << 'EOF'
import struct
# [Vollständiges Script in Session-History — suche nach "Scan classes.dex"]
EOF
```
Dieses Script gibt aus: `Lklassenname; -> methodname() @ instruction X`

### Klassen-Mapping 5.154.0 → 5.155.0 (als Referenz)
| Rolle | 5.154 | 5.155 |
|-------|-------|-------|
| CustomTabsArticleLauncher (boolean j) | `Laecj` | `Ladwy` |
| Browser-Factory (SGET Laqpb/Laqhn) | `Laecr` | `Ladxg` |
| Disabled Browser-Selector | `Laecu` | `Ladxj` |
| Enabled Browser-Selector | `Laecx` | `Ladxm` |
| Navigation Activity (classes.dex) | `Lajdk` | `Laixy` ← **NOCH NICHT COMMITTED** |
| ReadNow Handler (classes3.dex) | `Laedz` | `Ladyo` |
| Navigator (classes3.dex) | `Lajdr` | `Laiyf` |
| Click Handler (classes3.dex) | `Lajgz` | nicht gefunden (methodOrNull OK) |
| Click Handler (classes3.dex) | `Lajhk` | nicht gefunden (methodOrNull OK) |

### Offener Stand (Session unterbrochen)
- PR #20 ist FALSCH: `LajdkFingerprint` auf `methodOrNull` gesetzt — das ist NICHT die Lösung!
- Richtige neue Klasse für `Lajdk` ist **`Laixy`** (classes.dex, method `access$1301`, reads bei [1039, 1674])
- PR #20 muss durch einen neuen PR ersetzt werden, der `Laixy` als Klasse setzt UND `.method` beibehält
- `aixy.java` prüfen um die Klasse zu verstehen: es ist `StartActivity` (Navigation Activity)

### Nächste Schritte
1. PR #20 schließen (nicht mergen!)
2. Neuen Branch von main erstellen
3. In `Fingerprints.kt`: `LajdkFingerprint` → `classDef.type == "Laixy;"` setzen
4. In `EnableCustomTabsPatch.kt`: `LajdkFingerprint.method` (required) beibehalten
5. Commit + Push + PR → Squash Merge

---

## CI / Release-Prozess

- CI läuft automatisch bei jedem Push auf `main`
- `gradle-semantic-release-plugin` bestimmt die Version anhand von Conventional Commits
- `fix:` → Patch-Bump (z.B. 1.3.2 → 1.3.3)
- `feat:` → Minor-Bump (z.B. 1.3.x → 1.4.0)
- Das `.mpp` wird neu gebaut und auf GitHub Releases hochgeladen
- `patches-bundle.json` wird automatisch mit neuer `download_url` aktualisiert
- Morphe erkennt die neue Version anhand der `version`-Nummer in `patches-bundle.json`
