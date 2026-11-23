package com.Parallax.Papa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class ParallaxDownloader {
    private static final long MAX_ARCHIVE_BYTES = 100L * 1024 * 1024;
    private ParallaxDownloader() {}

    public static final class Result {
        public final boolean success;
        public final String message;
        private Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
    private static Result ok() { return new Result(true, "Downloaded ZIP archive"); }
    private static Result fail(String message) { return new Result(false, message); }

    /** Legacy boolean API kept for existing callers. */
    public static boolean download(String url, String path) {
        return downloadChecked(url, path).success;
    }

    public static Result downloadChecked(String urlStr, String path) {
        File target = new File(path);
        File pending = new File(path + ".part");
        if (pending.exists() && !pending.delete()) return fail("Cannot clear temporary download");
        HttpURLConnection conn = null;
        try {
            URL current = new URL(urlStr);
            for (int redirects = 0; redirects <= 5; redirects++) {
                if (!"https".equalsIgnoreCase(current.getProtocol()))
                    return fail("Archive URL must use HTTPS");
                conn = (HttpURLConnection) current.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("Accept", "application/zip, application/octet-stream, */*");
                int code = conn.getResponseCode();
                if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                    String location = conn.getHeaderField("Location");
                    if (location == null || redirects == 5) return fail("Too many archive redirects");
                    URL next = new URL(current, location);
                    conn.disconnect();
                    conn = null;
                    current = next;
                    continue;
                }
                if (code != HttpURLConnection.HTTP_OK)
                    return fail("Archive server returned HTTP " + code);
                // getContentLengthLong() requires Android 7; this loader supports API 19.
                if (conn.getContentLength() > MAX_ARCHIVE_BYTES)
                    return fail("Archive exceeds 100 MiB limit");

                long bytes = 0;
                try (InputStream input = conn.getInputStream();
                     FileOutputStream output = new FileOutputStream(pending)) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        bytes += count;
                        if (bytes > MAX_ARCHIVE_BYTES) return fail("Archive exceeds 100 MiB limit");
                        output.write(buffer, 0, count);
                    }
                    output.getFD().sync();
                }
                if (bytes < 4 || !isZip(pending)) {
                    // Several servers return HTTP 200 with the plain text "File not found".
                    if (isMissingFileText(pending))
                        return fail("Server says File not found; update the archive URL");
                    return fail("Server response is not a ZIP archive; check the URL");
                }
                if (target.exists() && !target.delete())
                    return fail("Cannot replace previous archive");
                if (!pending.renameTo(target)) return fail("Cannot save downloaded archive");
                return ok();
            }
            return fail("Too many archive redirects");
        } catch (java.net.SocketTimeoutException error) {
            return fail("Archive server timed out");
        } catch (Exception error) {
            android.util.Log.w("ParallaxDownloader", "Archive transfer failed: " + error.getClass().getSimpleName());
            return fail("Archive connection failed: " + error.getClass().getSimpleName());
        } finally {
            if (conn != null) conn.disconnect();
            if (pending.exists()) pending.delete();
        }
    }

    private static boolean isZip(File file) throws java.io.IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            byte[] magic = new byte[4];
            int offset = 0;
            while (offset < magic.length) {
                int n = input.read(magic, offset, magic.length - offset);
                if (n < 0) return false;
                offset += n;
            }
            return magic[0] == 'P' && magic[1] == 'K' &&
                ((magic[2] == 3 && magic[3] == 4) || (magic[2] == 5 && magic[3] == 6));
        }
    }
    private static boolean isMissingFileText(File file) throws java.io.IOException {
        if (file.length() > 128) return false;
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream input = new FileInputStream(file)) {
            int count = input.read(bytes);
            return count > 0 && new String(bytes, 0, count, "UTF-8").trim()
                .equalsIgnoreCase("File not found");
        }
    }
}
