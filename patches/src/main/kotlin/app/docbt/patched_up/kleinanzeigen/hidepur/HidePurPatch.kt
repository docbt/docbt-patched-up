package app.docbt.patched_up.kleinanzeigen.hidepur

import app.morphe.patcher.extensions.InstructionExtensions
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
val hidePurPatch = bytecodePatch(
    name = "Hide Pur",
    description = "Hides the Pur ad-free subscription option from the settings menu.",
) {
    compatibleWith(COMPAT)

    execute {
        with(InstructionExtensions) {
            // setupSections() (2026.9.0) / q() (2026.12.0) in SettingsAndHelpFragment:
            // p7 = showAdFreeSubscription. The single IF_EQZ p7 sends to GONE when false,
            // VISIBLE when true. Forcing vReg = 0 before IF_EQZ always hides the Pur entry.
            val method = SetupSectionsPurFingerprint.method
            var ifEqzIndex = -1
            var showAdFreeReg = -1
            for ((i, instr) in method.implementation!!.instructions.withIndex()) {
                if (instr.opcode == Opcode.IF_EQZ) {
                    ifEqzIndex = i
                    showAdFreeReg = (instr as OneRegisterInstruction).registerA
                    break
                }
            }
            check(ifEqzIndex != -1) { "IF_EQZ not found in setupSections/q" }
            method.addInstruction(ifEqzIndex, "const/4 v$showAdFreeReg, 0x0")
        }
    }
}
