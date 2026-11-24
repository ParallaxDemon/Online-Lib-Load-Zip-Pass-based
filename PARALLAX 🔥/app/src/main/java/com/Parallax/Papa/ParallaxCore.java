package com.Parallax.Papa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.File;

/** Public entry point for embedding the loader classes in an Android host app. */
public final class ParallaxCore {
    public interface Listener { void onState(String message, boolean finished); }
    private static final String PREF = "parallax_secure";
    private static final String KEY_DONE = "done";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static boolean running;
    private ParallaxCore() {}

    public static synchronized boolean isRunning() { return running; }

    public static boolean hasActivated(Context context) {
        File dir = new File(context.getFilesDir(), "payload");
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_DONE, false) && dir.exists();
    }

    public static void init(Context context) { init(context, null); }
    public static void init(Context context, Listener listener) {
        if (hasActivated(context)) {
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                new AlertDialog.Builder(context).setTitle("Already activated")
                    .setMessage("Reload the library archive?")
                    .setPositiveButton("Reload", (dialog, which) -> reload(context, listener))
                    .setNegativeButton("Keep current", (dialog, which) -> state(context, listener, "Ready", true))
                    .show();
            } else state(context, listener, "Already activated; call reload(context) to refresh", true);
            return;
        }
        start(context, listener, false);
    }
    public static void reload(Context context) { reload(context, null); }
    public static void reload(Context context, Listener listener) { start(context, listener, true); }
    private static synchronized boolean acquire() {
        if (running) return false;
        running = true;
        return true;
    }
    private static void start(Context context, Listener listener, boolean reload) {
        if (!acquire()) { state(context, listener, "Load already running", true); return; }
        Context app = context.getApplicationContext();
        new Thread(() -> {
            try {
                state(app, listener, "Reading configuration…", false);
                ParallaxConfig.Result config = ParallaxConfig.loadChecked(app);
                if (config.value == null) { state(app, listener, config.error, true); return; }
                JSONObject cfg = config.value;
                String url = ParallaxCrypto.decrypt(cfg.getString("url"));
                String password = ParallaxCrypto.decrypt(cfg.getString("password"));
                if (url == null || password == null) { state(app, listener, "Invalid configuration", true); return; }
                File zip = new File(app.getFilesDir(), "payload.zip");
                File dir = new File(app.getFilesDir(), "payload");
                state(app, listener, "Downloading archive…", false);
                ParallaxDownloader.Result transfer = ParallaxDownloader.downloadChecked(url, zip.getAbsolutePath());
                if (!transfer.success) { state(app, listener, transfer.message, true); return; }
                if (reload) {
                    deleteRecursive(dir);
                    app.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean(KEY_DONE, false).apply();
                }
                if (!dir.exists() && !dir.mkdirs()) { state(app, listener, "Cannot create payload directory", true); return; }
                state(app, listener, "Extracting archive…", false);
                ParallaxZip.Result extracted = ParallaxZip.unzipChecked(
                    zip.getAbsolutePath(), dir.getAbsolutePath(), password);
                if (!extracted.success) {
                    state(app, listener, "Archive extraction failed: " + extracted.error, true); return;
                }
                state(app, listener, "Loading libraries…", false);
                int count = ParallaxLoader.loadAll(dir);
                if (count == 0) { state(app, listener, "No compatible library loaded", true); return; }
                app.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean(KEY_DONE, true).apply();
                state(app, listener, count + " library(s) loaded", true);
            } catch (Exception | LinkageError error) {
                android.util.Log.e("ParallaxCore", "Loader failed", error);
                state(app, listener, "Loader failed: " + error.getClass().getSimpleName(), true);
            } finally { synchronized (ParallaxCore.class) { running = false; } }
        }, "parallax-loader").start();
    }
    private static void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteRecursive(child);
        }
        if (!file.delete()) android.util.Log.w("ParallaxCore", "Could not delete " + file.getName());
    }
    private static void state(Context context, Listener listener, String message, boolean finished) {
        MAIN.post(() -> {
            if (listener != null) listener.onState(message, finished);
            else Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }
}
