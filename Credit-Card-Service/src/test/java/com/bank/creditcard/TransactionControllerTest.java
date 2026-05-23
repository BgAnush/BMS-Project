package com.bank.creditcard;

import com.bank.card.CreditCardServiceApplication;
import com.bank.card.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CreditCardServiceApplication.class)
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService service;

    @Test
    void transaction_success() throws Exception {

        when(service.process(anyLong(), any()))
                .thenReturn("TRANSACTION SUCCESSFUL");

        mockMvc.perform(post("/credit-card/user/transaction")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "cardNumber":"1111",
                          "amount":100,
                          "pin":"1234",
                          "merchant":"Amazon",
                          "type":"PURCHASE"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(content().string("TRANSACTION SUCCESSFUL"));
    }
}