# Build status

The loader URL and ZIP password are encoded in Java `ParallaxConfig.java`. The PX/XOR/HEX decode was verified for the configured URL and password; the repository ZIP AES-256 verifier and HMAC match password 989878. CI injection is a no-op for these built-in constants. This exact private source snapshot has not been compiled into an APK here because Android SDK/Gradle is unavailable in this environment. The previous public CI APK is TEST-CONFIG and cannot download the ZIP.
