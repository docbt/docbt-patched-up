// Original source: https://github.com/ReVanced/revanced-patches (GPL v3)
// Ported via https://github.com/hoo-dles/morphe-patches
package app.docbt.patched_up.all.misc.packagename

import app.morphe.patcher.patch.Option
import app.morphe.patcher.patch.OptionException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.util.logging.Logger

lateinit var packageNameOption: Option<String>

private fun NodeList.elements(): List<Element> =
    (0 until length).map { item(it) as Element }

/**
 * Set the package name to use.
 * If this is called multiple times, the first call will set the package name.
 *
 * @param fallbackPackageName The package name to use if the user has not already specified a package name.
 * @return The package name that was set.
 * @throws OptionException.ValueValidationException If the package name is invalid.
 */
fun setOrGetFallbackPackageName(fallbackPackageName: String): String {
    val packageName = packageNameOption.value!!

    return if (packageName == packageNameOption.default) {
        fallbackPackageName.also { packageNameOption.value = it }
    } else {
        packageName
    }
}

val changePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Appends \".morphe\" to the package name by default. " +
            "Changing the package name of the app can lead to unexpected issues.",
) {
    packageNameOption = stringOption(
        key = "packageName",
        default = "Default",
        values = mapOf("Default" to "Default"),
        title = "Package name",
        description = "The name of the package to rename the app to.",
        required = true,
    ) { value ->
        value == "Default" || value!!.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$"))
    }

    val updatePermissions by booleanOption(
        key = "updatePermissions",
        default = false,
        title = "Update permissions",
        description = "Update compatibility receiver permissions. " +
                "Enabling this can fix installation errors, but this can also break features in certain apps.",
    )

    val updateProviders by booleanOption(
        key = "updateProviders",
        default = false,
        title = "Update providers",
        description = "Update provider names declared by the app. " +
                "Enabling this can fix installation errors, but this can also break features in certain apps.",
    )

    finalize {
        val incompatibleAppPackages = setOf(
            "com.reddit.frontpage",
            "com.duolingo",
            "com.twitter.android",
            "tv.twitch.android.app",
        )

        document("AndroidManifest.xml").use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0) as Element
            val packageName = manifest.getAttribute("package")

            if (incompatibleAppPackages.contains(packageName)) {
                return@finalize Logger.getLogger(this::class.java.name).severe(
                    "'$packageName' does not work correctly with \"Change package name\"",
                )
            }

            val replacementPackageName = packageNameOption.value
            val newPackageName = if (replacementPackageName != packageNameOption.default) {
                replacementPackageName!!
            } else {
                "$packageName.morphe"
            }

            manifest.setAttribute("package", newPackageName)

            if (updatePermissions == true) {
                val receiverNotExported = "DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
                val androidName = "android:name"
                val oldName = "$packageName.$receiverNotExported"
                val newName = "$newPackageName.$receiverNotExported"

                (document.getElementsByTagName("permission").elements() +
                    document.getElementsByTagName("uses-permission").elements())
                    .filter { it.getAttribute(androidName) == oldName }
                    .forEach { it.setAttribute(androidName, newName) }
            }

            if (updateProviders == true) {
                val androidAuthority = "android:authorities"
                val authorityPrefix = "$packageName."

                document.getElementsByTagName("provider").elements().forEach { provider ->
                    val authorities = provider.getAttribute(androidAuthority)
                    if (authorities.startsWith(authorityPrefix)) {
                        provider.setAttribute(
                            androidAuthority,
                            authorities.replace(packageName, newPackageName)
                        )
                    }
                }
            }
        }
    }
}
