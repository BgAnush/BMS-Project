package com.bank.creditcard;

import com.bank.card.CreditCardServiceApplication;
import com.bank.card.model.CreditCard;
import com.bank.card.service.CreditCardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CreditCardServiceApplication.class)
@AutoConfigureMockMvc
class CreditCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreditCardService service;

    @Test
    void requestCard_api_success() throws Exception {

        CreditCard card = new CreditCard();
        card.setId(1L);

        when(service.requestCard(anyLong(), any()))
                .thenReturn(card);

        mockMvc.perform(post("/credit-card/user/request")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"type":"GOLD"}
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void getUserCards_api_success() throws Exception {

        when(service.getUserCards(1L))
                .thenReturn(List.of(new CreditCard()));

        mockMvc.perform(get("/credit-card/user/cards")
                .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }
}