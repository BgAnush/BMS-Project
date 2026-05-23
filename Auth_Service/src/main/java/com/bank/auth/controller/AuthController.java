package com.bank.auth.controller;

import com.bank.auth.dto.*;
import com.bank.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    // =========================================================
    // INTERNAL DELETE
    // =========================================================
    @Hidden
    @DeleteMapping("/internal/{profileId}")
    public void deleteUser(@PathVariable Integer profileId) {

        log.warn("Internal delete request for profileId={}", profileId);
        service.deleteUser(profileId);
    }

    // =========================================================
    // ADMIN REGISTER
    // =========================================================
    @PostMapping(value = "/admin/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public String register(
            @Valid @ModelAttribute RegisterRequest req,
            @RequestPart("image") MultipartFile image
    ) {

        log.info("Admin register API called for email={}", req.getEmail());

        String imagePath = service.saveImage(image);
        return service.register(req, imagePath);
    }

    // =========================================================
    // USER REGISTER (FIXED DTO)
    // =========================================================
    @PostMapping(value = "/user/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String userRegister(
            @Valid @ModelAttribute UserRegisterRequest req,
            @RequestPart("image") MultipartFile image
    ) {

        log.info("User register API called for email={}", req.getEmail());

        String imagePath = service.saveImage(image);
        return service.userRegister(req, imagePath);
    }

    // =========================================================
    // LOGIN
    // =========================================================
    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest req) {

        log.info("Login API called for customerId={}", req.getCustomerId());
        return service.login(req);
    }
}