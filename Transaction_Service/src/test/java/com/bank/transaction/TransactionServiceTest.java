package com.bank.transaction;

import com.bank.transaction.client.AccountClient;
import com.bank.transaction.dto.AccountResponseDTO;
import com.bank.transaction.exception.InsufficientBalanceException;
import com.bank.transaction.exception.UnauthorizedException;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.service.TransactionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @InjectMocks
    private TransactionService service;

    @Mock
    private TransactionRepository repo;

    @Mock
    private AccountClient accountClient;

    private AccountResponseDTO account;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        account = new AccountResponseDTO();
        account.setAccountNumber("ACC123");
        account.setBalance(1000.0);
    }

    // =========================
    // SUCCESS CASES
    // =========================

    @Test
    void deposit_success() {
        when(accountClient.getAccountByUserId(1)).thenReturn(account);

        String result = service.deposit(1, 200.0);

        assertEquals("Deposit Successful", result);
        verify(accountClient).deposit("ACC123", 200.0);
    }

    @Test
    void withdraw_success() {
        when(accountClient.getAccountByUserId(1)).thenReturn(account);

        String result = service.withdraw(1, 200.0);

        assertEquals("Withdraw Successful", result);
    }

    @Test
    void transfer_success() {
        AccountResponseDTO target = new AccountResponseDTO();
        target.setAccountNumber("ACC999");

        when(accountClient.getAccountByUserId(1)).thenReturn(account);
        when(accountClient.getInternal("ACC999")).thenReturn(target);

        String result = service.transfer(1, "ACC999", 200.0);

        assertEquals("Transfer Successful", result);
    }

    // =========================
    // VALIDATION ERRORS
    // =========================

    @Test
    void deposit_invalid_amount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deposit(1, -10.0));
    }

    @Test
    void withdraw_invalid_amount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.withdraw(1, 0.0));
    }

    @Test
    void transfer_invalid_amount() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1, "ACC999", -5.0));
    }

    // =========================
    // ACCOUNT ERRORS
    // =========================

    @Test
    void null_userId_should_throw() {
        assertThrows(UnauthorizedException.class,
                () -> service.deposit(null, 100.0));
    }

    @Test
    void account_not_found_should_throw() {
        when(accountClient.getAccountByUserId(1)).thenReturn(null);

        assertThrows(UnauthorizedException.class,
                () -> service.deposit(1, 100.0));
    }

    // =========================
    // BALANCE ERRORS
    // =========================

    @Test
    void withdraw_insufficient_balance() {
        account.setBalance(100.0);
        when(accountClient.getAccountByUserId(1)).thenReturn(account);

        assertThrows(InsufficientBalanceException.class,
                () -> service.withdraw(1, 500.0));
    }

    @Test
    void transfer_insufficient_balance() {
        account.setBalance(50.0);

        AccountResponseDTO target = new AccountResponseDTO();
        target.setAccountNumber("ACC999");

        when(accountClient.getAccountByUserId(1)).thenReturn(account);
        when(accountClient.getInternal("ACC999")).thenReturn(target);

        assertThrows(InsufficientBalanceException.class,
                () -> service.transfer(1, "ACC999", 200.0));
    }

    // =========================
    // TARGET ACCOUNT ERRORS
    // =========================

    @Test
    void transfer_target_null_should_throw() {
        when(accountClient.getAccountByUserId(1)).thenReturn(account);

        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1, null, 100.0));
    }

    @Test
    void transfer_target_blank_should_throw() {
        when(accountClient.getAccountByUserId(1)).thenReturn(account);

        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1, "", 100.0));
    }

    @Test
    void transfer_target_not_found_should_throw() {
        when(accountClient.getAccountByUserId(1)).thenReturn(account);
        when(accountClient.getInternal("ACC999")).thenReturn(null);

        assertThrows(UnauthorizedException.class,
                () -> service.transfer(1, "ACC999", 100.0));
    }
}