package com.bank.account;

import com.bank.account.client.UserClient;
import com.bank.account.dto.*;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.InsufficientBalanceException;
import com.bank.account.model.Account;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository repo;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private AccountService service;

    private Account account;

    @BeforeEach
    void setup() {
        account = Account.builder()
                .accountNumber("123")
                .userId(1)
                .accType("savings")
                .balance(1000.0)
                .branchLocation("BLR")
                .cardNumber("1111222233334444")
                .interestRate(5.1)
                .active(true)
                .build();
    }

    // ================= CREATE =================
    @Test
    void createAccount_success() {

        AccountRequestDTO dto = new AccountRequestDTO();
        dto.setAccountNumber("123");
        dto.setUserId(1);
        dto.setAccType("savings");

        UserResponseDTO user = new UserResponseDTO();
        user.setAccountNumber("123");

        when(userClient.getUserById(1)).thenReturn(user);
        when(repo.existsByAccountNumber("123")).thenReturn(false);
        when(repo.save(any())).thenReturn(account);

        AccountResponseDTO res = service.createAccount(dto);

        assertEquals("123", res.getAccountNumber());
    }

    // ================= DEPOSIT =================
    @Test
    void deposit_success() {

        when(repo.findByAccountNumber("123"))
                .thenReturn(Optional.of(account));

        when(repo.save(any(Account.class))).thenReturn(account);

        String res = service.deposit("123", 500.0);

        assertEquals(
            "Deposited successfully. New Balance: 1500.0",
            res
        );
    }
    
    

    

    // ================= GET =================
    @Test
    void getByAccount_success() {

        when(repo.findByAccountNumber("123"))
                .thenReturn(Optional.of(account));

        AccountResponseDTO res = service.getByAccountNumber("123");

        assertEquals("123", res.getAccountNumber());
    }

    @Test
    void getByAccount_notFound() {

        when(repo.findByAccountNumber("123"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.getByAccountNumber("123"));
    }

    // ================= USER =================
    @Test
    void getByUser_success() {

        when(repo.findByUserId(1)).thenReturn(List.of(account));

        List<AccountResponseDTO> res = service.getByUserId(1);

        assertEquals(1, res.size());
    }

    @Test
    void getByUser_notFound() {

        when(repo.findByUserId(1)).thenReturn(List.of());

        assertThrows(AccountNotFoundException.class,
                () -> service.getByUserId(1));
    }

    // ================= UPDATE =================
    @Test
    void update_success() {

        AccountRequestDTO dto = new AccountRequestDTO();
        dto.setAccType("current");

        when(repo.findByAccountNumber("123"))
                .thenReturn(Optional.of(account));

        when(repo.save(any())).thenReturn(account);

        AccountResponseDTO res = service.updateAccount("123", dto);

        assertNotNull(res);
    }

    // ================= DELETE =================
    @Test
    void delete_success() {

        when(repo.findByAccountNumber("123"))
                .thenReturn(Optional.of(account));

        doNothing().when(repo).delete(account);
        doNothing().when(userClient).deleteUser(1);

        String res = service.deleteAccount("123");

        assertEquals("Account deleted successfully", res);
    }

    
    @Test
    void deposit_invalidAmount() {

        assertThrows(ResponseStatusException.class,
                () -> service.deposit("123", -1.0));
    }

    // ================= WITHDRAW =================
    @Test
    void withdraw_success() {

        when(repo.findByAccountNumber("123"))
                .thenReturn(Optional.of(account));

        when(repo.save(any())).thenReturn(account);

        String res = service.withdraw("123", 200.0);

        assertTrue(res.contains("Withdraw"));
    }

    @Test
    void withdraw_insufficient() {

        when(repo.findByAccountNumber("123"))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientBalanceException.class,
                () -> service.withdraw("123", 99999.0)
        );

        verify(repo, never()).save(any());
    }
}