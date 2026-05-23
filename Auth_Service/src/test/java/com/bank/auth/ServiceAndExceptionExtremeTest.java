package com.bank.auth;

import com.bank.auth.dto.LoginRequest;
import com.bank.auth.entity.AuthUser;
import com.bank.auth.exception.UserNotFoundException;
import com.bank.auth.repository.AuthRepository;
import com.bank.auth.service.AuthService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ServiceAndExceptionExtremeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @MockBean
    private AuthRepository repo;

    // =========================================================
    // SERVICE EXCEPTION BRANCHES
    // =========================================================
    @Test
    void testLoginUserNotFound() {

        when(repo.findByCustomerId(anyString()))
                .thenReturn(Optional.empty());

        LoginRequest request =
                new LoginRequest("NON", "PASS");

        assertThrows(
                UserNotFoundException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void testDeleteUserNotFound() {

        when(repo.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> authService.deleteUser(999)
        );
    }

    // =========================================================
    // IMAGE CONTROLLER EDGE CASES
    // =========================================================
    @Test
    void testBlankFilename() throws Exception {

        mockMvc.perform(get("/uploads/ "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFileNotFound() throws Exception {

        mockMvc.perform(get("/uploads/ghost_file.jpg"))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // INTERNAL DELETE SUCCESS
    // =========================================================
    @Test
    void testInternalDeleteSuccess() throws Exception {

        AuthUser user = new AuthUser();

        when(repo.findById(1))
                .thenReturn(Optional.of(user));

        mockMvc.perform(delete("/auth/internal/1"))
                .andExpect(status().isOk());

        verify(repo, times(1)).delete(user);
    }
}