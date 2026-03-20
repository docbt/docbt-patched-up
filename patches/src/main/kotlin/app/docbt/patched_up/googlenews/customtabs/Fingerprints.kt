package app.docbt.patched_up.googlenews.customtabs

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.Opcode

// All methods in Google News v5.154.0 that read Laecj;->j:Z (iget-boolean).
// Laecj = CustomTabsArticleLauncher; field j controls CustomTabs (true) vs WebView (false).
//
// Verified read sites:
//   classes.dex:  Lajdk (navigation activity, method E)    [516, 833]
//   classes3.dex: Laedz (ReadNow handler, method a)        [12]
//                 Lajdr (navigator, method a)               [16]
//                 Lajgz (click handler, method a)           [59]
//                 Lajhk (click handler, method a)           [26]
//                 CustomTabsTrampolineActivity (onCreate)   [24]

internal object LajdkFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laecj;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajdk;" },
)

internal object LaedzFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laecj;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Laedz;" },
)

internal object LajdrFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laecj;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajdr;" },
)

internal object LajgzFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laecj;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajgz;" },
)

internal object LajhkFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laecj;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajhk;" },
)

// CustomTabsTrampolineActivity.onCreate() reads Laecj.j at [24]; if j==0 it logs
// "Unexpected intent; activity is not enabled" and finishes. Patch j-read to always true.
internal object CustomTabsTrampolineFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laecj;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef ->
        classDef.type == "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsTrampolineActivity;"
    },
)

// Laecr.b() returns a Laecs implementation based on experiment flag Laqpb.a().g().
// When the experiment is OFF, IF_EQZ at [5] branches to the null-returning Laecs,
// which causes Laecj.k=null → Laecj.b() fails → CustomTabs never binds.
// Patching the IF_EQZ to NOP forces the always-enabled Laecs path (returns real browser pkg).
internal object LaecrFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laqpb;", "a", "Laqpb;", Opcode.SGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Laecr;" },
)

// Laecu.a() — "disabled" Laecs: filters all installed browsers against an experiment allowlist,
// returns null if none match. IF_EQZ at [15] returns null when the filtered list is empty.
// NOP-ing it forces the fallback Lgh.a() call which picks the default browser via resolveActivity.
internal object LaecuFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laecu;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Laecu;" },
)

// Laecx.a() — "enabled" Laecs: picks the best CT-supporting browser via Lgh.a(), then checks
// if it's in the experiment allowlist. IF_EQZ at [27] redirects to allowlist-only path when
// the default browser isn't allowlisted (e.g. Firefox/Brave). NOP-ing it always returns
// whatever Lgh.a() found (default browser has priority via resolveActivity).
internal object LaecxFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laecx;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Laecx;" },
)
