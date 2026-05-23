package com.bank.card.util;

import java.util.Base64;

public class CvvUtil {
 private CvvUtil() {
    }

    public static String encrypt(String cvv) {
        return Base64.getEncoder().encodeToString(cvv.getBytes());
    }

    public static String decrypt(String encrypted) {
        return new String(Base64.getDecoder().decode(encrypted));
    }
}