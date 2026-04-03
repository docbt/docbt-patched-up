package app.docbt.patched_up.kleinanzeigen.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch

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
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides sponsored ads and Google Ads. Also disables Microsoft Clarity analytics.",
) {
    compatibleWith(COMPAT)

    execute {
        // Liberty init method initializes the ad/analytics SDK.
        // Returning early before execution prevents all ads and analytics from loading.
        LibertyInitFingerprint.method.addInstruction(0, "return-void")
    }
}
