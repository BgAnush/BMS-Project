package com.bank.loan;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bank.loan.controller.LoanController;
import com.bank.loan.dto.LoanRequestDTO;
import com.bank.loan.dto.LoanResponseDTO;
import com.bank.loan.entity.Loan;
import com.bank.loan.service.LoanService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService service;

    @Autowired
    private ObjectMapper mapper;

    // ✅ APPLY LOAN
    @Test
    void applyLoan_success() throws Exception {

        LoanRequestDTO dto = new LoanRequestDTO(50000.0, 12, "PERSONAL");

        LoanResponseDTO response = new LoanResponseDTO(1L, 50000.0, "PENDING");

        Mockito.when(service.applyLoan(Mockito.eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/loan/user/apply")
                        .header("X-User-Id", "1")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // ❌ APPLY LOAN WITHOUT HEADER
    @Test
    void applyLoan_missingHeader() throws Exception {

        LoanRequestDTO dto = new LoanRequestDTO(50000.0, 12, "PERSONAL");

        mockMvc.perform(post("/loan/user/apply")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // ✅ FIXED
    }

    // ✅ GET USER LOANS
    @Test
    void getLoans_success() throws Exception {

        Loan loan = new Loan();
        loan.setLoanId(1L);

        Mockito.when(service.getUserLoans(1L))
                .thenReturn(List.of(loan));

        mockMvc.perform(get("/loan/user/my-loans")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ✅ APPROVE LOAN
    @Test
    void approveLoan_success() throws Exception {

        Loan loan = new Loan();
        loan.setStatus("ACTIVE");

        Mockito.when(service.approveLoan("LN-1")).thenReturn(loan);

        mockMvc.perform(put("/loan/manager/approve/LN-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ✅ DELETE LOAN
    @Test
    void deleteLoan_success() throws Exception {

        mockMvc.perform(delete("/loan/admin/delete/LN-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted successfully"));
    }
    
    @Test
    void testCloseLoan_user_success() throws Exception {

        when(service.closeLoan(1L, "LN-1"))
                .thenReturn("Loan closed successfully");

        mockMvc.perform(put("/loan/user/close/LN-1")
                        .header("X-User-Id", "1")
                        .header("X-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan closed successfully"));
    }
    // ================= ADMIN GET ALL =================

    @Test
    void testAdminGetAllLoans() throws Exception {

        when(service.getAllLoans())
                .thenReturn(List.of(new Loan()));

        mockMvc.perform(get("/loan/admin/all")
                        .header("X-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    // ================= MANAGER PENDING =================

    @Test
    void testManagerPendingLoans() throws Exception {

        when(service.getPendingLoans())
                .thenReturn(List.of(new Loan()));

        mockMvc.perform(get("/loan/manager/pending")
                        .header("X-Role", "MANAGER"))
                .andExpect(status().isOk());
    }

    // ================= MANAGER REJECT LOAN =================

    @Test
    void testManagerRejectLoan() throws Exception {

        Loan loan = Loan.builder()
                .loanNumber("LN-1")
                .status("REJECTED")
                .build();

        when(service.rejectLoan("LN-1")).thenReturn(loan);

        mockMvc.perform(put("/loan/manager/reject/LN-1")
                        .header("X-Role", "MANAGER"))
                .andExpect(status().isOk());
    }
}