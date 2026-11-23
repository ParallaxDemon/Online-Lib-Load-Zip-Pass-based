package com.Parallax.Papa;

import java.io.File;

public final class ParallaxLoader {
    static { System.loadLibrary("ParallaxAlwaysOP"); }
    private ParallaxLoader() {}
    private static native String[] nativeCandidates(String directory);

    /** System.load retains Android's JNI_OnLoad and class-loader behavior. */
    public static int loadAll(File dir) {
        String[] paths = nativeCandidates(dir.getAbsolutePath());
        int loaded = 0;
        if (paths == null) return 0;
        for (String path : paths) {
            File library = new File(path);
            try {
                // Some extraction paths do not preserve the executable bit; System.load needs it.
                if (!library.setExecutable(true, false)) {
                    android.util.Log.w("ParallaxLoader", "Could not mark executable: " + library.getName());
                }
                System.load(library.getAbsolutePath());
                android.util.Log.i("ParallaxLoader", "Loaded " + library.getName());
                loaded++;
            } catch (UnsatisfiedLinkError | SecurityException error) {
                android.util.Log.w("ParallaxLoader", "Could not load " + library.getName(), error);
            }
        }
        return loaded;
    }
}
