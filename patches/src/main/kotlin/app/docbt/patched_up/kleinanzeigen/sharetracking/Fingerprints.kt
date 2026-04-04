package app.docbt.patched_up.kleinanzeigen.sharetracking

import app.morphe.patcher.Fingerprint

// e.h() — builds the VIP ad share URL, switches utm_source by target app.
// "sharesheet" is the stable default source string (used for generic share sheet).
internal object ShareUrlBuilderFingerprint : Fingerprint(
    strings = listOf("sharesheet"),
    custom = { _, classDef -> classDef.type == "Lebk/ui/vip/compose/content/e;" },
)

// e.i(url, source) — appends UTM params to a URL and returns the result.
// Called by h() for ad sharing and directly for store sharing.
// Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
// Identified by: same class as h(), returns String, takes exactly 2 String parameters.
internal object ShareUrlParamBuilderFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lebk/ui/vip/compose/content/e;" &&
        method.returnType == "Ljava/lang/String;" &&
        method.parameterTypes.size == 2 &&
        method.parameterTypes.all { it.toString() == "Ljava/lang/String;" }
    },
)
