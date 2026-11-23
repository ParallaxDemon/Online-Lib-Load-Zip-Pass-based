package com.Parallax.Papa;

import android.content.Context;
import org.json.JSONObject;

/** Built-in config for the reusable DEX. These values are reversible, not secret. */
public final class ParallaxConfig {
    // The CI build replaces these tokens in its temporary source copy using Actions secrets.
    private static final String URL_HEX = "@PARALLAX_URL_HEX@";
    private static final String PASSWORD_HEX = "@PARALLAX_PASSWORD_HEX@";
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
