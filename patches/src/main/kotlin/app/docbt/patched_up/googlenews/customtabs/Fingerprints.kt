package app.docbt.patched_up.googlenews.customtabs

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.Opcode

// All methods in Google News v5.155.0 that read Ladwy;->j:Z (iget-boolean).
// Ladwy = CustomTabsArticleLauncher; field j controls CustomTabs (true) vs WebView (false).
//
// Verified read sites:
//   classes.dex:  Lajbx (click handler, method b)          [~70]
//   classes3.dex: Ladyo (ReadNow handler, method a)        [~29]
//                 Laiyf (navigator, method a)               [~60]
//                 CustomTabsTrampolineActivity (onCreate)   [~73]

internal object LajdkFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladwy;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajbx;" },
)

internal object LaedzFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladwy;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Ladyo;" },
)

internal object LajdrFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladwy;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Laiyf;" },
)

internal object LajgzFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladwy;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajgz;" },
)

internal object LajhkFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladwy;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef -> classDef.type == "Lajhk;" },
)

// CustomTabsTrampolineActivity.onCreate() reads Ladwy.j; if j==0 it logs
// "Unexpected intent; activity is not enabled" and finishes. Patch j-read to always true.
internal object CustomTabsTrampolineFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladwy;", "j", "Z", Opcode.IGET_BOOLEAN)),
    custom = { _, classDef ->
        classDef.type == "Lcom/google/apps/dots/android/modules/reading/customtabs/CustomTabsTrampolineActivity;"
    },
)

// adxg.a() returns an adxh implementation based on experiment flag aqhn.a.get().g().
// When the experiment is OFF, IF_EQZ branches to the null-returning adxh,
// which causes adwy.k=null → adwy.b() fails → CustomTabs never binds.
// Patching the IF_EQZ to NOP forces the always-enabled adxh path (returns real browser pkg).
internal object LaecrFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Laqhn;", "a", "Laqhn;", Opcode.SGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Ladxg;" },
)

// adxj.a() — "disabled" adxh: filters all installed browsers against an experiment allowlist,
// returns null if none match. IF_EQZ at [15] returns null when the filtered list is empty.
// NOP-ing it forces the fallback fa.a() call which picks the default browser via resolveActivity.
internal object LaecuFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladxj;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Ladxj;" },
)

// adxm.a() — "enabled" adxh: picks the best CT-supporting browser via fa.a(), then checks
// if it's in the experiment allowlist. IF_EQZ at [27] redirects to allowlist-only path when
// the default browser isn't allowlisted (e.g. Firefox/Brave). NOP-ing it always returns
// whatever fa.a() found (default browser has priority via resolveActivity).
internal object LaecxFingerprint : Fingerprint(
    filters = listOf(fieldAccess("Ladxm;", "a", "Landroid/content/Context;", Opcode.IGET_OBJECT)),
    custom = { _, classDef -> classDef.type == "Ladxm;" },
)
