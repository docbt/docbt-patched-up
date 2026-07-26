package app.docbt.patched_up.kleinanzeigen.hidepur

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patches.all.misc.resources.ResourceType
import app.morphe.patches.all.misc.resources.resourceLiteral

// Resolves the Pur title string's ID at patch time from the current APK's
// own res/values/public.xml, instead of a fixed value which might change each APK release.
// So it self-corrects if aapt2 ever renumbers ka_gbl_pur between APK releases.
//
// filters[0] identifies the ROW (the Pur entry specifically, via its title
// resource); filters[1] identifies which of the ~14 setVisibility(I) calls
// in this method belongs to that row. Both are required — resourceLiteral
// alone only proves we're in the right method, not which instruction to patch.
internal object SetupSectionsPurFingerprint : Fingerprint(
    definingClass = "Lebk/ui/preferences/settings/settings_and_help/SettingsAndHelpFragment;",
    filters = listOf(
        resourceLiteral(ResourceType.STRING, "ka_gbl_pur"),
        methodCall(smali = "Landroid/view/View;->setVisibility(I)V"),
    ),
)
