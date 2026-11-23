package com.Parallax.Papa;

import android.content.Context;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class ParallaxConfig {
    private ParallaxConfig() {}
    public static JSONObject load(Context context) {
        try (InputStream input = context.getAssets().open("Parallax/ParallaxPapa.json");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = input.read(buffer)) != -1) output.write(buffer, 0, n);
            return new JSONObject(output.toString("UTF-8"));
        } catch (Exception e) {
            android.util.Log.e("ParallaxConfig", "Cannot read configuration asset", e);
            return null;
        }
    }
}
