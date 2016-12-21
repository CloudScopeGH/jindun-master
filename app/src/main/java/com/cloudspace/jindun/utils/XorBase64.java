package com.cloudspace.jindun.utils;

import android.util.Base64;

/**
 * 加密算法
 */
public class XorBase64 {


    public static String DEFAULT_KEY = "kulvX28.#_$[]jfiw^&*%(()÷≥|lkjfdl@#$!8ca024e6678bbf72df87044a5669a2cf";

    public static String encode(String s, String key) {
        return Base64.encodeToString(xorWithKey(s.getBytes(), key.getBytes()),
                Base64.DEFAULT);
    }

    public static String decode(String s, String key) {
        try {
            return new String(xorWithKey(Base64.decode(s, Base64.DEFAULT),
                    key.getBytes()));
        } catch (Exception e) {
            return null;
        }

    }

    private static byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }
}