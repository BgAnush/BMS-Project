package com.bank.auth.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import com.bank.auth.validation.Adult;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "Password must be at least 8 characters long, contain 1 uppercase letter, 1 number, and 1 special character"
    )
    private String password;
    @NotBlank(message = "Role is required")
    @Pattern(
        regexp = "^(ADMIN|MANAGER|USER)$",
        message = "Role must be ADMIN, MANAGER or USER"
    )
    private String role;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Pattern(
    	    regexp = "^[6-9]\\d{9}$",
    	    message = "Mobile number must be 10 digits and start with 6-9"
    	)
    	private String mobileNumber;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Gender is required")
    private String gender;

    private String about;

    @NotNull(message = "Date of birth is required")
    @Adult
    private LocalDate dateOfBirth;

    
}