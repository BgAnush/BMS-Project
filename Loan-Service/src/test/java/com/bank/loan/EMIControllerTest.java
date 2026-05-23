package com.bank.loan;

import com.bank.loan.controller.EMIController;
import com.bank.loan.dto.EmiResponseDTO;
import com.bank.loan.entity.EMI;
import com.bank.loan.service.EmiService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EMIController.class)
class EMIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmiService service;

    // ✅ GET EMI SCHEDULE
    @Test
    void getUserEmi_success() throws Exception {

        EmiResponseDTO dto = new EmiResponseDTO(
                1,
                LocalDate.now(),
                1000.0,
                "PENDING"
        );

        Mockito.when(service.getEmiSchedule(anyLong(), Mockito.eq("LN-1")))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/loan/emi/user/LN-1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ✅ PAY EMI
    @Test
    void payEmi_success() throws Exception {

        Mockito.when(service.payEmi(1L, "LN-1", 1))
                .thenReturn("EMI 1 paid successfully");

        mockMvc.perform(post("/loan/emi/user/pay/LN-1/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("EMI 1 paid successfully"));
    }

    // ✅ CURRENT MONTH EMI
    @Test
    void currentMonth_success() throws Exception {

        Mockito.when(service.getCurrentMonthEmi(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/loan/emi/user/current")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    // ✅ ADMIN GET ALL
    @Test
    void getAll_success() throws Exception {

        Mockito.when(service.getAllEmis())
                .thenReturn(List.of(new EMI()));

        mockMvc.perform(get("/loan/emi/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ❌ MISSING HEADER
    @Test
    void getUserEmi_missingHeader() throws Exception {

        mockMvc.perform(get("/loan/emi/user/LN-1"))
                .andExpect(status().isBadRequest()); // ✅ FIXED
    }
}