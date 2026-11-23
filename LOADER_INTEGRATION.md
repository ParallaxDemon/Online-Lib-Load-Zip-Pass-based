# ParallaxAlwaysOP loader: Android host integration

Use this in an Android app you own and can build or re-sign. A DEX file alone is not enough: this loader also needs its matching native library, ZIP4J classes, the JSON asset and the Internet permission. It works in portrait or landscape; only the **demo launcher Activity** requests landscape. The host keeps its own orientation and theme.

## Obtain the integration kit

Open GitHub Actions → **Build both Android APKs** on the `upgrade/native-loader-ui-build` branch and download **Parallax-Loader-integration-kit**. Its contents are:

| Kit path | Destination in a built host APK | Purpose |
| --- | --- | --- |
| `classes.dex` | Merge the classes into the host's DEX set; add as `classesN.dex` only when the host supports multidex | `com.Parallax.Papa` loader classes **and ZIP4J**, excluding the demo Activity |
| `lib/arm64-v8a/libParallaxAlwaysOP.so` and the other `lib/<abi>/` variants | Same `lib/<abi>/` paths | JNI configuration codec and `.so` discovery |
| `assets/Parallax/ParallaxPapa.json` | Same `assets/Parallax/ParallaxPapa.json` path | Encoded HTTPS ZIP URL and password |

The kit already includes ZIP4J in `classes.dex`; don't duplicate ZIP4J when merging that DEX. The DEX references the exact case-sensitive package `com.Parallax.Papa` and the JNI method names, so do not rename the classes or package. Do not overwrite the host's existing `classes.dex`; merge with Android D8/R8 or use a valid additional DEX in a multidex capable app. Adding a DEX file to a ZIP without arranging its entry call will not start the loader.

## Preferred integration: Android Gradle source project

1. Copy only `ParallaxConfig.java`, `ParallaxCore.java`, `ParallaxCrypto.java`, `ParallaxDialog.java`, `ParallaxDownloader.java`, `ParallaxLoader.java` and `ParallaxZip.java` from `PARALLAX 🔥/app/src/main/java/com/Parallax/Papa/` into the host's `app/src/main/java/com/Parallax/Papa/`. Do not copy the demo `MainActivity.java`.
2. Add the bundled `PARALLAX 🔥/app/libs/Zip4java.jar` as the host's `app/libs/Zip4java.jar` and add `implementation files('libs/Zip4java.jar')` to the host module's Gradle dependencies. Skip this step if you use the prebuilt integration DEX, which already contains ZIP4J.
3. Build `native/parallax_native.cpp` via the sample CMake project or copy the `.so` for each ABI from the kit to `app/src/main/jniLibs/<abi>/libParallaxAlwaysOP.so`. Keep the JNI source package and `System.loadLibrary("ParallaxAlwaysOP")` in sync.
4. Generate JSON with the **Parallax Config** APK. Put it at `app/src/main/assets/Parallax/ParallaxPapa.json`. It has the keys `url` and `password` in the existing PX/XOR/HEX format. Use an HTTPS URL to a password protected ZIP containing the `.so` files to load. The remote ZIP must contain libraries compatible with the running device ABI; the SDK's own `libParallaxAlwaysOP.so` belongs in `jniLibs`, not in that ZIP.
5. Add `<uses-permission android:name="android.permission.INTERNET"/>` outside `<application>` in the host manifest. On Android 12+, any host launcher Activity with an intent filter must declare `android:exported`. Do not copy the demo Activity or its landscape setting.
6. From a **live Activity** on its main thread, after `super.onCreate(...)`, call:

```java
ParallaxDialog.show(this);
```

The dialog automatically downloads, extracts and loads on opening (including a repeat launch). It shows progress and closes about 2.5 seconds after completion. It is about 540 dp wide on large screens and scrolls on short landscape screens. It does not require host `R` resources. To run without a dialog, call `ParallaxCore.init(this, (message, finished) -> { /* update UI */ });` or `ParallaxCore.reload(this, listener)`. These operations download and load asynchronously. Wait for `finished` and inspect the message before starting code that depends on the downloaded library.

## Integrating into an already built APK

Place kit `classes.dex` into the APK's DEX set with a proper DEX merge or a multidex aware additional DEX, put the native files and JSON in the paths shown in the table, ensure the manifest grants Internet, and add a call to `ParallaxDialog.show(activity)` from a suitable Activity. Rebuild and sign the resulting APK with a key you control. Modifying an APK changes its signature; an update can only replace an installed app when it uses the same signing identity. You need access to the host's build/integration process to make the entry call; the loader cannot automatically attach itself to an arbitrary app.

## Compatibility and checks

- API 19+ Java API; the demo APK targets 33. Host class loaders, native linker namespaces, ABI availability and app lifecycle can affect integration. The kit cannot guarantee compatibility with every APK.
- If the dialog reports download failure, check HTTPS, server response and size (100 MiB maximum). If extraction fails, check the ZIP password. If no compatible library loads, check ABI and dependencies with `adb logcat -s ParallaxLoader ParallaxCore`.
- Only load ZIPs from a trusted server. PX/XOR/HEX is reversible encoding, not protection of the URL or password. Keep a stable signing key for distributable releases; the demo release workflow signs with a temporary key.
