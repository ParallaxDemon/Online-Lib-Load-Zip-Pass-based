package com.parallax.papa.bol;

/** Legacy PX + XOR HEX format, shared with the loader. This is encoding, not encryption. */
public final class ParallaxCrypto {
    static { System.loadLibrary("parallaxnative"); }
    private ParallaxCrypto() {}
    private static native String nativeEncrypt(String value);
    public static String encrypt(String value) { return nativeEncrypt(value); }
}
