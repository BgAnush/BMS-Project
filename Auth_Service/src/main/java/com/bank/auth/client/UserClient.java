package com.bank.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import com.bank.auth.dto.UserRequest;

import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(name = "BANK-USER-SERVICE")
public interface UserClient {

    @PostMapping("/users/internal")
    void createUser(@RequestBody UserRequest userRequest);
}