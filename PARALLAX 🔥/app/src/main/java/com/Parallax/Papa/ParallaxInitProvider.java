package com.Parallax.Papa;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;

/** Manifest startup hook; waits for a visible Activity before opening the dialog. */
public final class ParallaxInitProvider extends ContentProvider {
    private static final String TAG = "ParallaxInit";

    @Override public boolean onCreate() {
        Context context = getContext();
        if (context == null || !(context.getApplicationContext() instanceof Application)) return false;
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            private int started;
            private boolean shownThisForeground;
            private boolean pending;
            private Activity dialogOwner;
            private Activity lastResumed;
            private Dialog dialog;

            @Override public void onActivityCreated(Activity activity, Bundle state) {}
            @Override public void onActivityStarted(Activity activity) { started++; }
            @Override public void onActivityResumed(Activity activity) {
                lastResumed = activity;
                if (shownThisForeground || pending || dialog != null || ParallaxCore.isRunning()) return;
                pending = true;
                activity.getWindow().getDecorView().postDelayed(() -> {
                    pending = false;
                    Activity target = lastResumed;
                    if (started <= 0 || target == null || target.isFinishing() ||
                        (Build.VERSION.SDK_INT >= 17 && target.isDestroyed()) ||
                        shownThisForeground || dialog != null || ParallaxCore.isRunning()) return;
                    try {
                        dialogOwner = target;
                        dialog = ParallaxDialog.show(target);
                        shownThisForeground = true;
                        dialog.setOnDismissListener(ignored -> {
                            dialog = null;
                            dialogOwner = null;
                            // Close the standalone demo host; do not finish an embedded app.
                            if ("com.Parallax.Papa.MainActivity".equals(target.getClass().getName())
                                && !target.isFinishing() &&
                                (Build.VERSION.SDK_INT < 17 || !target.isDestroyed())) target.finish();
                        });
                    } catch (RuntimeException error) {
                        dialogOwner = null;
                        dialog = null;
                        Log.e(TAG, "Unable to display loader", error);
                    }
                }, 300);
            }
            @Override public void onActivityPaused(Activity activity) {
                if (lastResumed == activity) lastResumed = null;
            }
            @Override public void onActivityStopped(Activity activity) {
                if (started > 0) started--;
                if (started == 0) shownThisForeground = false;
            }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) {}
            @Override public void onActivityDestroyed(Activity activity) {
                if (activity == dialogOwner && dialog != null) dialog.dismiss();
            }
        });
        return true;
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection,
                                  String[] selectionArgs, String sortOrder) { return null; }
    @Override public String getType(Uri uri) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection,
                                String[] selectionArgs) { return 0; }
}
