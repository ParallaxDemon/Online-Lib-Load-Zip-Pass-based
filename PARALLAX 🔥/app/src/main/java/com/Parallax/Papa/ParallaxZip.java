package com.Parallax.Papa;

import android.util.Log;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class ParallaxZip {

    private static final String TAG = "ParallaxZip";

    public static final class Result {
        public final boolean success;
        public final String error;

        private Result(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

        static Result ok() { return new Result(true, null); }
        static Result fail(String error) { return new Result(false, error); }
    }

    /** Legacy boolean API kept for existing callers. */
    public static boolean unzip(String zipPath, String dest, String password) {
        return unzipChecked(zipPath, dest, password).success;
    }

    /**
     * Extracts the archive and always reports the real failure reason.
     * The old implementation swallowed every exception and returned false,
     * which made every problem look like "wrong password" and left no logcat trace.
     */
    public static Result unzipChecked(String zipPath, String dest, String password) {
        File destination = new File(dest);
        if (!destination.exists() && !destination.mkdirs()) {
            return Result.fail("Cannot create extraction directory");
        }
        File archive = new File(zipPath);
        if (!archive.exists() || archive.length() == 0) {
            return Result.fail("Archive file is missing or empty");
        }

        try {
            // A previous load leaves the .so read-only (Android 15+ dlopen requirement).
            // Zip4j cannot overwrite it, so clear stale output before a fresh extract.
            if (destination.exists()) deleteContents(destination);

            ZipFile zipFile = new ZipFile(archive, password.toCharArray());
            try {
                zipFile.extractAll(dest);
                if (hasLibraries(destination)) return Result.ok();
                Log.w(TAG, "extractAll produced no .so files; retrying entry-by-entry");
            } catch (Throwable first) {
                Log.w(TAG, "extractAll failed; retrying entry-by-entry", first);
                deleteContents(destination);
            }

            // Fallback: decrypt and write each entry ourselves. Some Android
            // runtimes reject extractAll() even when the AES password is correct.
            List<FileHeader> headers = zipFile.getFileHeaders();
            if (headers == null || headers.isEmpty()) {
                return Result.fail("Archive has no entries (wrong password or corrupt ZIP)");
            }
            for (FileHeader header : headers) {
                File target = safeTarget(destination, header.getFileName());
                if (target == null) {
                    return Result.fail("Unsafe entry name in archive: " + header.getFileName());
                }
                if (header.isDirectory()) {
                    if (!target.exists() && !target.mkdirs()) {
                        return Result.fail("Cannot create directory " + target.getName());
                    }
                    continue;
                }
                File parent = target.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    return Result.fail("Cannot create directory for " + target.getName());
                }
                if (target.exists() && !target.delete()) {
                    return Result.fail("Cannot replace existing file " + target.getName());
                }
                try (InputStream input = zipFile.getInputStream(header);
                     FileOutputStream output = new FileOutputStream(target)) {
                    if (input == null) {
                        return Result.fail("Cannot decrypt entry " + header.getFileName());
                    }
                    byte[] buffer = new byte[16384];
                    int count;
                    while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                    output.getFD().sync();
                }
            }
            if (!hasLibraries(destination)) {
                return Result.fail("Archive extracted but contains no .so library");
            }
            return Result.ok();
        } catch (ZipException error) {
            Log.e(TAG, "Extraction failed: " + error.getMessage(), error);
            return Result.fail(describe(error, "wrong password or corrupt archive"));
        } catch (Throwable error) {
            Log.e(TAG, "Extraction failed", error);
            return Result.fail(describe(error, "unexpected extraction error"));
        }
    }

    private static String describe(Throwable error, String fallback) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
            ? fallback + " (" + error.getClass().getSimpleName() + ")"
            : message;
    }

    /** Reject path traversal and absolute paths from archive entry names. */
    private static File safeTarget(File destination, String entryName) {
        if (entryName == null || entryName.trim().isEmpty()) return null;
        File target = new File(destination, entryName);
        try {
            String root = destination.getCanonicalPath() + File.separator;
            String path = target.getCanonicalPath();
            return path.startsWith(root) ? target : null;
        } catch (Exception error) {
            return null;
        }
    }

    private static boolean hasLibraries(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return false;
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".so")) return true;
        }
        return false;
    }

    private static void deleteContents(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) deleteRecursive(file);
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteRecursive(child);
        }
        if (!file.delete()) Log.w(TAG, "Could not delete " + file.getName());
    }
}
