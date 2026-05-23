package com.bank.creditcard;

import com.bank.card.util.CardUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardUtilTest {

    @Test
    void testGenerateCardNumber() {

        String cardNumber = CardUtil.generateCardNumber();

        assertNotNull(cardNumber);

        // correct format check
        assertTrue(cardNumber.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}"));
    }
}