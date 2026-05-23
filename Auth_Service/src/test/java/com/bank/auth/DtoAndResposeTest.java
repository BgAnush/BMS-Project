package com.bank.auth;

import com.bank.auth.dto.*;
import com.bank.auth.dto.response.*;
import com.bank.auth.entity.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DtoAndEntityTests {

    private final Validator validator;

    public DtoAndEntityTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void testAuthUserAndResponses() {
        // Test Entity & Builder
        AuthUser user = AuthUser.builder()
                .profileId(1).customerId("CUS123456").email("test@bank.com")
                .password("Hash123!").role(Role.USER).build();
        
        assertEquals("CUS123456", user.getCustomerId());
        
        // Test Responses
        AuthResponse authRes = AuthResponse.builder().token("jwt-token").customerId("CUS123").build();
        ApiResponse<String> apiRes = ApiResponse.<String>builder()
                .success(true).message("Success").data("Payload").build();
        
        assertNotNull(authRes.getToken());
        assertTrue(apiRes.isSuccess());
    }

    @Test
    void testValidations() {
        // Test RegisterRequest Validation (Invalid Case)
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail("invalid-email"); // Should fail @Email
        reg.setPassword("short"); // Should fail @Pattern
        
        var violations = validator.validate(reg);
        assertFalse(violations.isEmpty());

        // Test LoginRequest (Valid Case)
        LoginRequest login = new LoginRequest("ADM123456", "Pass1234!");
        assertTrue(validator.validate(login).isEmpty());
    }

    @Test
    void testUserRequestGettersSetters() {
        UserRequest ur = new UserRequest();
        ur.setFullName("John Doe");
        ur.setMobileNumber("9876543210");
        ur.setDateOfBirth(LocalDate.of(1990, 1, 1));
        
        assertEquals("John Doe", ur.getFullName());
        assertEquals("9876543210", ur.getMobileNumber());
    }
}