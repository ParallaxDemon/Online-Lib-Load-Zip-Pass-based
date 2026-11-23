package com.parallax.papa.bol;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;

public class MainActivity extends Activity {

    EditText inputUrl, inputPass;
    Button btnGenerate, btnCopy;
    TextView outputJson;

    String lastJson = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputUrl = new EditText(this);
        inputUrl.setHint("LIB KA LINK DAAL");

        inputPass = new EditText(this);
        inputPass.setHint("LIB KA PASSWORD DAAL");

        btnGenerate = new Button(this);
        btnGenerate.setText("ENCRYPT ");

        btnCopy = new Button(this);
        btnCopy.setText("Copy ");

        outputJson = new TextView(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(inputUrl);
        layout.addView(inputPass);
        layout.addView(btnGenerate);
        layout.addView(btnCopy);
        layout.addView(outputJson);

        setContentView(layout);

        // 🔥 Generate JSON
        btnGenerate.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					String url = inputUrl.getText().toString();
					String pass = inputPass.getText().toString();

					if (url.isEmpty() || pass.isEmpty()) {
						outputJson.setText("❌ Enter both URL & Password");
						return;
					}

					String encUrl = ParallaxCrypto.encrypt(url);
					String encPass = ParallaxCrypto.encrypt(pass);

					lastJson = "{\n" +
                        "  \"url\": \"" + encUrl + "\",\n" +
                        "  \"password\": \"" + encPass + "\"\n" +
                        "}";

					outputJson.setText(lastJson);
				}
			});

        // 🔥 Copy JSON
        btnCopy.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					if (lastJson.isEmpty()) {
						Toast.makeText(MainActivity.this, "❌ Generate JSON first", Toast.LENGTH_SHORT).show();
						return;
					}

					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("json", lastJson);
					clipboard.setPrimaryClip(clip);

					Toast.makeText(MainActivity.this, "✅ ALL COPIED ", Toast.LENGTH_SHORT).show();
				}
			});
    }
}
