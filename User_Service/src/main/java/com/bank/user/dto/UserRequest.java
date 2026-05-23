package com.bank.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotNull(message = "Profile ID is required")
    private Integer profileId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100)
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String emailId;

    @Pattern(
    	    regexp = "^[6-9]\\d{9}$",
    	    message = "Mobile must be 10 digits starting with 6-9"
    	)
    	private String mobileNumber;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Address is required")
    private String address;

    private String about;

    @NotNull(message = "Date of birth is required")
    @Past(message = "DOB must be in past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Account number is required")
    private Long accountNumber;

    private String image;
}