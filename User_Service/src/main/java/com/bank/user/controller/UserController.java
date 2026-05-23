package com.bank.user.controller;

import com.bank.user.dto.UpdateUserRequest;
import com.bank.user.entity.BankUser;
import com.bank.user.exception.UnauthorizedException;
import com.bank.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService service;

    // =========================================================
    // USER ID EXTRACTOR (CLEAN + REUSABLE)
    // =========================================================
    private Long extractUserId(HttpServletRequest request) {

        String userId = request.getHeader("X-User-Id");

        if (userId == null) {
            log.error("Unauthorized access attempt: Missing X-User-Id header");
            throw new UnauthorizedException("Unauthorized request");
        }

        try {
            Long parsedId = Long.parseLong(userId);
            log.debug("Extracted User ID from header: {}", parsedId);
            return parsedId;
        } catch (NumberFormatException e) {
            log.error("Invalid User ID format in header: {}", userId);
            throw new UnauthorizedException("Invalid User ID format");
        }
    }

    // =========================================================
    // SELF PROFILE
    // =========================================================
    @GetMapping("/me")
    public BankUser getMyProfile(HttpServletRequest request) {

        log.info("GET /users/me called");

        Long userId = extractUserId(request);

        BankUser user = service.getUser(userId.intValue());

        log.info("Fetched profile for userId={}", userId);

        return user;
    }

    // =========================================================
    // UPDATE SELF PROFILE
    // =========================================================
    @PatchMapping("/me")
    public BankUser updateMyProfile(
            HttpServletRequest request,
             @RequestBody UpdateUserRequest req) {

        log.info("PATCH /users/me called");

        Long userId = extractUserId(request);

        BankUser updatedUser = service.updateUser(userId.intValue(), req);

        log.info("Updated profile for userId={}", userId);

        return updatedUser;
    }

    // =========================================================
    // ADMIN APIs
    // =========================================================
    @GetMapping("/admin/all")
    public List<BankUser> getAllUsers() {

        log.info("GET /users/all called");

        List<BankUser> users = service.getAllUsers();

        log.info("Fetched {} users", users.size());

        return users;
    }

    @GetMapping("/admin/{id}")
    public BankUser getUserById(@PathVariable Integer id) {

        log.info("GET /users/{} called", id);

        BankUser user = service.getUser(id);

        log.info("Fetched user with id={}", id);

        return user;
    }

    @PatchMapping("/admin/{id}")
    public BankUser adminUpdateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRequest req) {

        log.info("PATCH /users/admin/{} called", id);

        BankUser updatedUser = service.updateUser(id, req);

        log.info("Admin updated user with id={}", id);

        return updatedUser;
    }

    @DeleteMapping("/admin/{id}")
    public void deleteUser(@PathVariable Integer id) {

        log.warn("DELETE /users/admin/{} called", id);

        service.deleteUser(id);

        log.warn("User deleted with id={}", id);
    }
}