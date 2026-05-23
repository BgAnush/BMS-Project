package com.bank.auth;

import com.bank.auth.dto.*;
import com.bank.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security for controller unit testing
class ControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void testLogin_Success() throws Exception {
        String json = "{\"customerId\":\"CUS123456\", \"password\":\"Pass123!\"}";
        when(authService.login(any(LoginRequest.class))).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("mock-jwt-token"));
    }

    @Test
    void testRegister_Success() throws Exception {

        MockMultipartFile image =
                new MockMultipartFile(
                        "image",
                        "test.jpg",
                        "image/jpeg",
                        "data".getBytes()
                );

        when(authService.saveImage(any()))
                .thenReturn("test.jpg");

        when(authService.register(any(RegisterRequest.class), anyString()))
                .thenReturn("REGISTERED: ADM123456");

        mockMvc.perform(
                        multipart("/auth/admin/register")
                                .file(image)
                                .param("email", "admin@bank.com")
                                .param("password", "Strong123!")
                                .param("role", "ADMIN")
                                .param("fullName", "John Admin")
                                .param("mobileNumber", "9876543210")
                                .param("address", "123 Street")
                                .param("gender", "Male")
                                .param("dateOfBirth", "1995-01-01")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("REGISTERED: ADM123456"));
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/auth/internal/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetImage_NotFound() throws Exception {
        // Test ImageController specifically
        mockMvc.perform(get("/uploads/missing.jpg"))
                .andExpect(status().isNotFound());
    }
    
  
}