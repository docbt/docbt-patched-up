package app.docbt.patched_up.kleinanzeigen.sharetracking

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.PatchException
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

// Anchor strings: appear together in the share-target switch statement.
// Using several co-occurring strings instead of just "sharesheet" avoids
// false-positive matches against unrelated code elsewhere in the merged
// multi-dex APK that might also contain the word "sharesheet" on its own.
private val ANCHOR_STRINGS = setOf("sharesheet", "com.whatsapp", "com.pinterest", "com.facebook.orca")

// True if any method in this class references all of ANCHOR_STRINGS
// (not necessarily the same method — they live in different switch
// branches of the same method in practice, but we don't rely on that).
private fun ClassDef.containsAnchorStrings(): Boolean {
    val remaining = ANCHOR_STRINGS.toMutableSet()
    for (method in methods) {
        val instructions = method.implementation?.instructions ?: continue
        for (instruction in instructions) {
            val ref = (instruction as? ReferenceInstruction)?.reference as? StringReference ?: continue
            remaining.remove(ref.string)
        }
        if (remaining.isEmpty()) return true
    }
    return remaining.isEmpty()
}

// e.i(url, source) — appends UTM params to a URL and returns the result.
// Class itself may be merged into an unrelated library namespace (e.g. OkHttp)
// by R8, and its name/package can change between versions — do not lock to type.
// Instead: identify the class via ANCHOR_STRINGS, then require the matched
// method to be the unique (String, String) -> String method in that class.
internal object ShareUrlParamBuilderFingerprint : Fingerprint(
    custom = { method, classDef ->
        val isRightShape =
            method.returnType == "Ljava/lang/String;" &&
            method.parameterTypes.size == 2 &&
            method.parameterTypes.all { it.toString() == "Ljava/lang/String;" }

        if (!isRightShape || !classDef.containsAnchorStrings()) {
            false
        } else {
            val shapeMatches = classDef.methods.filter { m ->
                m.returnType == "Ljava/lang/String;" &&
                m.parameterTypes.size == 2 &&
                m.parameterTypes.all { it.toString() == "Ljava/lang/String;" }
            }
            // The PatchException below is intentional and not just defensive boilerplate:
            // if a future app version adds a second (String, String) -> String method to
            // this class, we want the build to fail loudly with a clear message, rather
            // than silently patching whichever method happens to be listed first — the
            // exact class of bug that caused the original Hide Pur regression this patch
            // set was built to avoid repeating.
            if (shapeMatches.size != 1) {
                throw PatchException(
                    "Expected exactly 1 method matching (String, String) -> String in " +
                    "${classDef.type}, but found ${shapeMatches.size}" +
                    if (shapeMatches.isEmpty()) "." else ": ${shapeMatches.joinToString { it.name }}."
                )
            }
            true
        }
    },
)
