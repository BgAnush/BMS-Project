package com.bank.account.controller;

import com.bank.account.dto.AccountRequestDTO;
import com.bank.account.dto.AccountResponseDTO;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.UserValidationException;
import com.bank.account.constant.AccountConstants;
import com.bank.account.service.AccountService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService service;

    // ================= HEADER EXTRACTION =================

    private Integer extractUserId(HttpServletRequest request) {

        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new UserValidationException("Unauthorized request: missing user identity");
        }

        try {
            return Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            throw new UserValidationException("Invalid user ID format in header");
        }
    }

    // ================= ADMIN =================

    @PostMapping("/admin")
    public AccountResponseDTO create(@RequestBody AccountRequestDTO dto) {
        return service.createAccount(dto);
    }

    @DeleteMapping("/admin/{accountNumber}")
    public String delete(@PathVariable String accountNumber) {
        return service.deleteAccount(accountNumber);
    }

    @GetMapping("/admin")
    public List<AccountResponseDTO> getAll() {
        return service.getAllAccounts();
    }

    @PutMapping("/admin/{accountNumber}")
    public AccountResponseDTO update(@PathVariable String accountNumber,
                                     @RequestBody AccountRequestDTO dto) {
        return service.updateAccount(accountNumber, dto);
    }

    @GetMapping("/admin/user/{accountNumber}")
    public AccountResponseDTO getByAccount(@PathVariable String accountNumber) {
        return service.getByAccountNumber(accountNumber);
    }

    // ================= MANAGER =================

    @GetMapping("/manager/user/{userId}")
    public List<AccountResponseDTO> getByUser(@PathVariable Integer userId) {
        return service.getByUserId(userId);
    }

    // ================= USER =================

    @GetMapping("/user/me")
    public AccountResponseDTO getMyAccount(HttpServletRequest request) {

        Integer userId = extractUserId(request);

        return service.getByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new AccountNotFoundException(AccountConstants.ACCOUNT_NOT_FOUND));
    }

    // ================= INTERNAL (FEIGN / MICROSERVICE) =================

    @Hidden
    @GetMapping("/internal/user/{userId}")
    public AccountResponseDTO getByUserInternal(@PathVariable Integer userId) {
        return service.getByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new AccountNotFoundException(AccountConstants.ACCOUNT_NOT_FOUND));
    }

    @Hidden
    @PutMapping("/internal/{accountNumber}/deposit")
    public String deposit(@PathVariable String accountNumber,
                          @RequestParam Double amount) {
        return service.deposit(accountNumber, amount);
    }

    @Hidden
    @PutMapping("/internal/{accountNumber}/withdraw")
    public String withdraw(@PathVariable String accountNumber,
                           @RequestParam Double amount) {
        return service.withdraw(accountNumber, amount);
    }

    @Hidden
    @GetMapping("/internal/{accountNumber}")
    public AccountResponseDTO getAccount(@PathVariable String accountNumber) {
        return service.getByAccountNumber(accountNumber);
    }
}