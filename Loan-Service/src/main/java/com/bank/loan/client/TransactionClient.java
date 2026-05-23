package com.bank.loan.client;

import com.bank.loan.dto.TransactionRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "BANK-TRANSACTION-SERVICE")  // ⚠️ match exactly
public interface TransactionClient {

    @PostMapping("/transactions/internal/log")
    void logTransaction(@RequestBody TransactionRequestDTO dto);  // ✅ no static, no body
}