package com.bank.loan.controller;

import com.bank.loan.dto.*;
import com.bank.loan.entity.Loan;
import com.bank.loan.exception.BadRequestException;
import com.bank.loan.service.LoanService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/loan")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService service;

    // ================= USER =================

    @PostMapping("/user/apply")
    public ResponseEntity<LoanResponseDTO> applyLoan(
            HttpServletRequest request,
            @Valid @RequestBody LoanRequestDTO dto) {

        Long userId = extractUserId(request);

        log.info("Loan apply request | userId={} amount={} type={}",
                userId, dto.getAmount(), dto.getLoanType());

        LoanResponseDTO response = service.applyLoan(userId, dto);

        log.info("Loan applied successfully | userId={}", userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/my-loans")
    public ResponseEntity<List<Loan>> getLoans(HttpServletRequest request) {

        Long userId = extractUserId(request);

        log.info("Fetching user loans | userId={}", userId);

        List<Loan> loans = service.getUserLoans(userId);

        log.info("Loans fetched | userId={} count={}", userId, loans.size());

        return ResponseEntity.ok(loans);
    }

    // ================= MANAGER =================

    @PutMapping("/manager/approve/{loanNumber}")
    public ResponseEntity<Loan> approve(@PathVariable String loanNumber) {

        log.info("Loan approval request | loanNumber={}", loanNumber);

        Loan loan = service.approveLoan(loanNumber);

        log.info("Loan approved | loanNumber={}", loanNumber);

        return ResponseEntity.ok(loan);
    }

    @PutMapping("/manager/reject/{loanNumber}")
    public ResponseEntity<Loan> reject(@PathVariable String loanNumber) {

        log.warn("Loan rejection request | loanNumber={}", loanNumber);

        Loan loan = service.rejectLoan(loanNumber);

        log.warn("Loan rejected | loanNumber={}", loanNumber);

        return ResponseEntity.ok(loan);
    }

    @GetMapping("/manager/pending")
    public ResponseEntity<List<Loan>> pending() {

        log.info("Fetching pending loans");

        List<Loan> list = service.getPendingLoans();

        log.info("Pending loans count={}", list.size());

        return ResponseEntity.ok(list);
    }

    // ================= ADMIN =================

    @GetMapping("/admin/all")
    public ResponseEntity<List<Loan>> all() {

        log.info("Admin fetching all loans");

        List<Loan> list = service.getAllLoans();

        log.info("Total loans count={}", list.size());

        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/admin/delete/{loanNumber}")
    public ResponseEntity<String> delete(@PathVariable String loanNumber) {

        log.error("Loan delete request | loanNumber={}", loanNumber);

        service.deleteLoan(loanNumber);

        log.error("Loan deleted | loanNumber={}", loanNumber);

        return ResponseEntity.ok("Deleted successfully");
    }

    // ================= USER CLOSE =================

    @PutMapping("/user/close/{loanNumber}")
    public ResponseEntity<String> closeLoan(
            @PathVariable String loanNumber,
            HttpServletRequest request) {

        Long userId = extractUserId(request);

        log.info("Loan close request | userId={} loanNumber={}", userId, loanNumber);

        String response = service.closeLoan(userId, loanNumber);

        log.info("Loan closed successfully | userId={} loanNumber={}", userId, loanNumber);

        return ResponseEntity.ok(response);
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
            throw new BadRequestException("Invalid userId format");
        }
    }
}