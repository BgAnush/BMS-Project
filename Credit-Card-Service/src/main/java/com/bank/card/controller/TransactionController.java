package com.bank.card.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bank.card.dto.CardBillResponseDTO;
import com.bank.card.dto.TransactionDTO;
import com.bank.card.exception.UnauthorizedException;
import com.bank.card.model.CardTransaction;
import com.bank.card.service.TransactionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/credit-card")
@AllArgsConstructor
public class TransactionController {

    private final TransactionService service;

    // ================= TRANSACTION =================
    @PostMapping("/user/transaction")
    public ResponseEntity<String> transact(
            HttpServletRequest request,
            @Valid @RequestBody TransactionDTO dto) {

        Long userId = extractUserId(request);

        log.info("Card transaction request by userId: {} amount: {} type: {}",
                userId, dto.getAmount(), dto.getType());

        return ResponseEntity.ok(service.process(userId, dto));
    }

    // ================= USER TRANSACTIONS =================
    @GetMapping("/user/transactions")
    public ResponseEntity<List<CardTransaction>> getUserTransactions(HttpServletRequest request) {

        Long userId = extractUserId(request);

        log.info("Fetching card transactions for userId: {}", userId);

        return ResponseEntity.ok(service.getUserTransactions(userId));
    }

    // ================= ADMIN =================
    @GetMapping("/admin/transactions")
    public ResponseEntity<List<CardTransaction>> getAllTransactions() {

        log.info("Admin fetching ALL card transactions");

        return ResponseEntity.ok(service.getAllTransactions());
    }

    // ================= BILL PAYMENT =================
    @PutMapping("/user/pay")
    public ResponseEntity<CardBillResponseDTO> payBill(
            HttpServletRequest request,
            @RequestParam Double amount) {

        Long userId = extractUserId(request);

        log.info("Bill payment request by userId: {} amount: {}", userId, amount);

        return ResponseEntity.ok(service.payBill(userId, amount));
    }

    // ================= BILL DETAILS =================
    @GetMapping("/user/bill")
    public ResponseEntity<CardBillResponseDTO> getBill(HttpServletRequest request) {

        Long userId = extractUserId(request);

        log.info("Fetching bill details for userId: {}", userId);

        return ResponseEntity.ok(service.getBillDetails(userId));
    }

    // ================= HEADER =================
    private Long extractUserId(HttpServletRequest request) {

        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new UnauthorizedException("Unauthorized request");
        }

        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.error("Invalid X-User-Id format: {}", userId);
            throw new UnauthorizedException("Invalid User ID format");
        }
    }
}