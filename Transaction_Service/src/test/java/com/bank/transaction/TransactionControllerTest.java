package com.bank.transaction;

import com.bank.transaction.controller.TransactionController;
import com.bank.transaction.dto.DepositeDTO;
import com.bank.transaction.dto.TransactionRequestDTO;
import com.bank.transaction.exception.GlobalExceptionHandler;
import com.bank.transaction.exception.InsufficientBalanceException;
import com.bank.transaction.exception.UnauthorizedException;
import com.bank.transaction.service.TransactionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;


@Import(GlobalExceptionHandler.class)
@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService service;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // SUCCESS CASES
    // =========================

    @Test
    void deposit_success() throws Exception {

        DepositeDTO dto = new DepositeDTO();
        dto.setAmount(100.0);

        when(service.deposit(1, 100.0)).thenReturn("Deposit Successful");

        mockMvc.perform(post("/transactions/user/deposit")
                .header("X-User-Id", "1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Deposit Successful"));
    }

    @Test
    void withdraw_success() throws Exception {

        DepositeDTO dto = new DepositeDTO();
        dto.setAmount(50.0);

        when(service.withdraw(1, 50.0)).thenReturn("Withdraw Successful");

        mockMvc.perform(post("/transactions/user/withdraw")
                .header("X-User-Id", "1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void transfer_success() throws Exception {

        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setTargetAccountNumber("ACC999");
        dto.setAmount(200.0);

        when(service.transfer(1, "ACC999", 200.0))
                .thenReturn("Transfer Successful");

        mockMvc.perform(post("/transactions/user/transfer")
                .header("X-User-Id", "1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    // =========================
    // HEADER ERRORS (VERY IMPORTANT)
    // =========================
    @Test
    void missing_header_should_throw() throws Exception {

        DepositeDTO dto = new DepositeDTO();
        dto.setAmount(100.0);

        mockMvc.perform(post("/transactions/user/deposit")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized()); // 401
    }
    @Test
    void invalid_header_format_should_throw() throws Exception {

        DepositeDTO dto = new DepositeDTO();
        dto.setAmount(100.0);

        mockMvc.perform(post("/transactions/user/deposit")
                .header("X-User-Id", "abc")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // 400
    }

    // =========================
    // SERVICE EXCEPTION TESTS
    // =========================

    @Test
    void unauthorized_exception() throws Exception {

        DepositeDTO dto = new DepositeDTO();
        dto.setAmount(100.0);

        when(service.deposit(1, 100.0))
                .thenThrow(new UnauthorizedException("Account not found"));

        mockMvc.perform(post("/transactions/user/deposit")
                .header("X-User-Id", "1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Account not found"));
    }

    @Test
    void insufficient_balance_exception() throws Exception {

        DepositeDTO dto = new DepositeDTO();
        dto.setAmount(500.0);

        when(service.withdraw(1, 500.0))
                .thenThrow(new InsufficientBalanceException("Insufficient balance"));

        mockMvc.perform(post("/transactions/user/withdraw")
                .header("X-User-Id", "1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void illegal_argument_exception() throws Exception {

        DepositeDTO dto = new DepositeDTO();
        dto.setAmount(-10.0);

        when(service.deposit(1, -10.0))
                .thenThrow(new IllegalArgumentException("Invalid amount"));

        mockMvc.perform(post("/transactions/user/deposit")
                .header("X-User-Id", "1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid amount"));
    }

    @Test
    void generic_exception() throws Exception {

        DepositeDTO dto = new DepositeDTO();
        dto.setAmount(100.0);

        when(service.deposit(1, 100.0))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(post("/transactions/user/deposit")
                .header("X-User-Id", "1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    // =========================
    // GET APIs
    // =========================

    @Test
    void get_my_transactions() throws Exception {

        when(service.getMyTransactions(1)).thenReturn(List.of());

        mockMvc.perform(get("/transactions/user/me")
                .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void get_all_transactions() throws Exception {

        when(service.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/transactions/admin"))
                .andExpect(status().isOk());
    }

    @Test
    void get_by_account() throws Exception {

        when(service.getByAccount("ACC123")).thenReturn(List.of());

        mockMvc.perform(get("/transactions/admin/ACC123"))
                .andExpect(status().isOk());
    }
}