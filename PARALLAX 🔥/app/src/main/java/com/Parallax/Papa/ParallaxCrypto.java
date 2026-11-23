package com.Parallax.Papa;

/** Legacy PX + XOR HEX format, shared with the generator. This is encoding, not encryption. */
public final class ParallaxCrypto {
    static { System.loadLibrary("parallaxnative"); }
    private ParallaxCrypto() {}
    private static native String nativeDecrypt(String value);
    public static String decrypt(String value) { return nativeDecrypt(value); }
}
