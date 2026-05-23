package com.bank.transaction.controller;

import com.bank.transaction.dto.*;
import com.bank.transaction.exception.InvalidHeaderException;
import com.bank.transaction.exception.UnauthorizedException;
import com.bank.transaction.service.TransactionService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    // ================= USER OPERATIONS =================

    @PostMapping("/user/deposit")
    public String deposit(HttpServletRequest request, @RequestBody DepositeDTO dto) {

        Integer userId = extractUserId(request);
        log.info("Deposit request from userId: {} amount: {}", userId, dto.getAmount());

        return service.deposit(userId, dto.getAmount());
    }

    @PostMapping("/user/withdraw")
    public String withdraw(HttpServletRequest request, @RequestBody DepositeDTO dto) {

        Integer userId = extractUserId(request);
        log.info("Withdraw request from userId: {} amount: {}", userId, dto.getAmount());

        return service.withdraw(userId, dto.getAmount());
    }

    @PostMapping("/user/transfer")
    public String transfer(HttpServletRequest request, @RequestBody TransactionRequestDTO dto) {

        Integer userId = extractUserId(request);
        log.info("Transfer request from userId: {} to {} amount: {}",
                userId, dto.getTargetAccountNumber(), dto.getAmount());

        return service.transfer(
                userId,
                dto.getTargetAccountNumber(),
                dto.getAmount()
        );
    }

    @GetMapping("/user/me")
    public List<TransactionResponseDTO> getMyTransactions(HttpServletRequest request) {

        Integer userId = extractUserId(request);
        log.info("Fetching transactions for userId: {}", userId);

        return service.getMyTransactions(userId);
    }

    // ================= ADMIN =================

    @GetMapping("/admin")
    public List<TransactionResponseDTO> getAll() {

        log.info("Admin fetching all transactions");

        return service.getAll();
    }

    @GetMapping("/admin/{accountNumber}")
    public List<TransactionResponseDTO> getByAccount(@PathVariable String accountNumber) {

        log.info("Admin fetching transactions for account: {}", accountNumber);

        return service.getByAccount(accountNumber);
    }

    // ================= HEADER HANDLING =================

    private Integer extractUserId(HttpServletRequest request) {

        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new UnauthorizedException("Missing X-User-Id header");
        }

        try {
            return Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            log.error("Invalid X-User-Id format: {}", userId);
            throw new InvalidHeaderException("Invalid X-User-Id format");
        }
    }
}