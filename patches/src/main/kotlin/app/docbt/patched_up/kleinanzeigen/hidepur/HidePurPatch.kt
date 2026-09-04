package app.docbt.patched_up.kleinanzeigen.hidepur

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.misc.resources.resourceMappingPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private val COMPAT = Compatibility(
    name = "Kleinanzeigen",
    packageName = "com.ebay.kleinanzeigen",
    appIconColor = 0x2EAD33,
    targets = listOf(AppTarget(version = "2026.36.0")),
)

@Suppress("unused")
val hidePurPatch = bytecodePatch(
    name = "Hide Pur",
    description = "Hides the Pur ad-free subscription option from the settings menu.",
) {
    compatibleWith(COMPAT)
    dependsOn(resourceMappingPatch)

    execute {
        HidePurEligibilityFingerprint.let {
            val method = it.method
            val instructions = method.implementation!!.instructions.toList()
            val literalIndex = it.instructionMatches[0].index

            // The Pur row is only appended inside `if (eligible) { list.add(...) }`.
            // Walk backward from the ka_gbl_pur reference to find that specific
            // gate, not forward from the top of the method.
            val branchIndex = (literalIndex downTo 0).first { i ->
                instructions[i].opcode == Opcode.IF_EQZ || instructions[i].opcode == Opcode.IF_NEZ
            }
            val gateReg = (instructions[branchIndex] as OneRegisterInstruction).registerA

            // Force the gate to always take the "ineligible" branch, regardless of
            // whether the compiler emitted IF_EQZ or IF_NEZ for this check.
            val forceValue = if (instructions[branchIndex].opcode == Opcode.IF_EQZ) "0x0" else "0x1"

            method.addInstructions(branchIndex, "const/4 v$gateReg, $forceValue")
        }
    }
}
