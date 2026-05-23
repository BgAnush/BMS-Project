package com.bank.auth.utility;

import java.util.UUID;


public class CustomerIdGenerator {

    private CustomerIdGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String generate(String role) {

        String prefix;

        switch (role.toUpperCase()) {
            case "ADMIN":
                prefix = "ADM";
                break;
            case "MANAGER":
                prefix = "MNG";
                break;
            default:
                prefix = "CUS";
        }

        return prefix + UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();
    }
}