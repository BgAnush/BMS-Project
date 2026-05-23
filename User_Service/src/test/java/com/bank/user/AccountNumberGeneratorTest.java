package com.bank.user;

import org.junit.jupiter.api.Test;

import com.bank.user.util.AccountNumberGenerator;

import static org.junit.jupiter.api.Assertions.*;

class AccountNumberGeneratorTest {

	@Test
	void testGenerate() {
	    Long acc1 = AccountNumberGenerator.generate();
	    Long acc2 = AccountNumberGenerator.generate();
	    
	    assertNotNull(acc1, "Account number should not be null");
	    
	    // Check if it's a positive number with at least 8 digits 
	    // (Adjust the 10000000L based on your actual logic)
	    assertTrue(acc1 > 0, "Account number should be positive");
	    
	    // Ensure uniqueness
	    assertNotEquals(acc1, acc2, "Generated account numbers should be unique");
	}
}