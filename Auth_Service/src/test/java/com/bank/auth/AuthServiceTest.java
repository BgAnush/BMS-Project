package com.bank.auth;

import com.bank.auth.client.UserClient;
import com.bank.auth.dto.LoginRequest;
import com.bank.auth.dto.RegisterRequest;
import com.bank.auth.entity.AuthUser;
import com.bank.auth.entity.Role;
import com.bank.auth.exception.AuthenticationFailedException;
import com.bank.auth.exception.UserAlreadyExistsException;
import com.bank.auth.repository.AuthRepository;
import com.bank.auth.security.JwtService;
import com.bank.auth.service.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthRepository repo;
    @Mock PasswordEncoder encoder;
    @Mock UserClient userClient;
    @Mock JwtService jwtService;

    @InjectMocks AuthService service;

    private RegisterRequest buildRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@bank.com");
        req.setPassword("Pass@123");
        req.setRole("USER");
        req.setFullName("Test User");
        req.setMobileNumber("9876543210");
        req.setGender("Male");
        req.setAddress("Bangalore");
        req.setDateOfBirth(LocalDate.of(2000,1,1));
        return req;
    }

    // =========================
    // REGISTER SUCCESS
    // =========================
   

    // =========================
    // DUPLICATE EMAIL
    // =========================
    @Test
    void testRegister_duplicateEmail() {

        RegisterRequest req = buildRequest();

        when(repo.findByEmail(anyString()))
                .thenReturn(Optional.of(new AuthUser()));

        assertThrows(UserAlreadyExistsException.class,
                () -> service.register(req, null));
    }

    // =========================
    // LOGIN SUCCESS
    // =========================
    @Test
    void testLogin_success() {

        LoginRequest req = new LoginRequest();
        req.setCustomerId("CUS001");
        req.setPassword("pass");

        AuthUser user = new AuthUser();
        user.setProfileId(1); // ✅ IMPORTANT FIX
        user.setCustomerId("CUS001");
        user.setPassword("hash");
        user.setRole(Role.USER);

        when(repo.findByCustomerId("CUS001"))
                .thenReturn(Optional.of(user));

        when(encoder.matches(anyString(), anyString()))
                .thenReturn(true);

        when(jwtService.generateToken(anyInt(), anyString(), anyString()))
                .thenReturn("jwt");

        String token = service.login(req);

        assertEquals("jwt", token);
    }

    // =========================
    // LOGIN FAIL PASSWORD
    // =========================
    @Test
    void testLogin_invalidPassword() {

        LoginRequest req = new LoginRequest();
        req.setCustomerId("CUS001");
        req.setPassword("wrong");

        AuthUser user = new AuthUser();
        user.setPassword("hash");

        when(repo.findByCustomerId(anyString()))
                .thenReturn(Optional.of(user));

        when(encoder.matches(anyString(), anyString()))
                .thenReturn(false);

        assertThrows(AuthenticationFailedException.class,
                () -> service.login(req));
    }
    

        // ---------------- SUCCESS CASE ----------------
        @Test
        void testSaveImageSuccess() {

            MockMultipartFile file =
                    new MockMultipartFile(
                            "image",
                            "test.jpg",
                            "image/jpeg",
                            "dummy-image-content".getBytes()
                    );

            String result = service.saveImage(file);

            assertNotNull(result);
            assertTrue(result.endsWith(".jpg"));
            assertTrue(result.length() > 10);
        }

        // ---------------- NULL FILE NAME EDGE CASE ----------------
        @Test
        void testSaveImageNullOriginalFilename() {

            MockMultipartFile file =
                    new MockMultipartFile(
                            "image",
                            null,
                            "image/jpeg",
                            "dummy-image-content".getBytes()
                    );

            String result = service.saveImage(file);

            assertNotNull(result);
            assertTrue(result.contains("_"));
        }

       
    }
    
