package com.Parallax.Papa;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    private ProgressBar progress;
    private Button action;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.status);
        progress = findViewById(R.id.progress);
        action = findViewById(R.id.action);
        action.setOnClickListener(v -> run(true));
        run(false);
    }
    private void run(boolean reload) {
        action.setEnabled(false);
        ParallaxCore.Listener listener = (message, finished) -> {
            if (isFinishing() || isDestroyed()) return;
            status.setText(message);
            progress.setVisibility(finished ? View.GONE : View.VISIBLE);
            action.setEnabled(finished);
        };
        if (reload) ParallaxCore.reload(this, listener);
        else ParallaxCore.init(this, listener);
    }
}
