package app.docbt.patched_up.kleinanzeigen.sharetracking

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private val COMPAT = Compatibility(
    name = "Kleinanzeigen",
    packageName = "com.ebay.kleinanzeigen",
    appIconColor = 0x2EAD33,
    targets = listOf(
        AppTarget(version = "2026.14.2"),
        AppTarget(version = "2026.14.0"),
    ),
)

@Suppress("unused")
val removeTrackingParamsPatch = bytecodePatch(
    name = "Remove tracking parameters from share URLs",
    description = "Strips UTM tracking parameters from URLs shared via the in-app share function.",
) {
    compatibleWith(COMPAT)

    execute {
        with(InstructionExtensions) {
            val method = ShareUrlBuilderFingerprint.method
            val instructions = method.implementation!!.instructions

            // Find all return-object instructions in reverse order so that inserting
            // instructions before each one does not shift the indices of later ones.
            val returnIndices = instructions
                .withIndex()
                .filter { (_, instr) -> instr.opcode == Opcode.RETURN_OBJECT }
                .map { (i, instr) -> i to (instr as OneRegisterInstruction).registerA }
                .toList()
                .reversed()

            for ((index, reg) in returnIndices) {
                // Strip UTM params from the URL before returning.
                // Uri.parse(url).buildUpon().clearQuery().build().toString()
                // Each addInstruction(index, ...) inserts at `index`, pushing previous
                // insertions down — so we add them in reverse order of desired execution.
                method.addInstruction(index, "move-result-object v$reg")
                method.addInstruction(index, "invoke-virtual {v$reg}, Landroid/net/Uri;.toString:()Ljava/lang/String;")
                method.addInstruction(index, "move-result-object v$reg")
                method.addInstruction(index, "invoke-virtual {v$reg}, Landroid/net/Uri\$Builder;.build:()Landroid/net/Uri;")
                method.addInstruction(index, "move-result-object v$reg")
                method.addInstruction(index, "invoke-virtual {v$reg}, Landroid/net/Uri\$Builder;.clearQuery:()Landroid/net/Uri\$Builder;")
                method.addInstruction(index, "move-result-object v$reg")
                method.addInstruction(index, "invoke-virtual {v$reg}, Landroid/net/Uri;.buildUpon:()Landroid/net/Uri\$Builder;")
                method.addInstruction(index, "move-result-object v$reg")
                method.addInstruction(index, "invoke-static {v$reg}, Landroid/net/Uri;.parse:(Ljava/lang/String;)Landroid/net/Uri;")
            }
        }
    }
}
