# docbt-patched-up

Revived and updated patches for [Morphe](https://morphe.software). Brings back functionality that was lost due to app updates or abandoned patch sources.

&nbsp;
## About

This repository contains patches for Android apps, compatible with the [Morphe Patcher](https://morphe.software).
Patches are kept up to date and revived when upstream sources are no longer maintained.

### Patches

#### Google News / Magazines (`com.google.android.apps.magazines`)

| Patch | Description |
|---|---|
| Enable Custom Tabs | Opens articles in your default browser instead of the in-app reader |
| GmsCore Support | Enables Google login via MicroG on renamed-package installations |

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

Licensed under the [GNU General Public License v3.0](LICENSE), with additional conditions under GPLv3 Section 7:

- **Name Restriction (7c):** The name **"Morphe"** may not be used for derivative works.
  Derivatives must adopt a distinct identity unrelated to "Morphe."

See the [LICENSE](LICENSE) and [NOTICE](NOTICE) files for full details.
