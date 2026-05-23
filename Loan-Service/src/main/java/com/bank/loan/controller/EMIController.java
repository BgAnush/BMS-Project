package com.bank.loan.controller;

import com.bank.loan.dto.EmiResponseDTO;
import com.bank.loan.entity.EMI;
import com.bank.loan.exception.BadRequestException;
import com.bank.loan.service.EmiService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/loan/emi")
@RequiredArgsConstructor
public class EMIController {

    private final EmiService service;

    // ================= USER =================

    @GetMapping("/user/{loanNumber}")
    public ResponseEntity<List<EmiResponseDTO>> getUserEmi(
            @PathVariable String loanNumber,
            HttpServletRequest request) {

        Long userId = extractUserId(request);

        log.info("Fetching EMI schedule | userId={} loanNumber={}", userId, loanNumber);

        return ResponseEntity.ok(service.getEmiSchedule(userId, loanNumber));
    }

    @PostMapping("/user/pay/{loanNumber}/{emiNumber}")
    public ResponseEntity<String> payEmi(
            @PathVariable String loanNumber,
            @PathVariable Integer emiNumber,
            HttpServletRequest request) {

        Long userId = extractUserId(request);

        log.info("EMI payment request | userId={} loanNumber={} emiNumber={}",
                userId, loanNumber, emiNumber);

        return ResponseEntity.ok(service.payEmi(userId, loanNumber, emiNumber));
    }

    @GetMapping("/user/current")
    public ResponseEntity<List<EmiResponseDTO>> currentMonth(HttpServletRequest request) {

        Long userId = extractUserId(request);

        log.info("Fetching current month EMI | userId={}", userId);

        return ResponseEntity.ok(service.getCurrentMonthEmi(userId));
    }

    // ================= ADMIN =================

    @GetMapping("/admin/all")
    public ResponseEntity<List<EMI>> getAll() {

        log.info("Admin fetching ALL EMI records");

        return ResponseEntity.ok(service.getAllEmis());
    }

    // ================= COMMON =================

    private Long extractUserId(HttpServletRequest request) {

        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new BadRequestException("Unauthorized: Missing user header");
        }

        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.error("Invalid X-User-Id format: {}", userId);
            throw new BadRequestException("Invalid format for X-User-Id header");
        }
    }
}