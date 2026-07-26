package app.docbt.patched_up.kleinanzeigen.hidepur

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.misc.resources.resourceMappingPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

private val COMPAT = Compatibility(
    name = "Kleinanzeigen",
    packageName = "com.ebay.kleinanzeigen",
    appIconColor = 0x2EAD33,
    targets = listOf(AppTarget(version = "2026.30.0")),
)

@Suppress("unused")
val hidePurPatch = bytecodePatch(
    name = "Hide Pur",
    description = "Hides the Pur ad-free subscription option from the settings menu.",
) {
    compatibleWith(COMPAT)

    // resourceMappingPatch is required whenever a fingerprint uses resourceLiteral(): this patch builds
    // the resource-name -> id lookup table (from public.xml) that the filter
    // reads at match time. Omitting this dependency leaves the table empty and
    // the fingerprint will fail to match with no obvious error pointing here.
    dependsOn(resourceMappingPatch)

    execute {
        SetupSectionsPurFingerprint.let {
            // instructionMatches[1] = the second filter above (the setVisibility
            // call for the Pur row specifically, not just any setVisibility call).
            val setVisibilityMatch = it.instructionMatches[1]
            val invoke = setVisibilityMatch.getInstruction<FiveRegisterInstruction>()
            val visibilityArgRegister = invoke.registerD // the `int` visibility arg register, not the View receiver

            // const/16, not const/4: const/4's literal is a signed 4-bit value
            // (range -8..7), and View.GONE = 8 overflows that by exactly one.
            // Using const/4 here compiles to invalid smali and fails silently
            // downstream as "Collection is empty" during instruction assembly.
            it.method.addInstructions(
                setVisibilityMatch.index,
                "const/16 v$visibilityArgRegister, 0x8", // View.GONE
            )
        }
    }
}
