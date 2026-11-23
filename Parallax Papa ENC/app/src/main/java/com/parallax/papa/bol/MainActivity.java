package com.parallax.papa.bol;

import android.app.Activity;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private EditText url, password;
    private TextView result;
    private String lastJson;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        url = findViewById(R.id.url);
        password = findViewById(R.id.password);
        result = findViewById(R.id.result);
        Button generate = findViewById(R.id.generate);
        Button copy = findViewById(R.id.copy);
        generate.setOnClickListener(v -> {
            String link = url.getText().toString().trim();
            String pass = password.getText().toString();
            if (!link.startsWith("https://") || pass.isEmpty()) {
                result.setText("Enter an HTTPS archive URL and a password.");
                lastJson = null;
                return;
            }
            try {
                JSONObject json = new JSONObject();
                json.put("url", ParallaxCrypto.encrypt(link));
                json.put("password", ParallaxCrypto.encrypt(pass));
                lastJson = json.toString(2);
                result.setText(lastJson);
            } catch (Exception error) { result.setText("Could not generate configuration"); }
        });
        copy.setOnClickListener(v -> {
            if (lastJson == null) { Toast.makeText(this, "Generate JSON first", Toast.LENGTH_SHORT).show(); return; }
            ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Parallax config", lastJson));
            Toast.makeText(this, "JSON copied", Toast.LENGTH_SHORT).show();
        });
    }
}
