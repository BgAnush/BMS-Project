package com.bank.user;

import com.bank.user.controller.UserInternalController;
import com.bank.user.dto.UserRequest;
import com.bank.user.entity.BankUser;
import com.bank.user.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserInternalControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService service;

    @Test
    void createInternal() throws Exception {

        BankUser u = new BankUser();
        u.setProfileId(1);

        when(service.createUser(any(UserRequest.class))).thenReturn(u);

        mockMvc.perform(post("/users/internal")
                        .contentType("application/json")
                        .content("""
                                {
                                  "profileId":1,
                                  "customerId":"C1",
                                  "fullName":"A",
                                  "emailId":"a@gmail.com",
                                  "mobileNumber":"9876543210",
                                  "gender":"M",
                                  "address":"BLR",
                                  "dateOfBirth":"2000-01-01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(1));
    }
}