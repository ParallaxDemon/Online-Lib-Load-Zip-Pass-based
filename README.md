# Parallax online library loader

Two Android projects:

- `PARALLAX 🔥`: download a configured HTTPS ZIP, extract it with its password, then load `.so` files through `System.load`.
- `Parallax Papa ENC`: generate replacement encoded URL/password values for the Java constants in `ParallaxConfig.java`.

The loader URL and ZIP password are encoded constants directly in `PARALLAX 🔥/app/src/main/java/com/Parallax/Papa/ParallaxConfig.java`; no JSON asset or Actions secrets are read. The CI helper leaves these Java constants intact. The shared native code in `native/parallax_native.cpp` decodes the original PX/XOR/HEX values. This is **reversible encoding**, not protection of the URL or password in a built APK.

For exact DEX, native library and provider paths, see [LOADER_INTEGRATION.md](LOADER_INTEGRATION.md). The manifest provider opens a Java-only dialog with a fresh random color theme and starts loading when a host Activity becomes visible. AndroidManifest.xml is required provider registration metadata.

## Build

Run GitHub Actions **Build both Android APKs** and download the two debug APK artifacts and loader integration kit. Locally, with Android SDK, NDK 25.2.9519653, CMake 3.22.1, JDK 17 and Gradle 7.6.4 installed:

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
