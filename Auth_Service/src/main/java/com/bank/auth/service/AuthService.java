package com.bank.auth.service;

import com.bank.auth.client.NotificationFeignClient;
import com.bank.auth.dto.LoginRequest;
import com.bank.auth.dto.RegisterRequest;
import com.bank.auth.dto.RegistrationMailRequest;
import com.bank.auth.dto.UserRegisterRequest;
import com.bank.auth.dto.UserRequest;
import com.bank.auth.entity.AuthUser;
import com.bank.auth.entity.Role;
import com.bank.auth.exception.AuthenticationFailedException;
import com.bank.auth.exception.ExternalServiceException;
import com.bank.auth.exception.UserAlreadyExistsException;
import com.bank.auth.exception.UserNotFoundException;
import com.bank.auth.repository.AuthRepository;
import com.bank.auth.security.JwtService;
import com.bank.auth.utility.CustomerIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository repo;
    private final PasswordEncoder encoder;
    private final AsyncService asyncService;
    private final JwtService jwtService;

    // =========================================================
    // NOTIFICATION SERVICE
    // =========================================================
    private final NotificationFeignClient notificationFeignClient;

    // =========================================================
    // INTERNAL DTO TO REDUCE PARAMETER COUNT
    // =========================================================
    private static class UserCreationData {

        private String email;
        private String password;
        private Role role;
        private String fullName;
        private String mobile;
        private String gender;
        private String address;
        private String about;
        private java.time.LocalDate dob;
        private String imagePath;
    }

    // =========================================================
    // ADMIN REGISTER
    // =========================================================
    @Transactional
    public String register(RegisterRequest req, String path) {

        log.info("Admin Register request for email={}", req.getEmail());

        validateEmail(req.getEmail());

        Role role = Role.valueOf(req.getRole().toUpperCase());

        if (role != Role.ADMIN && role != Role.MANAGER) {

            log.error("Invalid role attempt: {}", req.getRole());

            throw new IllegalArgumentException(
                    "Admin can only create ADMIN or MANAGER"
            );
        }

        UserCreationData data = buildAdminCreationData(req, role, path);

        return createUser(data);
    }

    // =========================================================
    // USER REGISTER
    // =========================================================
    @Transactional
    public String userRegister(UserRegisterRequest req, String path) {

        log.info("User Register request for email={}", req.getEmail());

        validateEmail(req.getEmail());

        UserCreationData data = buildUserCreationData(req, path);

        return createUser(data);
    }

    // =========================================================
    // COMMON USER CREATION
    // =========================================================
    private String createUser(UserCreationData data) {

        AuthUser user = new AuthUser();

        user.setEmail(data.email);
        user.setPassword(encoder.encode(data.password));
        user.setRole(data.role);
        user.setCustomerId(
                CustomerIdGenerator.generate(data.role.name())
        );

        AuthUser saved = repo.save(user);

        log.info(
                "User saved with CustomerId={}",
                saved.getCustomerId()
        );

        UserRequest ur = new UserRequest();

        ur.setProfileId(saved.getProfileId());
        ur.setCustomerId(saved.getCustomerId());
        ur.setFullName(data.fullName);
        ur.setEmailId(data.email);
        ur.setMobileNumber(data.mobile);
        ur.setGender(data.gender);
        ur.setAddress(data.address);
        ur.setAbout(data.about);
        ur.setDateOfBirth(data.dob);
        ur.setImage(data.imagePath);

        asyncService.sendToUserService(ur);

        log.info(
                "User data sent to UserService for customerId={}",
                saved.getCustomerId()
        );

        // =========================================================
        // SEND REGISTRATION MAIL
        // =========================================================
        try {

            RegistrationMailRequest mailRequest =
                    new RegistrationMailRequest();

            mailRequest.setEmail(data.email);

            mailRequest.setFullName(data.fullName);

            mailRequest.setCustomerId(
                    saved.getCustomerId()
            );

            mailRequest.setRole(
                    saved.getRole().name()
            );

            notificationFeignClient
                    .sendRegistrationMail(mailRequest);

            log.info(
                    "Registration mail request sent successfully for customerId={}",
                    saved.getCustomerId()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to call Notification Service for customerId={}",
                    saved.getCustomerId(),
                    e
            );
        }

        return "REGISTERED: " + saved.getCustomerId();
    }

    // =========================================================
    // LOGIN
    // =========================================================
    public String login(LoginRequest req) {

        log.info(
                "Login attempt for customerId={}",
                req.getCustomerId()
        );

        AuthUser user = repo.findByCustomerId(req.getCustomerId())
                .orElseThrow(() -> {

                    log.error(
                            "User not found: {}",
                            req.getCustomerId()
                    );

                    return new UserNotFoundException("User not found");
                });

        if (!encoder.matches(
                req.getPassword(),
                user.getPassword()
        )) {

            log.warn(
                    "Invalid password for customerId={}",
                    req.getCustomerId()
            );

            throw new AuthenticationFailedException(
                    "Invalid password"
            );
        }

        String token = jwtService.generateToken(
                user.getProfileId(),
                user.getCustomerId(),
                user.getRole().name()
        );

        log.info(
                "Login successful for customerId={}",
                req.getCustomerId()
        );

        return token;
    }

    // =========================================================
    // DELETE USER
    // =========================================================
    public void deleteUser(Integer profileId) {

        log.warn(
                "Delete request for profileId={}",
                profileId
        );

        AuthUser user = repo.findById(profileId)
                .orElseThrow(() -> {

                    log.error(
                            "User not found: {}",
                            profileId
                    );

                    return new UserNotFoundException(
                            "User not found"
                    );
                });

        repo.delete(user);

        log.warn(
                "User deleted for profileId={}",
                profileId
        );
    }

    // =========================================================
    // IMAGE UPLOAD
    // =========================================================
    public String saveImage(MultipartFile file) {

        try {

            log.info(
                    "Uploading image: {}",
                    file.getOriginalFilename()
            );

            String uploadDir =
                    "C:/Capgemini Project/Auth_Service/uploads/";

            File dir = new File(uploadDir);

            if (!dir.exists()) {

                dir.mkdirs();

                log.info("Upload directory created");
            }

            String cleanName = file.getOriginalFilename()
                    .replaceAll(
                            "[^a-zA-Z0-9\\.\\-]",
                            "_"
                    );

            String fileName =
                    System.currentTimeMillis()
                            + "_"
                            + cleanName;

            Path filePath = Paths.get(uploadDir, fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            log.info(
                    "Image uploaded successfully: {}",
                    fileName
            );

            return fileName;

        } catch (Exception e) {

            log.error("Image upload failed", e);

            throw new ExternalServiceException(
                    "Image upload failed"
            );
        }
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private void validateEmail(String email) {

        if (repo.findByEmail(email).isPresent()) {

            log.warn("Email already exists: {}", email);

            throw new UserAlreadyExistsException(
                    "Email already exists"
            );
        }
    }

    private UserCreationData buildAdminCreationData(
            RegisterRequest req,
            Role role,
            String path
    ) {

        UserCreationData data = new UserCreationData();

        data.email = req.getEmail();
        data.password = req.getPassword();
        data.role = role;
        data.fullName = req.getFullName();
        data.mobile = req.getMobileNumber();
        data.gender = req.getGender();
        data.address = req.getAddress();
        data.about = req.getAbout();
        data.dob = req.getDateOfBirth();
        data.imagePath = path;

        return data;
    }

    private UserCreationData buildUserCreationData(
            UserRegisterRequest req,
            String path
    ) {

        UserCreationData data = new UserCreationData();

        data.email = req.getEmail();
        data.password = req.getPassword();
        data.role = Role.USER;
        data.fullName = req.getFullName();
        data.mobile = req.getMobileNumber();
        data.gender = req.getGender();
        data.address = req.getAddress();
        data.about = req.getAbout();
        data.dob = req.getDateOfBirth();
        data.imagePath = path;

        return data;
    }
}