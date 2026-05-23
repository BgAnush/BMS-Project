package com.bank.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.bank.user.controller.UserController;
import com.bank.user.dto.UpdateUserRequest;
import com.bank.user.entity.BankUser;
import com.bank.user.service.UserService;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService service;

    // ========================= GET ALL =========================
    @Test
    void getAllUsers_shouldReturnUsersList() throws Exception {

        // Arrange
        BankUser user = new BankUser();
        user.setProfileId(1);
        user.setFullName("John Doe");
        user.setEmailId("john@bank.com");

        when(service.getAllUsers()).thenReturn(List.of(user));

        // Act + Assert
        mockMvc.perform(get("/users/admin/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].profileId").value(1))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$[0].emailId").value("john@bank.com"));

        verify(service, times(1)).getAllUsers();
    }

    // ========================= GET BY ID =========================
    @Test
    void getUserById_shouldReturnUser() throws Exception {

        // Arrange
        BankUser user = new BankUser();
        user.setProfileId(1);
        user.setFullName("John Doe");
        user.setEmailId("john@bank.com");

        when(service.getUser(1)).thenReturn(user);

        // Act + Assert
        mockMvc.perform(get("/users/admin/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.emailId").value("john@bank.com"));

        verify(service, times(1)).getUser(1);
    }

    // ========================= DELETE =========================
    @Test
    void deleteUser() throws Exception {

        doNothing().when(service).deleteUser(1);

        mockMvc.perform(delete("/users/admin/1"))
                .andExpect(status().isOk());
    }

    // ========================= UPDATE =========================
    @Test
    void updateUser() throws Exception {
        BankUser u = new BankUser();
        u.setProfileId(1);

        when(service.updateUser(eq(1), any(UpdateUserRequest.class)))
                .thenReturn(u);

        mockMvc.perform(patch("/users/admin/1")
                        .contentType("application/json")
                        .content("""
                                {
                                    "fullName": "Test User",
                                    "mobileNumber": "9876543210",
                                    "address": "123 Bank Street, City",
                                    "about": "Testing coverage"
                                }
                                """))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.profileId").value(1));
        
        // This line ensures SonarQube is happy with the "assertion" requirement
        verify(service, times(1)).updateUser(eq(1), any(UpdateUserRequest.class));
    }
    // ========================= ME FLOW =========================
    @Test
    void meFlow() throws Exception {

        BankUser u = new BankUser();
        u.setProfileId(1);

        when(service.getUser(1)).thenReturn(u);

        mockMvc.perform(get("/users/me")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }
}