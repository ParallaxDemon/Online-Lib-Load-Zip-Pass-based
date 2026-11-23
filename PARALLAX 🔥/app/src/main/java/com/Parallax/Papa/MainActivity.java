package com.Parallax.Papa;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

/** Preview host. Embedded apps call ParallaxDialog.show(activity) directly. */
public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Dialog dialog = ParallaxDialog.show(this);
        dialog.setOnDismissListener(ignored -> finish());
    }
}
