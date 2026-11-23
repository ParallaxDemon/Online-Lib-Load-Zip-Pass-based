package com.Parallax.Papa;

import android.util.Log;

public class ParallaxCrypto {

    private static final String KEY = "ParallaxPapa";
    private static final String SALT = "PX";

    public static String decrypt(String hexData) {

        if (hexData == null || hexData.length() % 2 != 0) {
            Log.e("CRYPTO", "Invalid HEX input");
            return null;
        }

        try {
            byte[] data = hexToBytes(hexData);
            byte[] key = KEY.getBytes("UTF-8");

            byte[] out = new byte[data.length];

            for (int i = 0; i < data.length; i++) {
                out[i] = (byte) (data[i] ^ key[i % key.length]);
            }

            String result = new String(out, "UTF-8");

            Log.d("CRYPTO", "Decrypted RAW: " + result);

            // 🔥 STRICT CHECK
            if (!result.startsWith(SALT)) {
                Log.e("CRYPTO", "SALT mismatch → Wrong encryption");
                return null;
            }

            String finalPass = result.substring(SALT.length());

            Log.d("CRYPTO", "Final PASS: " + finalPass);

            return finalPass;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] hexToBytes(String hex) {

        int len = hex.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {

            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);

            if (hi == -1 || lo == -1) {
                return null;
            }

            data[i / 2] = (byte) ((hi << 4) + lo);
        }

        return data;
    }
}
