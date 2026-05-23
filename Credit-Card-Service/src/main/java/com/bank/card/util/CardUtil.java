package com.bank.card.util;

import java.util.Random;

public class CardUtil {

    private static final Random RANDOM = new Random();
 private CardUtil() {
    }

    public static String generateCardNumber() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            int digit = (i == 0) ? RANDOM.nextInt(9) + 1 : RANDOM.nextInt(10);
            sb.append(digit);

            if ((i + 1) % 4 == 0 && i != 15) {
                sb.append("-");
            }
        }

        return sb.toString();
    }
}