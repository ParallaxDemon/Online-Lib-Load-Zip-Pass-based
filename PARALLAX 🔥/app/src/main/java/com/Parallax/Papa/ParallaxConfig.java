package com.Parallax.Papa;

import android.content.Context;
import org.json.JSONObject;

/** Built-in config for the reusable DEX. These values are reversible, not secret. */
public final class ParallaxConfig {
    // Encoded values for the configured ZIP. The native PX/XOR/HEX codec decodes them.
    private static final String URL_HEX = "00391A15181C12427F4E170824090703420F0E157F311113310D1E00142804153F0F5F2E3E0D1B0F09412D11324C3C0E31055F3B051C4C283112034C3200010408431319274E020436125D09090D050B7F0C11083E4E1E080E28202A1B4F0A0820";
    private static final String PASSWORD_HEX = "00394B5955545640";
    private ParallaxConfig() {}

    public static final class Result {
        public final JSONObject value;
        public final String error;
        private Result(JSONObject value, String error) { this.value = value; this.error = error; }
    }
    public static JSONObject load(Context context) { return loadChecked(context).value; }
    public static Result loadChecked(Context context) {
        if (URL_HEX.startsWith("@") || PASSWORD_HEX.startsWith("@")) {
            return new Result(null, "Archive configuration was not supplied at build time");
        }
        try {
            JSONObject config = new JSONObject();
            config.put("url", URL_HEX);
            config.put("password", PASSWORD_HEX);
            return new Result(config, null);
        } catch (Exception error) {
            return new Result(null, "Built-in archive configuration invalid");
        }
    }
}
