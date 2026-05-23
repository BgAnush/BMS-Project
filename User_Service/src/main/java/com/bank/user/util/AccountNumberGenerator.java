package com.bank.user.util;

import java.util.Random;

public class AccountNumberGenerator {

    private static final Random random = new Random();

    // ✅ Private constructor to prevent instantiation
    private AccountNumberGenerator() {
        throw new IllegalStateException("Utility class");
    }

    public static Long generate() {
        int firstDigit = 1 + random.nextInt(9);
        int remainingDigits = random.nextInt(100000);
        return Long.parseLong(firstDigit + String.format("%05d", remainingDigits));
    }
}