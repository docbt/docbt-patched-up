package app.docbt.patched_up.googlenews.customtabs

import app.morphe.patcher.extensions.InstructionExtensions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val enableCustomTabsPatch = bytecodePatch(
    name = "Enable CustomTabs",
    description = "Enables CustomTabs to open articles in your default browser.",
) {
    compatibleWith("com.google.android.apps.magazines" to setOf("5.154.0.880997081"))

    execute {
        with(InstructionExtensions) {
            // Step 1: Patch Laecr.b() — replace IF_EQZ experiment flag check with NOP.
            // Laecr.b() normally returns null-browser Laecs when the experiment is OFF,
            // causing Laecj.k=null and binding failure. With NOP, it always returns the
            // enabled Laecs (Laecx), which uses PackageManager to find a CT-supporting browser.
            val laecrMethod = LaecrFingerprint.method
            var ifEqzIndex = -1
            for ((i, instr) in laecrMethod.implementation!!.instructions.withIndex()) {
                if (instr.opcode == Opcode.IF_EQZ) {
                    ifEqzIndex = i
                    break
                }
            }
            check(ifEqzIndex != -1) { "IF_EQZ not found in Laecr.b()" }
            laecrMethod.replaceInstruction(ifEqzIndex, "nop")

            // Step 2: Bypass experiment allowlist in Laecx.a() (enabled Laecs).
            // Laecx.a() picks the best CT-supporting browser via Lgh.a() (which gives priority
            // to the Android default browser via resolveActivity), then checks if it is in
            // the experiment allowlist. Non-Chrome browsers are not in that list.
            // Instruction [27] IF_EQZ: if browser not in allowlist → redirects to allowlist-only path.
            // NOP-ing [27] makes it always return whatever Lgh.a() found.
            LaecxFingerprint.methodOrNull?.replaceInstruction(27, "nop")

            // Step 3: Bypass allowlist in Laecu.a() (disabled Laecs, belt+suspenders).
            // Laecu.a() filters installed browsers against the allowlist first.
            // Instruction [15] IF_EQZ: if filtered list is empty → returns null.
            // NOP-ing it always attempts Lgh.a() so the default browser is tried.
            LaecuFingerprint.methodOrNull?.replaceInstruction(15, "nop")

            // Step 4: In every method that reads Laecj.j, replace iget-boolean Laecj;->j:Z
            // with const/4 vX, 0x1 so the CustomTabs branch is always taken.
            // Covers click handlers, navigation, ReadNow, and CustomTabsTrampolineActivity.
            val methods = listOf(
                LajdkFingerprint.method,
                LaedzFingerprint.methodOrNull,
                LajdrFingerprint.methodOrNull,
                LajgzFingerprint.methodOrNull,
                LajhkFingerprint.methodOrNull,
                CustomTabsTrampolineFingerprint.methodOrNull,
            ).filterNotNull()

            for (method in methods) {
                val targets = mutableListOf<Int>()
                var index = 0
                for (instr in method.implementation!!.instructions) {
                    if (instr.opcode == Opcode.IGET_BOOLEAN) {
                        val ref = (instr as ReferenceInstruction).reference
                        if (ref is FieldReference && ref.definingClass == "Laecj;" && ref.name == "j") {
                            targets.add(index)
                        }
                    }
                    index++
                }
                for (i in targets) {
                    val reg = method.getInstruction<OneRegisterInstruction>(i).registerA
                    method.replaceInstruction(i, "const/4 v$reg, 0x1")
                }
            }
        }
    }
}
