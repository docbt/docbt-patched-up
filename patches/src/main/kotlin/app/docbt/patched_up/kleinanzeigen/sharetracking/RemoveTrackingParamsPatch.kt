package app.docbt.patched_up.kleinanzeigen.sharetracking

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
val removeTrackingParamsPatch = bytecodePatch(
    name = "Remove tracking parameters from share URLs",
    description = "Strips UTM tracking parameters from URLs shared via the in-app share function.",
) {
    compatibleWith(COMPAT)

    execute {
        // e.i(url, source) appends UTM params to the URL and returns it.
        // Returning p0 (the first argument = the original URL) immediately
        // skips the UTM construction entirely.
        // This covers both ad sharing (called from e.h()) and store sharing (direct call).
        ShareUrlParamBuilderFingerprint.method.addInstruction(0, "return-object p0")
    }
}
