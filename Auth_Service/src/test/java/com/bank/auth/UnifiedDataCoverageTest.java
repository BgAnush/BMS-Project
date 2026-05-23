package com.bank.auth;

import com.bank.auth.dto.*;
import com.bank.auth.dto.response.*;
import com.bank.auth.entity.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class UnifiedDataCoverageTest {

    @Test
    void testAllDtosAndEntities() {
        // 1. UserRequest (Hits Getters, Setters, NoArgs, AllArgs, and Data methods)
        UserRequest ur1 = new UserRequest(1, "ADM1234", "Name", "e@b.com", "9876543210", "Male", "Addr", "About", LocalDate.now(), "img");
        UserRequest ur2 = new UserRequest();
        ur2.setProfileId(1);
        
        assertNotNull(ur1.toString());
        assertEquals(ur1.getProfileId(), ur2.getProfileId());
        assertNotEquals(ur1, ur2); // Checks equals/hashCode

        // 2. RegisterRequest
        RegisterRequest rr = new RegisterRequest("a@b.com", "Pass123!", "USER", "Name", "9876543210", "Addr", "Male", "About", LocalDate.now());
        assertNotNull(rr.getEmail());
        assertNotNull(rr.toString());

        // 3. LoginRequest
        LoginRequest lr = new LoginRequest("CUS123456", "Pass123!");
        lr.setCustomerId("ADM000000");
        assertEquals("ADM000000", lr.getCustomerId());

        // 4. Responses (Hits Builder + AllArgs)
        AuthResponse auth = AuthResponse.builder().token("tk").customerId("id").build();
        ApiResponse<String> api = new ApiResponse<>(true, "msg", "data");
        
        assertNotNull(auth.toString());
        assertTrue(api.isSuccess());

        // 5. Entity & Enums
        AuthUser user = AuthUser.builder().email("a@b.com").role(Role.ADMIN).build();
        user.setProfileId(101);
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(101, user.getProfileId());
    }
}