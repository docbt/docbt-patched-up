package app.docbt.patched_up.kleinanzeigen.ads

import app.morphe.patcher.Fingerprint

// Liberty init method: initializes the ad/analytics SDK (Liberty SDK).
// "KEY_LIBERTY_REFRESH_INTERVAL" is a stable string inside the init method
// across versions: 2026.9.0 (KaLibertyConfig.initLiberty) and 2026.12.0 (liberty_config/a.c).
// The classDef.type.contains("/Liberty;") approach broke in 2026.12.0 due to obfuscation.
internal object LibertyInitFingerprint : Fingerprint(
    strings = listOf("KEY_LIBERTY_REFRESH_INTERVAL"),
)
