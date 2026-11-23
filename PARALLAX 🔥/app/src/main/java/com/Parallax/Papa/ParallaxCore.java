package com.Parallax.Papa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;

public class ParallaxCore {

    private static final String PREF = "parallax_secure";
    private static final String KEY_DONE = "done";

    // ✅ ENTRY POINT
    public static void init(final Context context) {

        SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        File extractDir = new File(context.getFilesDir(), "payload");

        boolean alreadyDone = sp.getBoolean(KEY_DONE, false);

        // 🔥 अगर पहले load हो चुका है → dialog दिखाओ
        if (alreadyDone && extractDir.exists()) {

            showReloadDialog(context);
            return;
        }

        // 🔥 first time load
        startLoading(context, false);
    }

    // 🔥 RELOAD DIRECT
    public static void reload(Context context) {
        startLoading(context, true);
    }

    // 🔥 MAIN LOADER
    private static void startLoading(final Context context, final boolean forceReload) {

        Thread t = new Thread(new Runnable() {
				@Override
				public void run() {

					try {

						SharedPreferences sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

						File extractDir = new File(context.getFilesDir(), "payload");
						File zipFile = new File(context.getFilesDir(), "payload.zip");

						// 🔥 force reload cleanup
						if (forceReload) {
							deleteRecursive(extractDir);

							if (zipFile.exists()) {
								zipFile.delete();
							}

							sp.edit().putBoolean(KEY_DONE, false).apply();
							showToast(context, "♻️ Reloading...");
						}

						JSONObject cfg = ParallaxConfig.load(context);

						if (cfg == null) {
							showToast(context, "❌ Config Load Failed");
							return;
						}

						String url = ParallaxCrypto.decrypt(cfg.getString("url"));
						String pass = ParallaxCrypto.decrypt(cfg.getString("password"));

						if (url == null || pass == null) {
							showToast(context, "❌ Decrypt Failed");
							return;
						}

						boolean downloaded = ParallaxDownloader.download(url, zipFile.getAbsolutePath());

						if (!downloaded) {
							showToast(context, "❌ Download Failed");
							return;
						}

						if (!extractDir.exists()) {
							extractDir.mkdirs();
						}

						boolean unzipOk = ParallaxZip.unzip(
                            zipFile.getAbsolutePath(),
                            extractDir.getAbsolutePath(),
                            pass
						);

						if (!unzipOk) {
							showToast(context, "❌ Wrong Password");
							return;
						}

						// 🔥 LOAD LIBS
						ParallaxLoader.loadAll(extractDir);
						showToast(context, "🚀 Success");

						sp.edit().putBoolean(KEY_DONE, true).apply();

					} catch (Exception e) {
						e.printStackTrace();
						showToast(context, "❌ Error: " + e.getMessage());
					}
				}
			});

        t.start();
    }

    // 🔥 DIALOG (Reload / No)
    private static void showReloadDialog(final Context context) {

        android.os.Handler handler = new android.os.Handler(context.getMainLooper());

        handler.post(new Runnable() {
				@Override
				public void run() {

					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("ACTIVATE");
					builder.setMessage("IF ALREADY ACTIVATED THEN DO NOT ACTIVATE?");

					// 🔥 YES → reload
					builder.setPositiveButton("ACTIVATE", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								startLoading(context, true);
							}
						});

					// ❌ NO → do nothing
					builder.setNegativeButton(" NO ALREADY ACTIVATED DONE  ", null);

					builder.setCancelable(false);
					builder.show();
				}
			});
    }

    // 🔥 DELETE FOLDER
    private static void deleteRecursive(File file) {

        if (file == null || !file.exists()) return;

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    deleteRecursive(children[i]);
                }
            }
        }

        file.delete();
    }

    // 🔥 SAFE TOAST
    private static void showToast(final Context context, final String msg) {

        android.os.Handler handler = new android.os.Handler(context.getMainLooper());

        handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
				}
			});
    }
}
