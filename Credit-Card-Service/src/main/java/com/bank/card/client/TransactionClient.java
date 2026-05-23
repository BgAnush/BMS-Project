package com.bank.card.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bank.card.dto.TransactionRequestDTO;

@FeignClient(name = "BANK-TRANSACTION-SERVICE")
public interface TransactionClient {

    @PostMapping("/transactions/internal/log")
    void logTransaction(@RequestBody TransactionRequestDTO dto);
}