package com.bank.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BANK-AUTH-SERVICE")
public interface AuthClient {

    @DeleteMapping("/auth/internal/{profileId}")
    void deleteAuthUser(@PathVariable Integer profileId);
}