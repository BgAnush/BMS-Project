package com.bank.account.service;

import com.bank.account.client.UserClient;
import com.bank.account.constant.AccountConstants;
import com.bank.account.dto.AccountRequestDTO;
import com.bank.account.dto.AccountResponseDTO;
import com.bank.account.dto.UserResponseDTO;
import com.bank.account.exception.*;
import com.bank.account.model.Account;
import com.bank.account.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repo;
    private final UserClient userClient;

    // ---------------- CREATE ACCOUNT ----------------
    @Transactional
    public AccountResponseDTO createAccount(AccountRequestDTO dto) {

        log.info("Create account request for userId: {}", dto.getUserId());

        UserResponseDTO user;

        try {
            user = userClient.getUserById(dto.getUserId());
            log.info("User fetched successfully for userId: {}", dto.getUserId());
        } catch (Exception e) {
            log.error("User Service unavailable for userId: {}", dto.getUserId(), e);
            throw new UserServiceUnavailableException("User Service unavailable");
        }

        if (user == null || !user.getAccountNumber().equals(dto.getAccountNumber())) {
            log.warn("User validation failed for userId: {}", dto.getUserId());
            throw new UserValidationException("User validation failed");
        }

        if (repo.existsByAccountNumber(dto.getAccountNumber())) {
            log.warn("Account already exists: {}", dto.getAccountNumber());
            throw new BankException("Account already exists", HttpStatus.CONFLICT);
        }

        Account acc = Account.builder()
                .accountNumber(dto.getAccountNumber())
                .userId(dto.getUserId())
                .accType(dto.getAccType())
                .balance(dto.getBalance() == null ? 0.0 : dto.getBalance())
                .branchLocation(dto.getBranchLocation())
                .cardNumber(generateCardNumber())
                .interestRate(getInterestRate(dto.getAccType()))
                .openingDate(LocalDateTime.now())
                .active(true)
                .build();

        Account saved = repo.save(acc);

        log.info("Account created successfully: {}", saved.getAccountNumber());

        return mapToDTO(saved);
    }

    // ---------------- GET ACCOUNT ----------------
    public AccountResponseDTO getByAccountNumber(String accountNumber) {

        log.info("Fetching account: {}", accountNumber);

        return repo.findByAccountNumber(accountNumber)
                .map(acc -> {
                    log.info("Account found: {}", accountNumber);
                    return mapToDTO(acc);
                })
                .orElseThrow(() -> {
                    log.error("Account not found: {}", accountNumber);
                    return new AccountNotFoundException(AccountConstants.ACCOUNT_NOT_FOUND);
                });
    }

    public List<AccountResponseDTO> getAllAccounts() {
        log.info("Fetching all accounts");
        return repo.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<AccountResponseDTO> getByUserId(Integer userId) {

        log.info("Fetching accounts for userId: {}", userId);

        List<Account> accounts = repo.findByUserId(userId);

        if (accounts.isEmpty()) {
            log.warn("No accounts found for userId: {}", userId);
            throw new AccountNotFoundException(
                    AccountConstants.ACCOUNT_NOT_FOUND + " for userId: " + userId);
        }

        return accounts.stream().map(this::mapToDTO).toList();
    }

    // ---------------- UPDATE ----------------
    @Transactional
    public AccountResponseDTO updateAccount(String accountNumber, AccountRequestDTO dto) {

        log.info("Updating account: {}", accountNumber);

        Account acc = repo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account not found for update: {}", accountNumber);
                    return new AccountNotFoundException(AccountConstants.ACCOUNT_NOT_FOUND);
                });

        if (dto.getAccType() != null) {
            acc.setAccType(dto.getAccType());
            acc.setInterestRate(getInterestRate(dto.getAccType()));
        }

        if (dto.getBranchLocation() != null) {
            acc.setBranchLocation(dto.getBranchLocation());
        }

        if (dto.getBalance() != null) {
            acc.setBalance(dto.getBalance());
        }

        Account updated = repo.save(acc);

        log.info("Account updated successfully: {}", accountNumber);

        return mapToDTO(updated);
    }

    // ---------------- DELETE ----------------
    @Transactional
    public String deleteAccount(String accountNumber) {

        log.info("Deleting account: {}", accountNumber);

        Account acc = repo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account not found for deletion: {}", accountNumber);
                    return new AccountNotFoundException(AccountConstants.ACCOUNT_NOT_FOUND);
                });

        repo.delete(acc);

        try {
            userClient.deleteUser(acc.getUserId());
            log.info("User deleted from User Service: {}", acc.getUserId());
        } catch (Exception e) {
            log.warn("User Service delete failed for userId: {}", acc.getUserId(), e);
        }

        log.info("Account deleted successfully: {}", accountNumber);

        return "Account deleted successfully";
    }

    // ---------------- DEPOSIT ----------------
    @Transactional
    public String deposit(String accountNumber, Double amount) {

        log.info("Deposit request: {} amount: {}", accountNumber, amount);

        if (amount == null || amount <= 0) {
            log.warn("Invalid deposit amount: {}", amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }

        Account acc = repo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account not found for deposit: {}", accountNumber);
                    return new AccountNotFoundException(AccountConstants.ACCOUNT_NOT_FOUND);
                });

        acc.setBalance(acc.getBalance() + amount);
        repo.save(acc);

        log.info("Deposit successful. New balance: {}", acc.getBalance());

        return "Deposited successfully. New Balance: " + acc.getBalance();
    }

    // ---------------- WITHDRAW ----------------
    @Transactional
    public String withdraw(String accountNumber, Double amount) {

        log.info("Withdraw request: {} amount: {}", accountNumber, amount);

        Account acc = repo.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account not found for withdrawal: {}", accountNumber);
                    return new AccountNotFoundException(AccountConstants.ACCOUNT_NOT_FOUND);
                });

        if (acc.getBalance() < amount) {
            log.warn("Insufficient balance for account: {}", accountNumber);
            throw new InsufficientBalanceException("Insufficient balance");
        }

        acc.setBalance(acc.getBalance() - amount);
        repo.save(acc);

        log.info("Withdraw successful. Remaining balance: {}", acc.getBalance());

        return "Withdraw successful. Balance: " + acc.getBalance();
    }

    // ---------------- UTIL METHODS ----------------
    private AccountResponseDTO mapToDTO(Account acc) {
        return new AccountResponseDTO(
                acc.getAccountNumber(),
                acc.getUserId(),
                acc.getAccType(),
                acc.getBalance(),
                acc.getInterestRate(),
                acc.getBranchLocation(),
                acc.getOpeningDate(),
                acc.getCardNumber(),
                acc.getActive()
        );
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));

            if ((i + 1) % 4 == 0 && i != 15) {
                sb.append("-");
            }
        }

        return sb.toString();
    }

    private Double getInterestRate(String type) {
        return "savings".equalsIgnoreCase(type) ? 5.1 : 2.5;
    }
}