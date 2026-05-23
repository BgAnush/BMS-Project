package com.bank.registry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ServiceRegistryApplicationTests {

	@Test
	void contextLoads() {
	}
	  @Test
	    void testMainMethod() {

	        assertDoesNotThrow(() ->
	                ServiceRegistryApplication.main(
	                        new String[]{}
	                )
	        );
	    }
}
