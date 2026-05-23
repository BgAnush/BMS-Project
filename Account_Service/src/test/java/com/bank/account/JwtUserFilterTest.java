package com.bank.account;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtUserFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void filter_shouldExtractUserIdFromJwt() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("profileId", 505);
        
        String encodedPayload = Base64.getUrlEncoder()
                .encodeToString(payload.toString().getBytes())
                .replace("=", ""); // JWT parts don't have padding

        String fakeToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + encodedPayload + ".signature";

        mockMvc.perform(get("/accounts/admin")
                        .header("Authorization", "Bearer " + fakeToken))
                .andExpect(status().isOk());
        
    }
}