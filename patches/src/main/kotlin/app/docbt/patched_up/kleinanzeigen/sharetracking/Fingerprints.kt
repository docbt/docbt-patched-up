package app.docbt.patched_up.kleinanzeigen.sharetracking

import app.morphe.patcher.Fingerprint

// e.h() in Lebk/ui/vip/compose/content/e — builds the share URL for VIP ad pages.
// Takes (Context, String, String, Z, Z) and returns the share URL string with UTM params.
// Switches utm_source based on target app: "facebook", "twitter", "whatsapp", "sharesheet" (default).
// "sharesheet" is the fallback UTM source and is a stable unique string across versions.
internal object ShareUrlBuilderFingerprint : Fingerprint(
    strings = listOf("sharesheet"),
    custom = { _, classDef -> classDef.type == "Lebk/ui/vip/compose/content/e;" },
)
