package com.Parallax.Papa;

import java.io.File;

public final class ParallaxLoader {
    static { System.loadLibrary("parallaxnative"); }
    private ParallaxLoader() {}
    private static native String[] nativeCandidates(String directory);

    /** System.load retains Android's JNI_OnLoad and class-loader behavior. */
    public static int loadAll(File dir) {
        String[] paths = nativeCandidates(dir.getAbsolutePath());
        int loaded = 0;
        if (paths == null) return 0;
        for (String path : paths) {
            try {
                System.load(path);
                loaded++;
            } catch (UnsatisfiedLinkError | SecurityException error) {
                android.util.Log.w("ParallaxLoader", "Could not load " + new File(path).getName(), error);
            }
        }
        return loaded;
    }
}
