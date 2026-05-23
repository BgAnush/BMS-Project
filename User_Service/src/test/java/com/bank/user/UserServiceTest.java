package com.bank.user;

import com.bank.user.dto.UpdateUserRequest;
import com.bank.user.dto.UserRequest;
import com.bank.user.entity.BankUser;
import com.bank.user.exception.*;
import com.bank.user.repository.UserRepository;
import com.bank.user.service.UserService;
import com.bank.user.client.AuthClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository repo;
    private AuthClient authClient;
    private UserService service;

    @BeforeEach
    void setup() {
        repo = mock(UserRepository.class);
        authClient = mock(AuthClient.class);
        service = new UserService(repo, authClient);
    }

    // ========================= CREATE SUCCESS =========================
    @Test
    void createUser_success() {

        UserRequest req = validUser();

        when(repo.existsByEmailId(any())).thenReturn(false);
        when(repo.existsByMobileNumber(any())).thenReturn(false);
        when(repo.existsByAccountNumber(any())).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        BankUser user = service.createUser(req);

        assertEquals("Anush", user.getFullName());
        verify(repo).save(any());
    }

    // ========================= CREATE FAIL - EMAIL =========================
    @Test
    void createUser_emailExists() {

        UserRequest req = validUser();
        when(repo.existsByEmailId(any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.createUser(req));
    }

    // ========================= CREATE FAIL - AGE =========================
    @Test
    void createUser_underAge() {

        UserRequest req = validUser();
        req.setDateOfBirth(LocalDate.now());

        when(repo.existsByEmailId(any())).thenReturn(false);
        when(repo.existsByMobileNumber(any())).thenReturn(false);

        assertThrows(AgeValidationException.class,
                () -> service.createUser(req));
    }

    // ========================= GET USER =========================
    @Test
    void getUser_success() {

        BankUser user = new BankUser();
        user.setProfileId(1);

        when(repo.findById(1)).thenReturn(Optional.of(user));

        assertEquals(1, service.getUser(1).getProfileId());
    }

    // ========================= GET FAIL =========================
    @Test
    void getUser_notFound() {

        when(repo.findById(1)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.getUser(1));
    }

    // ========================= UPDATE SUCCESS =========================
    @Test
    void update_success() {

        BankUser user = new BankUser();
        user.setProfileId(1);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFullName("Updated");

        when(repo.findById(1)).thenReturn(Optional.of(user));
        when(repo.save(any())).thenReturn(user);

        BankUser result = service.updateUser(1, req);

        assertEquals("Updated", result.getFullName());
    }

    // ========================= UPDATE MOBILE DUPLICATE =========================
    @Test
    void update_mobileDuplicate() {

        BankUser user = new BankUser();
        user.setProfileId(1);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setMobileNumber("999");

        when(repo.findById(1)).thenReturn(Optional.of(user));
        when(repo.existsByMobileNumberAndProfileIdNot("999", 1)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.updateUser(1, req));
    }

    // ========================= DELETE =========================
    @Test
    void delete_success() {

        BankUser user = new BankUser();
        user.setProfileId(1);

        when(repo.findById(1)).thenReturn(Optional.of(user));

        service.deleteUser(1);

        verify(repo).delete(user);
        verify(authClient).deleteAuthUser(1);
    }

    // ========================= HELPER =========================
    private UserRequest validUser() {
        UserRequest req = new UserRequest();
        req.setProfileId(1);
        req.setCustomerId("CUS1");
        req.setFullName("Anush");
        req.setEmailId("a@gmail.com");
        req.setMobileNumber("9876543210");
        req.setGender("M");
        req.setAddress("BLR");
        req.setDateOfBirth(LocalDate.of(2000, 1, 1));
        return req;
    }
}