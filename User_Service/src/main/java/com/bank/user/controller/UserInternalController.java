package com.bank.user.controller;

import com.bank.user.dto.UserRequest;
import com.bank.user.entity.BankUser;
import com.bank.user.service.UserService;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/internal")
@Hidden
@Slf4j
public class UserInternalController {

    private final UserService service;

    public UserInternalController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public BankUser createUser(@RequestBody UserRequest req) {

        log.info("INTERNAL API CALL: Creating new user with email={}", req.getEmailId());

        BankUser createdUser = service.createUser(req);

        log.info("User created successfully with id={}", createdUser.getProfileId());

        return createdUser;
    }
}