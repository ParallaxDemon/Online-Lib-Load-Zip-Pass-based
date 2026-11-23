package com.Parallax.Papa;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ParallaxDownloader {

    public static boolean download(String urlStr, String path) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            InputStream in = conn.getInputStream();
            FileOutputStream out = new FileOutputStream(path);

            byte[] buffer = new byte[4096];
            int len;

            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            out.close();
            in.close();

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
