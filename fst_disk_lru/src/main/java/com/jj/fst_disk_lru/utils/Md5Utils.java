package com.jj.fst_disk_lru.utils;

import android.text.TextUtils;

import java.security.MessageDigest;

/**
 * Jay
 */
public class Md5Utils {
    /**
     * md5 32 小写
     *
     * @param text
     * @return
     */
    public static String encode(String text) {
        if (TextUtils.isEmpty(text)) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                int number = b & 0xff;
                String hex = Integer.toHexString(number);
                if (hex.length() == 1) {
                    sb.append("0" + hex);
                } else {
                    sb.append(hex);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
