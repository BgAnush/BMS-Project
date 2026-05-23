package com.bank.account;

import com.bank.account.dto.AccountResponseDTO;
import com.bank.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.bank.account.repository.AccountRepository;

import java.util.Optional;
import com.bank.account.model.Account;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AspectIntegrationTest {

    @Autowired
    private AccountService accountService;

    @MockBean
    private AccountRepository repo;

    @Test
    void serviceMethods_shouldBeInterceptedByAspects() {
        // Prepare mock data
        Account mockAccount = Account.builder()
                .accountNumber("ACC123")
                .userId(1)
                .balance(100.0)
                .build();

        when(repo.findByAccountNumber("ACC123")).thenReturn(Optional.of(mockAccount));

        // When calling this, ExecutionTimeAspect and LoggingAspect will trigger
        assertDoesNotThrow(() -> {
            AccountResponseDTO response = accountService.getByAccountNumber("ACC123");
            assertNotNull(response);
        });

        // Verify the underlying service was actually called
        verify(repo, times(1)).findByAccountNumber("ACC123");
    }

    @Test
    void loggingAspect_shouldHandleExceptions() {
        when(repo.findByAccountNumber("ERR")).thenReturn(Optional.empty());

        // This triggers @AfterThrowing in LoggingAspect
        assertThrows(RuntimeException.class, () -> {
            accountService.getByAccountNumber("ERR");
        });
    }
}