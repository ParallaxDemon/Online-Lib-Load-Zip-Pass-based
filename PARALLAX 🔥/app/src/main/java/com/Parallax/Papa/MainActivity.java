package com.Parallax.Papa;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;

/** Standalone demo. The manifest provider opens the dialog on resume. */
public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        TextView placeholder = new TextView(this);
        placeholder.setText("Checking Server");
        placeholder.setGravity(Gravity.CENTER);
        placeholder.setTextColor(Color.WHITE);
        placeholder.setBackgroundColor(Color.rgb(12, 16, 24));
        setContentView(placeholder);
    }
}
