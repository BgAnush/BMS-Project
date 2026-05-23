package com.bank.transaction.service;

import com.bank.transaction.client.AccountClient;
import com.bank.transaction.dto.*;
import com.bank.transaction.exception.*;
import com.bank.transaction.model.Transaction;
import com.bank.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repo;
    private final AccountClient accountClient;

    private static final String DEPOSIT = "DEPOSIT";
    private static final String WITHDRAW = "WITHDRAW";
    private static final String TRANSFER = "TRANSFER";

    // ================= COMMON =================
    private AccountResponseDTO getAccountByUserId(Integer userId) {

        if (userId == null) {
            throw new UnauthorizedException("UserId cannot be null");
        }

        AccountResponseDTO account = accountClient.getAccountByUserId(userId);

        if (account == null || account.getAccountNumber() == null) {
            throw new UnauthorizedException("Account not found");
        }

        return account;
    }

    // ================= DEPOSIT =================
    @Transactional
    public String deposit(Integer userId, Double amount) {

        validateAmount(amount);

        AccountResponseDTO acc = getAccountByUserId(userId);

        accountClient.deposit(acc.getAccountNumber(), amount);

        saveRecord(acc.getAccountNumber(), null, amount, DEPOSIT);

        return "Deposit Successful";
    }

    // ================= WITHDRAW =================
    @Transactional
    public String withdraw(Integer userId, Double amount) {

        validateAmount(amount);

        AccountResponseDTO acc = getAccountByUserId(userId);

        if (acc.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        accountClient.withdraw(acc.getAccountNumber(), amount);

        saveRecord(acc.getAccountNumber(), null, amount, WITHDRAW);

        return "Withdraw Successful";
    }

    // ================= TRANSFER =================
    @Transactional
    public String transfer(Integer userId, String targetAcc, Double amount) {

        validateAmount(amount);

        AccountResponseDTO sender = getAccountByUserId(userId);

        if (targetAcc == null || targetAcc.isBlank()) {
            throw new IllegalArgumentException("Target account required");
        }

        AccountResponseDTO target = accountClient.getInternal(targetAcc);

        if (target == null) {
            throw new UnauthorizedException("Target account not found");
        }

        if (sender.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        accountClient.withdraw(sender.getAccountNumber(), amount);
        accountClient.deposit(targetAcc, amount);

        // ✅ Save sender record (DEBIT)
        saveRecord(sender.getAccountNumber(), targetAcc, amount, TRANSFER);

        // ✅ Save receiver record (CREDIT)
        saveRecord(targetAcc, sender.getAccountNumber(), amount, TRANSFER);

        return "Transfer Successful";
    }

    // ================= INTERNAL =================
    @Transactional
    public void saveRecord(TransactionInternalDTO dto) {

        saveRecord(
                dto.getAccountNumber(),
                dto.getTargetAccountNumber(),
                dto.getAmount(),
                dto.getType()
        );
    }

    // ================= SAVE =================
    private void saveRecord(String acc, String target, Double amt, String type) {

        Transaction t = new Transaction();
        t.setAccountNumber(acc);
        t.setTargetAccountNumber(target);
        t.setAmount(amt);
        t.setType(type);
        t.setTransactionDate(LocalDateTime.now());

        repo.save(t);
    }

    // ================= USER =================
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getMyTransactions(Integer userId) {

        String userAccount = getAccountByUserId(userId).getAccountNumber();

        return repo.findByAccountNumberOrTargetAccountNumber(userAccount, userAccount)
                .stream()
                .map(t -> mapToDTO(t, userAccount))
                .toList();
    }

    // ================= ADMIN =================
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getAll() {

        return repo.findAll()
                .stream()
                .map(t -> mapToDTO(t, t.getAccountNumber()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getByAccount(String accountNumber) {

        return repo.findByAccountNumberOrTargetAccountNumber(accountNumber, accountNumber)
                .stream()
                .map(t -> mapToDTO(t, accountNumber))
                .toList();
    }

    // ================= VALIDATION =================
    private void validateAmount(Double amount) {

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }

    // ================= MAPPER =================
    private TransactionResponseDTO mapToDTO(Transaction t, String userAccount) {

        String direction;

        // Incoming money
        if (t.getTargetAccountNumber() != null &&
            t.getTargetAccountNumber().equals(userAccount)) {

            direction = "CREDIT";

        // Outgoing money
        } else if (t.getAccountNumber() != null &&
                   t.getAccountNumber().equals(userAccount)) {

            direction = "DEBIT";

        } else {
            direction = "UNKNOWN";
        }

        return new TransactionResponseDTO(
                t.getId(),
                t.getAccountNumber(),
                t.getTargetAccountNumber(),
                t.getAmount(),
                direction + " - " + t.getType(),
                t.getTransactionDate()
        );
    }
}