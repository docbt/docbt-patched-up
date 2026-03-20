<div align="center">

# docbt-patched-up

**Revived and updated patches for Morphe. Community-driven. GPL-compliant.**

<br>

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Built for Morphe](https://img.shields.io/badge/Built%20for-Morphe-1E5AA8?style=flat-square)](https://morphe.software)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=flat-square&logo=gradle)](https://gradle.org)
[![Patches](https://img.shields.io/badge/Patches-1%20App-success?style=flat-square)](#supported-apps--patches)

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=flat-square&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/docbt)

</div>

&nbsp;
## About

The goal of this project is to migrate existing patches to Morphe and keep them compatible with the latest version of the original apps. Regular updates — stay tuned.

### Supported Apps & Patches <a name="supported-apps--patches"></a>

| App | Package | Version | Patches |
|---|---|---|---|
| Google News | `com.google.android.apps.magazines` | [![Google News](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fraw.githubusercontent.com%2Fdocbt%2Fdocbt-patched-up%2Fmain%2Fversions.json&query=%24.googleNews&label=&color=blue&style=flat-square)](versions.json) | Custom Tabs, GMS Support, Extension |

&nbsp;
## How to use

Click here to add these patches to Morphe:
**https://morphe.software/add-source?github=docbt/docbt-patched-up**

Or manually add this URL as a patch source in Morphe:
**https://github.com/docbt/docbt-patched-up**

&nbsp;
## Contributing

Contributions are welcome. Please read the [contribution guidelines](CONTRIBUTING.md) before submitting a pull request.

&nbsp;
## Building

```bash
./gradlew :patches:buildAndroid
```

&nbsp;
## License

This project is licensed under the [GNU General Public License v3.0](LICENSE), with additional conditions under GPLv3 Section 7:

- **Attribution (7b):** The name **"docbt"** and original authorship must be preserved in all derivative works and forks. The notice in the [NOTICE](NOTICE) file may not be removed or altered.
- **Name Restriction (7c):** The name **"Morphe"** may not be used for derivative works. Derivatives must adopt a distinct identity unrelated to "Morphe."

See the [NOTICE](NOTICE) file for full Section 7 conditions.
