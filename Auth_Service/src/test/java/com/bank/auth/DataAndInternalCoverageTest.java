package com.bank.auth;

import com.bank.auth.dto.LoginRequest;
import com.bank.auth.dto.RegisterRequest;
import com.bank.auth.dto.UserRequest;
import com.bank.auth.dto.response.ApiResponse;
import com.bank.auth.dto.response.AuthResponse;
import com.bank.auth.entity.AuthUser;
import com.bank.auth.entity.Role;
import com.bank.auth.utility.CustomerIdGenerator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DataAndInternalCoverageTest {

    @Test
    void testLombokAndDataMethods() {

        // =====================================================
        // UserRequest Coverage
        // =====================================================

        UserRequest ur1 = new UserRequest(
                1,
                "ADM1234",
                "Name",
                "a@b.com",
                "9876543210",
                "Male",
                "Addr",
                "About",
                LocalDate.now(),
                "img"
        );

        UserRequest ur2 = new UserRequest();

        ur2.setProfileId(1);

        assertNotNull(ur1.toString());

        // FIXED SONAR ISSUE
        assertNotEquals(0, ur1.hashCode());

        assertEquals(
                ur1.getProfileId(),
                ur2.getProfileId()
        );

        assertNotEquals(ur1, ur2);

        // =====================================================
        // RegisterRequest Coverage
        // =====================================================

        RegisterRequest rr = new RegisterRequest(
                "e@b.com",
                "P@ss123",
                "USER",
                "John",
                "9876543210",
                "Addr",
                "Male",
                "Bio",
                LocalDate.now()
        );

        assertNotNull(rr.toString());

        // =====================================================
        // LoginRequest Coverage
        // =====================================================

        LoginRequest lr = new LoginRequest(
                "CUS123456",
                "P@ss123"
        );

        assertNotNull(lr.toString());

        // =====================================================
        // Response DTO Coverage
        // =====================================================

        AuthResponse auth = AuthResponse.builder()
                .token("jwt")
                .customerId("CUS")
                .build();

        ApiResponse<String> api = ApiResponse.<String>builder()
                .success(true)
                .message("OK")
                .data("X")
                .build();

        assertEquals("jwt", auth.getToken());

        assertTrue(api.isSuccess());

        // =====================================================
        // Entity + Enum Coverage
        // =====================================================

        AuthUser user = AuthUser.builder()
                .profileId(10)
                .email("x@y.com")
                .role(Role.MANAGER)
                .build();

        user.setCustomerId("MNG123");

        assertEquals(Role.MANAGER, user.getRole());

        // FIXED SONAR ISSUE
        assertNotEquals(0, user.hashCode());
    }

    @Test
    void testUtilityCoverage() {

        // =====================================================
        // CustomerIdGenerator Coverage
        // =====================================================

        String adminId = CustomerIdGenerator.generate("ADMIN");
        String managerId = CustomerIdGenerator.generate("MANAGER");
        String userId = CustomerIdGenerator.generate("USER");

        assertNotNull(adminId);
        assertNotNull(managerId);
        assertNotNull(userId);

        assertTrue(adminId.startsWith("ADM"));
        assertTrue(managerId.startsWith("MNG"));
        assertTrue(userId.startsWith("CUS"));
    }
}