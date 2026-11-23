# Parallax online library loader

Two Android projects:

- `PARALLAX 🔥`: download a configured HTTPS ZIP, extract it with its password, then load `.so` files through `System.load`.
- `Parallax Papa ENC`: create the matching `ParallaxPapa.json` configuration from an HTTPS URL and ZIP password.

The generated JSON goes into `PARALLAX 🔥/app/src/main/assets/Parallax/ParallaxPapa.json`. The native code in `native/parallax_native.cpp` retains the original PX + XOR + HEX configuration format so existing configuration files remain readable. This format **does not protect secrets**: anyone with the APK can recover the URL and password. Use a trusted HTTPS endpoint and protect the archive distribution server separately.

For exact DEX, JNI, asset and host integration steps see [LOADER_INTEGRATION.md](LOADER_INTEGRATION.md). The loader has a Java-only dialog with a fresh random color theme each opening. A manifest provider displays it when a host Activity becomes visible and starts loading automatically. AndroidManifest.xml remains required provider metadata.

## Use the loader in another Android app

Copy `com/Parallax/Papa` classes into the host app's DEX, include the ZIP4J dependency (`app/libs/Zip4java.jar`), the asset JSON and the `libParallaxAlwaysOP.so` for each target ABI, and grant `android.permission.INTERNET`. Register `ParallaxInitProvider` in the host manifest as shown in the integration guide; it displays the dialog automatically when an Activity becomes visible. `ParallaxCore.init(context, listener)` remains available to receive progress and `ParallaxCore.reload(context)` downloads again. The provider has no dependency on the demo activity. Keep the original package names because the native JNI symbols refer to them.

An arbitrary DEX alone cannot use native methods: the matching `libParallaxAlwaysOP.so` must also be packaged. Loading `.so` files also depends on ABI, Android linker behavior and library dependencies. The demo targets Android API 19+ and builds the native component for the default Android ABIs.

## Build

Run GitHub Actions **Build both Android APKs**, then download the two debug APK artifacts from the workflow run. Locally, with Android SDK, NDK 25.2.9519653, CMake 3.22.1, JDK 17 and Gradle 7.6.4 installed:

```sh
mkdir -p build-src
cp -a 'PARALLAX 🔥' build-src/Project
cp -a native build-src/native
gradle -p build-src/Project :app:assembleDebug
rm -rf build-src/Project
cp -a 'Parallax Papa ENC' build-src/Project
gradle -p build-src/Project :app:assembleDebug
```

Debug APKs are for testing. Public release builds require your own signing key. Do not commit signing credentials.

## Signed release builds

The **Build signed release APKs** workflow creates two installable release APK artifacts. It signs each with a temporary CI key that is discarded after the job. This makes the APKs suitable for installation and testing, but a future build cannot update an installed APK without the same signing key. For production distribution, supply and retain your own signing key and configure the release workflow to use it.
