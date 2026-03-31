package app.docbt.patched_up.kleinanzeigen.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides sponsored ads and Google Ads. Also disables Microsoft Clarity analytics.",
) {
    compatibleWith("com.ebay.kleinanzeigen" to setOf("2026.14.0"))

    execute {
        // Liberty init method initializes the ad/analytics SDK.
        // Returning early before execution prevents all ads and analytics from loading.
        LibertyInitFingerprint.method.addInstruction(0, "return-void")
    }
}
