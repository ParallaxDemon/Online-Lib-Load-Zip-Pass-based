package com.Parallax.Papa;

import android.content.Context;
import org.json.JSONObject;
import java.io.InputStream;

public class ParallaxConfig {

    public static JSONObject load(Context context) {
        try {
            InputStream is = context.getAssets().open("Parallax/ParallaxPapa.json");

            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            return new JSONObject(new String(buffer, "UTF-8"));

        } catch (Exception e) {
            return null;
        }
    }
}
