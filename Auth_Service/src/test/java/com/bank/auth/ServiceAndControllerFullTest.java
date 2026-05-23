package com.bank.auth;

import com.bank.auth.dto.LoginRequest;
import com.bank.auth.dto.UserRegisterRequest;
import com.bank.auth.service.AuthService;
import com.bank.auth.utility.CustomerIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ServiceAndControllerFullTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void testUtilityClass() {
        // This hits the com.bank.auth.utility package (currently at 0%)
        assertNotNull(CustomerIdGenerator.generate("ADMIN"));
        assertNotNull(CustomerIdGenerator.generate("MANAGER"));
        assertNotNull(CustomerIdGenerator.generate("USER"));
    }

    @Test
    void testFullAuthFlow() throws Exception {

        // ================= MOCKS =================

        when(authService.saveImage(any()))
                .thenReturn("saved_image.jpg");

        when(authService.userRegister(any(UserRegisterRequest.class), anyString()))
                .thenReturn("REGISTERED: CUS123456");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn("token_123");

        // ================= REGISTER =================

        MockMultipartFile file =
                new MockMultipartFile(
                        "image",
                        "test.jpg",
                        "image/jpeg",
                        "content".getBytes()
                );

        mockMvc.perform(
                        multipart("/auth/user/register")
                                .file(file)
                                .param("email", "test@bank.com")
                                .param("password", "Strong@123")
                                .param("fullName", "John Doe")
                                .param("mobileNumber", "9876543210")
                                .param("address", "123 Lane")
                                .param("gender", "Male")
                                .param("dateOfBirth", "1990-01-01")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("REGISTERED: CUS123456"));

        // ================= LOGIN =================

        String loginJson = """
            {
                "customerId":"CUS123456",
                "password":"Pass@1234"
            }
            """;

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("token_123"));

        // ================= DELETE =================

        doNothing().when(authService).deleteUser(anyInt());

        mockMvc.perform(delete("/auth/internal/1"))
                .andExpect(status().isOk());
    } 

    @Test
    void testImageController() throws Exception {
        // Hits the ImageController logic
        mockMvc.perform(get("/uploads/test.png"))
                .andExpect(status().isNotFound()); // Or isOk if you mock the file system
    }
}