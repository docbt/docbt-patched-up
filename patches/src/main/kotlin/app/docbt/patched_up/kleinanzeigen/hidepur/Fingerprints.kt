package app.docbt.patched_up.kleinanzeigen.hidepur

import app.morphe.patcher.Fingerprint
import app.morphe.patches.all.misc.resources.ResourceType
import app.morphe.patches.all.misc.resources.resourceLiteral

// Anchors on the ka_gbl_pur string resource to locate the eligibility check
// that gates whether Pur is added to the Compose settings list. No class
// constraint: the containing class is an R8-merged utility with no stable name.
internal object HidePurEligibilityFingerprint : Fingerprint(
    filters = listOf(
        resourceLiteral(ResourceType.STRING, "ka_gbl_pur"),
    ),
)
