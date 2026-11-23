package com.Parallax.Papa;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;

/** Resource-independent dialog API for a host Activity, including landscape layouts. */
public final class ParallaxDialog {
    private static final Random RANDOM = new Random();
    private static final ArrayList<Integer> RECENT_HUES = new ArrayList<>();
    private ParallaxDialog() {}

    private static synchronized int nextHue() {
        int hue;
        do { hue = RANDOM.nextInt(360); } while (RECENT_HUES.contains(hue));
        RECENT_HUES.add(hue);
        if (RECENT_HUES.size() > 100) RECENT_HUES.remove(0);
        return hue;
    }

    public static Dialog show(Activity activity) {
        if (activity == null || activity.isFinishing() ||
            (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()))
            throw new IllegalStateException("A live Activity is required to display the loader");

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        int hue = nextHue();
        int background = Color.HSVToColor(new float[]{hue, .58f, .18f});
        int foreground = Color.rgb(242, 247, 255);
        int accent = Color.HSVToColor(new float[]{(hue + 145) % 360, .55f, .95f});
        int muted = Color.rgb(203, 213, 225);
        int pad = dp(activity, 22);

        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(pad, pad, pad, pad);
        GradientDrawable surface = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{background, Color.HSVToColor(new float[]{(hue + 25) % 360, .7f, .12f})});
        surface.setCornerRadius(dp(activity, 20));
        surface.setStroke(dp(activity, 1), accent);
        content.setBackground(surface);

        TextView eyebrow = label(activity, "Checking Server", accent, 12, true);
        content.addView(eyebrow);
        TextView title = label(activity, "Online library loader", foreground, 23, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = dp(activity, 10);
        content.addView(title, titleParams);
        TextView detail = label(activity, "Welcome to our VIP Server", muted, 14, false);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = dp(activity, 7);
        content.addView(detail, detailParams);

        TextView status = label(activity, "Activating....", foreground, 16, false);
        LinearLayout.LayoutParams stateParams = new LinearLayout.LayoutParams(-1, -2);
        stateParams.topMargin = dp(activity, 22);
        content.addView(status, stateParams);
        ProgressBar progress = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21)
            progress.setIndeterminateTintList(ColorStateList.valueOf(accent));
        progress.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(-1, dp(activity, 3));
        progressParams.topMargin = dp(activity, 13);
        content.addView(progress, progressParams);

        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(false);
        scroll.addView(content);
        dialog.setContentView(scroll);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.65f;
            window.setAttributes(attributes);
        }
        dialog.show();
        if (window != null) {
            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
            int width = Math.min(dp(activity, 540), (int) (screenWidth * .92f));
            int height = Math.min(dp(activity, 250), (int) (screenHeight * .92f));
            window.setLayout(width, height);
            window.setGravity(Gravity.CENTER);
        }
        ParallaxCore.Listener listener = (message, finished) -> {
            if (!dialog.isShowing() || activity.isFinishing() ||
                (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed())) return;
            status.setText(message);
            progress.setVisibility(finished ? View.GONE : View.VISIBLE);
            if (finished) new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (dialog.isShowing()) dialog.dismiss();
            }, 2500);
        };
        // Re-download on repeat launches so a previous activation also loads in this process.
        if (ParallaxCore.hasActivated(activity)) ParallaxCore.reload(activity, listener);
        else ParallaxCore.init(activity, listener);
        return dialog;
    }

    private static TextView label(Activity activity, String value, int color, int size, boolean bold) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }
    private static int dp(Activity activity, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
            activity.getResources().getDisplayMetrics());
    }
}
