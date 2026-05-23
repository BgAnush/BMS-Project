package com.bank.transaction.controller;

import com.bank.transaction.dto.TransactionInternalDTO;
import com.bank.transaction.service.TransactionService;

import io.swagger.v3.oas.annotations.Hidden;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/transactions/internal")
@RequiredArgsConstructor
@Hidden
public class TransactionInternalController {

    private final TransactionService service;

    // ================= INTERNAL LOG =================
    @PostMapping("/log")
    public void logTransaction(@RequestBody TransactionInternalDTO dto) {

        if (dto == null || dto.getAccountNumber() == null) {
            log.error("Invalid internal transaction request");
            throw new IllegalArgumentException("Invalid transaction payload");
        }

        log.info("Internal transaction log request | account={} target={} amount={} type={}",
                dto.getAccountNumber(),
                dto.getTargetAccountNumber(),
                dto.getAmount(),
                dto.getType()
        );

        service.saveRecord(dto);

        log.info("Transaction logged successfully");
    }
}