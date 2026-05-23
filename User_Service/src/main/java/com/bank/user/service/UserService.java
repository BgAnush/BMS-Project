package com.bank.user.service;

import com.bank.user.client.AuthClient;
import com.bank.user.dto.UpdateUserRequest;
import com.bank.user.dto.UserRequest;
import com.bank.user.entity.BankUser;
import com.bank.user.exception.*;
import com.bank.user.repository.UserRepository;
import com.bank.user.util.AccountNumberGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository repo;
    private final AuthClient authClient;

    private static final int MIN_AGE = 18;

    // =========================================================
    // CREATE USER
    // =========================================================
    public BankUser createUser(UserRequest req) {

        log.info("Creating new user with email={}", req != null ? req.getEmailId() : null);

        Objects.requireNonNull(req, "Request cannot be null");

        if (repo.existsByEmailId(req.getEmailId())) {
            log.error("User creation failed: Email already exists -> {}", req.getEmailId());
            throw new DuplicateResourceException("Email already exists");
        }

        if (repo.existsByMobileNumber(req.getMobileNumber())) {
            log.error("User creation failed: Mobile already exists -> {}", req.getMobileNumber());
            throw new DuplicateResourceException("Mobile already exists");
        }

        if (isUnderAge(req.getDateOfBirth())) {
            log.error("User creation failed: Age validation failed for DOB={}", req.getDateOfBirth());
            throw new AgeValidationException();
        }

        BankUser user = new BankUser();

        user.setProfileId(req.getProfileId());
        user.setCustomerId(req.getCustomerId());
        user.setFullName(req.getFullName());
        user.setEmailId(req.getEmailId());
        user.setMobileNumber(req.getMobileNumber());
        user.setGender(req.getGender());
        user.setAddress(req.getAddress());
        user.setAbout(req.getAbout());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setImage(req.getImage());

        Long accountNumber = generateAccountNumber();
        user.setAccountNumber(accountNumber);

        BankUser savedUser = repo.save(user);

        log.info("User created successfully with id={} and accountNumber={}",
                savedUser.getProfileId(), accountNumber);

        return savedUser;
    }

    // =========================================================
    // GET USER
    // =========================================================
    public BankUser getUser(Integer id) {

        log.debug("Fetching user with id={}", id);

        BankUser user = repo.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id={}", id);
                    return new UserNotFoundException(id);
                });

        log.debug("User fetched successfully with id={}", id);

        return user;
    }

    // =========================================================
    // UPDATE USER
    // =========================================================
    public BankUser updateUser(Integer id, UpdateUserRequest req) {

        log.info("Updating user with id={}", id);

        if (req == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        BankUser user = getUser(id);

        // Update Full Name
        if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
            user.setFullName(req.getFullName().trim());
        }

        // Update Address
        if (req.getAddress() != null && !req.getAddress().trim().isEmpty()) {
            user.setAddress(req.getAddress().trim());
        }

        // Update About
        if (req.getAbout() != null && !req.getAbout().trim().isEmpty()) {
            user.setAbout(req.getAbout().trim());
        }

        // Update Mobile Number
        if (req.getMobileNumber() != null &&
                !req.getMobileNumber().trim().isEmpty()) {

            if (repo.existsByMobileNumberAndProfileIdNot(
                    req.getMobileNumber(), id)) {

                throw new DuplicateResourceException(
                        "Mobile already in use"
                );
            }

            user.setMobileNumber(req.getMobileNumber().trim());
        }

        BankUser updatedUser = repo.save(user);

        log.info("User updated successfully with id={}", id);

        return updatedUser;
    }

    // =========================================================
    // DELETE USER + AUTH SYNC
    // =========================================================
    public void deleteUser(Integer id) {

        log.warn("Deleting user with id={}", id);

        BankUser user = getUser(id);

        repo.delete(user);

        log.warn("User deleted from UserService DB with id={}", id);

        // sync with auth service
        try {
            authClient.deleteAuthUser(id);
            log.info("Auth service synced: deleted user with id={}", id);
        } catch (Exception ex) {
            log.error("Auth sync failed for user id={}", id, ex);
            throw new AuthenticationServiceException("Failed to sync with Auth Service");
        }
    }

    // =========================================================
    // ALL USERS
    // =========================================================
    public List<BankUser> getAllUsers() {

        log.info("Fetching all users");

        List<BankUser> users = repo.findAll();

        log.info("Total users fetched={}", users.size());

        return users;
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private boolean isUnderAge(LocalDate dob) {

        boolean result = dob.isAfter(LocalDate.now().minusYears(MIN_AGE));

        if (result) {
            log.debug("Age validation failed for DOB={}", dob);
        }

        return result;
    }

    private Long generateAccountNumber() {

        Long acc;
        int attempts = 0;

        do {
            acc = AccountNumberGenerator.generate();
            log.debug("Generated account number attempt {} -> {}", attempts + 1, acc);

            if (++attempts > 5) {
                log.error("Failed to generate unique account number after 5 attempts");
                throw new AccountGenerationException(
                        "Failed to generate unique account number after 5 attempts"
                );
            }

        } while (repo.existsByAccountNumber(acc));

        log.info("Unique account number generated successfully -> {}", acc);

        return acc;
    }
}