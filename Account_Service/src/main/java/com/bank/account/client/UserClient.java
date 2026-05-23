package com.bank.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.bank.account.dto.UserResponseDTO;

@FeignClient(name = "BANK-USER-SERVICE")
public interface UserClient {

    // 🔥 Get user by userId
    @GetMapping("/users/admin/{userId}")
    UserResponseDTO getUserById(@PathVariable("userId") Integer userId);

    // 🔥 Get user by account number
    @GetMapping("/users/account/{accountNumber}")
    UserResponseDTO getByAccountNumber(@PathVariable("accountNumber") String accountNumber);

    // 🔥 Delete user (used when account deleted)
    @DeleteMapping("/users/admin/{userId}")
    void deleteUser(@PathVariable("userId") Integer userId);
    
}