package com.Parallax.Papa;

import java.io.File;

public class ParallaxLoader {

    public static void loadAll(File dir) {

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {

            if (f.isDirectory()) {
                loadAll(f);
            } else {

                if (f.getName().endsWith(".so")) {
                    try {
                        System.load(f.getAbsolutePath());
                    } catch (Throwable ignored) {}
                }
            }
        }
    }
}
