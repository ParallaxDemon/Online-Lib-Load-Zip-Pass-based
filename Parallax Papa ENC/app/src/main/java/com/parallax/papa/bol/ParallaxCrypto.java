package com.parallax.papa.bol;

public class ParallaxCrypto {

    private static final String KEY = "ParallaxPapa";
    private static final String SALT = "PX"; // extra safety

    public static String encrypt(String plain) {
        try {
            String data = SALT + plain;

            byte[] input = data.getBytes("UTF-8");
            byte[] key = KEY.getBytes("UTF-8");

            byte[] out = new byte[input.length];

            for (int i = 0; i < input.length; i++) {
                out[i] = (byte) (input[i] ^ key[i % key.length]);
            }

            return bytesToHex(out);

        } catch (Exception e) {
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}
