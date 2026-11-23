# ParallaxAlwaysOP loader: Android host integration

Use this in an Android app you own and can build or re-sign. A DEX file alone is not enough: this loader also needs its matching native library, ZIP4J classes, the JSON asset and the Internet permission. It works in portrait or landscape; only the **demo launcher Activity** requests landscape. The host keeps its own orientation and theme.

## Obtain the integration kit

Open GitHub Actions → **Build both Android APKs** on the `upgrade/native-loader-ui-build` branch and download **Parallax-Loader-integration-kit**. Its contents are:

| Kit path | Destination in a built host APK | Purpose |
| --- | --- | --- |
| `classes.dex` | Merge the classes into the host's DEX set; add as `classesN.dex` only when the host supports multidex | `com.Parallax.Papa` loader classes **and ZIP4J**, excluding the demo Activity |
| `lib/arm64-v8a/libParallaxAlwaysOP.so` and the other `lib/<abi>/` variants | Same `lib/<abi>/` paths | JNI configuration codec and `.so` discovery |
| `assets/Parallax/ParallaxPapa.json` | Same `assets/Parallax/ParallaxPapa.json` path | Encoded HTTPS ZIP URL and password |

The kit already includes ZIP4J in `classes.dex`; do not duplicate ZIP4J when merging that DEX. The DEX contains `ParallaxInitProvider` and references the exact case-sensitive package `com.Parallax.Papa` and its JNI method names, so do not rename them. Do not overwrite the host's `classes.dex`; merge with Android D8/R8 or use a valid additional DEX in a multidex capable app. Adding a DEX alone does not register the startup provider.

## Preferred integration: Android Gradle source project

1. Copy only `ParallaxConfig.java`, `ParallaxCore.java`, `ParallaxCrypto.java`, `ParallaxDialog.java`, `ParallaxDownloader.java`, `ParallaxLoader.java`, `ParallaxZip.java` and `ParallaxInitProvider.java` from `PARALLAX 🔥/app/src/main/java/com/Parallax/Papa/` into the host's `app/src/main/java/com/Parallax/Papa/`. Do not copy the demo `MainActivity.java`.
2. Add the bundled `PARALLAX 🔥/app/libs/Zip4java.jar` as the host's `app/libs/Zip4java.jar` and add `implementation files('libs/Zip4java.jar')` to the host module's Gradle dependencies. Skip this step if you use the prebuilt integration DEX, which already contains ZIP4J.
3. Build `native/parallax_native.cpp` via the sample CMake project or copy the `.so` for each ABI from the kit to `app/src/main/jniLibs/<abi>/libParallaxAlwaysOP.so`. Keep the JNI source package and `System.loadLibrary("ParallaxAlwaysOP")` in sync.
4. Generate JSON with the **Parallax Config** APK. Put it at `app/src/main/assets/Parallax/ParallaxPapa.json`. It has the keys `url` and `password` in the existing PX/XOR/HEX format. Use an HTTPS URL to a password protected ZIP containing the `.so` files to load. The remote ZIP must contain libraries compatible with the running device ABI; the SDK's own `libParallaxAlwaysOP.so` belongs in `jniLibs`, not in that ZIP.
5. Add the Internet permission and the provider to the host's `app/src/main/AndroidManifest.xml` (replace a conflicting authority if you already have a provider at that URI):

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application>
        <provider
            android:name="com.Parallax.Papa.ParallaxInitProvider"
            android:authorities="${applicationId}.parallax.loader.init"
            android:exported="false" />
    </application>
</manifest>
```

Merge these entries into the **existing** host manifest. `${applicationId}` resolves during a Gradle build; for an already built APK replace it with the host's real application ID, for example `com.example.game.parallax.loader.init`. The provider runs in the default process and registers Activity lifecycle callbacks. On the first visible Activity in each foreground session, it opens the loader dialog and begins download/extraction/loading automatically. There is **no `onCreate` call** to add to a host Activity. The standalone demo Activity requests landscape; the provider never changes a host Activity's orientation. Keep the provider nonexported and avoid placing it in a separate process.

The loader dialog is constructed entirely in Java. Each opening chooses a random color theme and avoids repeating any of the previous 100 hue choices within a process. It has no action buttons; it closes about 2.5 seconds after completion. For an explicit extra opening from a live Activity, `ParallaxDialog.show(activity)` remains available. Code that depends on the downloaded `.so` must wait for the asynchronous load to finish; the provider starts it automatically but cannot make it synchronous with host startup.

The **AndroidManifest.xml is required by Android** for the provider. It is registration metadata, not UI theme or layout XML. The loader uses no resource XML; the separate config generator is its own app.

## Integrating into an already built APK

Place kit `classes.dex` into the APK's DEX set with a proper DEX merge or a multidex aware additional DEX, put the native files and JSON in the paths shown in the table, ensure the manifest grants Internet and declares the nonexported `ParallaxInitProvider` with the host application ID as its authority. Rebuild and sign the resulting APK with a key you control. Modifying an APK changes its signature; an update can only replace an installed app when it uses the same signing identity. You need access to the host's build/integration process to merge DEX and register the provider; the loader cannot attach itself to an arbitrary app without that work.

## Compatibility and checks

- API 19+ Java API; the demo APK targets 33. Host class loaders, native linker namespaces, ABI availability and app lifecycle can affect integration. The kit cannot guarantee compatibility with every APK.
- If the dialog says `Configuration unavailable`, `ParallaxConfig.load()` could not open or parse `assets/Parallax/ParallaxPapa.json` in the process that loaded the DEX. Check the exact path and case, make sure the file is in the base APK that process uses, and read `adb logcat -s ParallaxConfig:E` for the swallowed exception. Do not call `ParallaxCore.init()` with a Context from another package or process; the provider starts loading automatically, and any manual call should use the host's own `activity.getApplicationContext()`.
- When merging into an already built APK, `${applicationId}` is never substituted. Replace it with the real package id, for example `com.pubg.imobile.parallax.loader.init`, before rebuilding and signing. An unresolved placeholder authority is a common integration error.
- If the dialog says `Server says File not found`, the configured URL returned that message even though its HTTP status was 200; generate a fresh JSON pointing to a direct ZIP. Other download errors show HTTP status, timeout, redirect, ZIP format or save failures. Archive size is limited to 100 MiB. If extraction fails, check the ZIP password. If no compatible library loads, check ABI and dependencies with `adb logcat -s ParallaxLoader ParallaxCore`.
- Only load ZIPs from a trusted server. PX/XOR/HEX is reversible encoding, not protection of the URL or password. Keep a stable signing key for distributable releases; the demo release workflow signs with a temporary key.

The bundled config now points at the `payload/out/ParallaxPayload.zip` archive in this repository through `raw.githubusercontent.com`; it contains a demo arm64 `.so` and uses password `1234`. The old `parallaxserver.online/...libOWNERHUBEE.zip` endpoint returned HTTP 200 with `File not found`, which the downloader reports as `Server says File not found; update the archive URL`. Replace the JSON asset with a new URL and password for your own payload. The downloader checks ZIP signature before extraction, follows up to five HTTPS redirects and keeps the previous payload when a download fails. If the archive is removed or moved later, generate and package another config. The URL and password are reversible from the APK: do not treat the JSON as a secret.
