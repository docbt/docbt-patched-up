package app.docbt.patched_up.kleinanzeigen.hidepur

import app.morphe.patcher.Fingerprint

// setupSections (2026.9.0) / q (2026.12.0) in SettingsAndHelpFragment:
// parameter p7 = showAdFreeSubscription (bool). The single IF_EQZ p7 controls
// whether the Pur section container is VISIBLE or GONE.
// SettingsAndHelpFragment is never obfuscated — stable across versions.
internal object SetupSectionsPurFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lebk/ui/preferences/settings/settings_and_help/SettingsAndHelpFragment;" &&
        !method.name.startsWith("access\$") &&
        method.parameterTypes.count { it == "Z" } >= 5 &&
        method.parameterTypes.any { it == "Ljava/lang/String;" }
    },
)
