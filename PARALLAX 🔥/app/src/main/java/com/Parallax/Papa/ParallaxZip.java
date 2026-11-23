package com.Parallax.Papa;

import net.lingala.zip4j.ZipFile;

public class ParallaxZip {

    public static boolean unzip(String zipPath, String dest, String password) {
        try {
            ZipFile zipFile = new ZipFile(zipPath, password.toCharArray());
            zipFile.extractAll(dest);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
