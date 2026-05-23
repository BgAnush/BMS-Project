package com.bank.account;

import com.bank.account.controller.AccountController;
import com.bank.account.dto.AccountResponseDTO;
import com.bank.account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService service;

    @Autowired
    private ObjectMapper objectMapper;

    // ================= CREATE =================
    @Test
    void createAccount_success() throws Exception {

        AccountResponseDTO response = new AccountResponseDTO();
        response.setAccountNumber("123");

        when(service.createAccount(any())).thenReturn(response);

        mockMvc.perform(post("/accounts/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "accountNumber": "123",
                                  "accType": "SAVINGS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("123"));
    }

    // ================= DELETE =================
    @Test
    void deleteAccount_success() throws Exception {

        when(service.deleteAccount("123")).thenReturn("deleted");

        mockMvc.perform(delete("/accounts/admin/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("deleted"));
    }

    // ================= UPDATE =================
    @Test
    void updateAccount_success() throws Exception {

        AccountResponseDTO response = new AccountResponseDTO();
        response.setAccountNumber("123");

        when(service.updateAccount(eq("123"), any()))
                .thenReturn(response);

        mockMvc.perform(put("/accounts/admin/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accType": "CURRENT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("123"));
    }

    // ================= USER - ME =================
    @Test
    void getMyAccount_success() throws Exception {

        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setAccountNumber("123");

        when(service.getByUserId(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/accounts/user/me")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("123"));
    }

    // ================= INTERNAL USER =================
    @Test
    void internalGetByUser_success() throws Exception {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setAccountNumber("123");

        // Match the @GetMapping("/manager/user/{userId}")
        when(service.getByUserId(1)).thenReturn(List.of(dto));

        mockMvc.perform(get("/accounts/manager/user/1"))
                .andExpect(status().isOk());
    }

    // ================= WITHDRAW =================
    @Test
    void withdraw_success() throws Exception {

        when(service.withdraw("123", 200.0))
                .thenReturn("withdraw success");

        mockMvc.perform(put("/accounts/internal/123/withdraw")
                        .param("amount", "200"))
                .andExpect(status().isOk())
                .andExpect(content().string("withdraw success"));
    }

    // ================= CREDIT =================
    @Test
    void credit_success() throws Exception {
        when(service.deposit("123", 500.0))
                .thenReturn("Deposit success. Balance: 1500.0");

        // Fixed path from /internal/credit to /internal/123/deposit
        mockMvc.perform(put("/accounts/internal/123/deposit")
                        .param("amount", "500"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Deposit success")));
    }
    // ================= ERROR CASE =================
    @Test
    void accountNotFound_shouldReturn500() throws Exception {
        when(service.getByUserId(1))
                .thenThrow(new RuntimeException("Account not found"));

        mockMvc.perform(get("/accounts/manager/user/1"))
                .andExpect(status().isInternalServerError());
    }
}