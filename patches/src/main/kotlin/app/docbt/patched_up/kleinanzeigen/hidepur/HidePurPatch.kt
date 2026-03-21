package app.docbt.patched_up.kleinanzeigen.hidepur

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val hidePurPatch = bytecodePatch(
    name = "Hide Pur",
    description = "Hides the Pur ad-free subscription option from the settings menu.",
) {
    compatibleWith("com.ebay.kleinanzeigen" to setOf("2016.9.0"))

    execute {
        // getShowAdFreeSubscription() returns a boolean controlling Pur visibility.
        // Returning false always hides the subscription entry.
        ShowAdFreeSubscriptionFingerprint.method.let { method ->
            method.addInstruction(0, "return v0")
            method.addInstruction(0, "const/4 v0, 0x0")
        }
    }
}
