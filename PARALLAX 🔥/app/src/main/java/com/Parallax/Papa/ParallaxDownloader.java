package com.Parallax.Papa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class ParallaxDownloader {
    private ParallaxDownloader() {}
    public static boolean download(String urlStr, String path) {
        HttpURLConnection conn = null;
        File target = new File(path);
        File pending = new File(path + ".part");
        try {
            URL url = new URL(urlStr);
            if (!"https".equalsIgnoreCase(url.getProtocol())) return false;
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(false);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return false;
            if (conn.getContentLength() > 100 * 1024 * 1024) return false;
            long total = 0;
            try (InputStream input = conn.getInputStream(); FileOutputStream output = new FileOutputStream(pending)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = input.read(buffer)) != -1) {
                    total += n;
                    if (total > 100 * 1024 * 1024) return false;
                    output.write(buffer, 0, n);
                }
                output.getFD().sync();
            }
            return total > 0 && pending.renameTo(target);
        } catch (Exception e) {
            return false;
        } finally {
            if (pending.exists()) pending.delete();
            if (conn != null) conn.disconnect();
        }
    }
}
