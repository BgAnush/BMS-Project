package com.bank.auth;

import com.bank.auth.dto.LoginRequest;
import com.bank.auth.dto.RegisterRequest;
import com.bank.auth.dto.UserRequest;
import com.bank.auth.dto.response.ApiResponse;
import com.bank.auth.entity.AuthUser;
import com.bank.auth.entity.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UnifiedCoverageBoosterTest {

    @Test
    void boostDtoAndResponseCoverage() {

        // =========================================================
        // RegisterRequest Coverage
        // =========================================================
        RegisterRequest reg1 = new RegisterRequest(
                "a@b.com",
                "P@ss123",
                "USER",
                "John",
                "9876543210",
                "Addr",
                "Male",
                "Bio",
                LocalDate.now()
        );

        RegisterRequest reg2 = new RegisterRequest(
                "a@b.com",
                "P@ss123",
                "USER",
                "John",
                "9876543210",
                "Addr",
                "Male",
                "Bio",
                LocalDate.now()
        );

        RegisterRequest reg3 = new RegisterRequest();

        assertEquals(reg1, reg2);
        assertNotEquals(0, reg1.hashCode());
        assertFalse(reg1.toString().isEmpty());

        reg3.setAbout("New Bio");
        assertEquals("New Bio", reg3.getAbout());

        // =========================================================
        // LoginRequest Coverage
        // =========================================================
        LoginRequest log1 = new LoginRequest("CUS123", "Pass");
        LoginRequest log2 = new LoginRequest("CUS123", "Pass");

        assertEquals(log1, log2);
        assertNotEquals(0, log1.hashCode());

        // =========================================================
        // ApiResponse Coverage
        // =========================================================
        ApiResponse<String> api1 = ApiResponse.<String>builder()
                .success(true)
                .message("Operation Successful")
                .data("Some Data")
                .build();

        ApiResponse<String> api2 =
                new ApiResponse<>(true, "Operation Successful", "Some Data");

        assertEquals(api1, api2);
        assertNotEquals(0, api1.hashCode());
        assertFalse(api1.toString().isEmpty());

        assertEquals("Some Data", api1.getData());
        assertEquals("Operation Successful", api1.getMessage());
        assertTrue(api1.isSuccess());

        // =========================================================
        // UserRequest Coverage
        // =========================================================
        UserRequest ur1 = new UserRequest(
                1,
                "ADM1",
                "Full Name",
                "e@b.com",
                "9876543210",
                "Male",
                "Addr",
                "Bio",
                LocalDate.now(),
                "img"
        );

        UserRequest ur2 = new UserRequest(
                1,
                "ADM1",
                "Full Name",
                "e@b.com",
                "9876543210",
                "Male",
                "Addr",
                "Bio",
                LocalDate.now(),
                "img"
        );

        assertEquals(ur1, ur2);
        assertNotEquals(0, ur1.hashCode());
        assertFalse(ur1.toString().isEmpty());

        ur1.setProfileId(100);
        assertEquals(100, ur1.getProfileId());
    }

    @Test
    void boostEntityCoverage() {

        AuthUser u1 = AuthUser.builder()
                .profileId(1)
                .email("t@t.com")
                .role(Role.USER)
                .build();

        AuthUser u2 = AuthUser.builder()
                .profileId(1)
                .email("t@t.com")
                .role(Role.USER)
                .build();

        assertEquals(u1, u2);
        assertNotEquals(0, u1.hashCode());
        assertFalse(u1.toString().isEmpty());

        assertEquals(Role.USER, u1.getRole());
    }
}