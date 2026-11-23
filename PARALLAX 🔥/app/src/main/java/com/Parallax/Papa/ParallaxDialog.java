package com.Parallax.Papa;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

/** Resource-independent dialog API for a host Activity, including landscape layouts. */
public final class ParallaxDialog {
    private ParallaxDialog() {}

    public static Dialog show(Activity activity) {
        if (activity == null || activity.isFinishing() ||
            (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()))
            throw new IllegalStateException("A live Activity is required to display the loader");

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        int background = Color.rgb(13, 20, 36);
        int foreground = Color.rgb(242, 247, 255);
        int accent = Color.rgb(116, 233, 207);
        int muted = Color.rgb(166, 183, 205);
        int pad = dp(activity, 22);

        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(pad, pad, pad, pad);
        GradientDrawable surface = new GradientDrawable();
        surface.setColor(background);
        surface.setCornerRadius(dp(activity, 20));
        content.setBackground(surface);

        TextView eyebrow = label(activity, "PARALLAX  /  LIBRARY", accent, 12, true);
        content.addView(eyebrow);
        TextView title = label(activity, "Online library loader", foreground, 23, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = dp(activity, 10);
        content.addView(title, titleParams);
        TextView detail = label(activity, "Load the configured ZIP archive into this app.", muted, 14, false);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = dp(activity, 7);
        content.addView(detail, detailParams);

        TextView status = label(activity,
            ParallaxCore.hasActivated(activity) ? "Already activated" : "Ready to activate", foreground, 16, false);
        LinearLayout.LayoutParams stateParams = new LinearLayout.LayoutParams(-1, -2);
        stateParams.topMargin = dp(activity, 22);
        content.addView(status, stateParams);
        ProgressBar progress = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(-1, dp(activity, 3));
        progressParams.topMargin = dp(activity, 13);
        content.addView(progress, progressParams);

        LinearLayout actions = new LinearLayout(activity);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actionsParams = new LinearLayout.LayoutParams(-1, -2);
        actionsParams.topMargin = dp(activity, 20);
        content.addView(actions, actionsParams);
        Button close = new Button(activity);
        close.setText("Close");
        close.setTextColor(foreground);
        close.setAllCaps(false);
        actions.addView(close);
        Button activate = new Button(activity);
        activate.setText(ParallaxCore.hasActivated(activity) ? "Reload" : "Activate");
        activate.setTextColor(background);
        activate.setAllCaps(false);
        GradientDrawable primary = new GradientDrawable();
        primary.setColor(accent);
        primary.setCornerRadius(dp(activity, 10));
        activate.setBackground(primary);
        actions.addView(activate);
        close.setOnClickListener(v -> dialog.dismiss());
        activate.setOnClickListener(v -> {
            activate.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            ParallaxCore.Listener listener = (message, finished) -> {
                if (!dialog.isShowing() || activity.isFinishing()) return;
                status.setText(message);
                progress.setVisibility(finished ? View.GONE : View.VISIBLE);
                activate.setEnabled(finished);
                if (finished && ParallaxCore.hasActivated(activity)) activate.setText("Reload");
            };
            if (ParallaxCore.hasActivated(activity)) ParallaxCore.reload(activity, listener);
            else ParallaxCore.init(activity, listener);
        });

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
            int height = Math.min(dp(activity, 340), (int) (screenHeight * .92f));
            window.setLayout(width, height);
            window.setGravity(Gravity.CENTER);
        }
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
