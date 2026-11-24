# ParallaxAlwaysOP: DEX को अपने Android APK में जोड़ना

Loader का URL और ZIP password `ParallaxConfig.java` में encoded PX/XOR/HEX constants हैं। Build workflow इन Java constants को ज्यों का त्यों रखता है; Actions secrets और `Parallax/ParallaxPapa.json` asset की जरूरत नहीं है। Native codec इन्हें decode करता है। Encoding reversible है: APK/DEX पाने वाला व्यक्ति URL और password निकाल सकता है। इस source को build करके नया APK/kit DEX लें; पुराना `TEST-CONFIG` APK invalid configuration दिखाएगा।

## Integration kit कहाँ से लें

**Build both Android APKs** workflow का `Parallax-Loader-integration-kit` artifact खोलें। Kit में `classes.dex`, `lib/<abi>/libParallaxAlwaysOP.so` और यह guide होते हैं। `classes.dex` में loader और ZIP4J दोनों शामिल हैं, demo `MainActivity` शामिल नहीं है।

| Kit file | अपने APK / source project में जगह |
| --- | --- |
| `classes.dex` | APK की root DEX set में सही तरह merge करें। Existing `classes.dex` overwrite न करें। Multidex सक्षम app में अतिरिक्त `classesN.dex` संभव है। |
| `lib/arm64-v8a/libParallaxAlwaysOP.so` | APK में `lib/arm64-v8a/libParallaxAlwaysOP.so`; source project में `app/src/main/jniLibs/arm64-v8a/libParallaxAlwaysOP.so`। बाकी ABIs के लिए उनकी समान path। |
| `LOADER_INTEGRATION.md` | Setup reference; APK में डालने की जरूरत नहीं। |

Downloaded `libDARK.so` ARM64 ELF है। उसे केवल 64-bit ARM process में load किया जा सकता है; SDK की `libParallaxAlwaysOP.so` के दूसरे ABI variants होने से `libDARK.so` दूसरे ABI पर नहीं चलेगा। एक DEX को किसी APK में रख देने से वह अपने आप शुरू नहीं होता।

## Source project से जोड़ना (recommended)

1. `PARALLAX 🔥/app/src/main/java/com/Parallax/Papa/` से `ParallaxConfig`, `ParallaxCore`, `ParallaxCrypto`, `ParallaxDialog`, `ParallaxDownloader`, `ParallaxLoader`, `ParallaxZip` और `ParallaxInitProvider` Java classes host के `app/src/main/java/com/Parallax/Papa/` में जोड़ें। Demo `MainActivity` न जोड़ें। Package spelling/case न बदलें; JNI names इसी package पर निर्भर हैं।
2. `PARALLAX 🔥/app/libs/Zip4java.jar` host के `app/libs/` में रखें और host Gradle में `implementation files('libs/Zip4java.jar')` जोड़ें। Prebuilt kit DEX लेने पर ZIP4J अलग से न जोड़ें।
3. Kit की `libParallaxAlwaysOP.so` files को प्रत्येक ABI की `app/src/main/jniLibs/<abi>/` path में रखें या repo के `native/parallax_native.cpp` से compile करें।
4. Host के मौजूदा `AndroidManifest.xml` में Internet permission और provider जोड़ें:

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

ऊपर दिए entries host के **मौजूदा** manifest में merge करें; नया अलग manifest न लगाएँ। Gradle `${applicationId}` को host package से बदल देता है। Already built APK edit करते समय authority में असली application ID लिखें। Provider को अलग process में न डालें। Android manifest registration जरूरी है; UI और रंग Java से बनते हैं।

5. App की पहली visible Activity पर provider अपने आप dialog दिखाता है, ZIP download/extract करता है और `.so` load करता है। Host Activity के `onCreate` में कोई call जरूरी नहीं है। Library की जरूरत वाले host code को loader पूरा होने तक इंतजार करना होगा; provider app startup रोकता नहीं है। यदि आप खुद dialog खोलना चाहते हैं तो live Activity से `ParallaxDialog.show(activity)` call कर सकते हैं।

## Built APK में DEX डालते समय

Host के original DEX को बचाएँ। D8/R8 से loader classes merge करें या host की multidex व्यवस्था में नया `classesN.dex` जोड़ें। ZIP4J पहले से kit DEX में है। ऊपर की ABI paths में native files डालें, host manifest में permission/provider merge करें, फिर APK rebuild और अपनी signing key से re-sign करें। केवल classes.dex copy करके या provider जोड़कर config asset missing error ठीक नहीं होगा यदि आप पुराने DEX का उपयोग कर रहे हैं: **नए workflow का kit DEX** इस्तेमाल करें जिसमें build time config है। `Parallax/ParallaxPapa.json` कहीं रखने की जरूरत नहीं है।

## Check और troubleshooting

- नए build में APK/kit DEX की `ParallaxConfig` class में encoded values होने चाहिए; `@PARALLAX_URL_HEX@` जैसा token या `AssetManager.open("Parallax/ParallaxPapa.json")` call नहीं होना चाहिए। अगर log में अब भी यह `FileNotFoundException` आता है, host में **पुराना loader DEX** चल रहा है या पुराने classes के साथ duplicate/merge गलत है।
- `NoClassDefFoundError` में DEX/ZIP4J merge देखें; `UnsatisfiedLinkError` में process ABI और matching `libParallaxAlwaysOP.so` देखें। `File not found` server से आया हो तो link फिर जाँचें। `adb logcat -s ParallaxCore ParallaxDownloader ParallaxLoader` से exact stage देखें।
- नया URL सत्यापित किया गया: HTTPS GET ZIP देता है, password AES-256 verifier और archive HMAC दोनों match हुए, एक ARM64 ELF `.so` सही size में decrypt हुई। यह network/server availability और हर host APK की linker compatibility की भविष्य की guarantee नहीं है।
- APK signing बदलने पर installed app को update करने के लिए वही signing identity जरूरी होती है। CI release builds temporary key से sign होते हैं; production के लिए अपनी स्थायी key रखें।
