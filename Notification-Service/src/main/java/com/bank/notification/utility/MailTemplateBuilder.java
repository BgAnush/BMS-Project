package com.bank.notification.utility;

public class MailTemplateBuilder {

    private MailTemplateBuilder() {
    }

    public static String buildRegistrationTemplate(
            String fullName,
            String customerId,
            String role
    ) {

        return """
                Dear %s,

                Welcome to Banking Management System.

                Your account has been created successfully.

                ---------------------------------------
                CUSTOMER ID : %s
                ROLE        : %s
                ---------------------------------------

                Please use this Customer ID for login.

                Keep your credentials secure and do not share them.

                Thank You,
                Banking Management System Team
                """
                .formatted(
                        fullName,
                        customerId,
                        role
                );
    }
}