package com.bank.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import com.bank.auth.validation.Adult;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotNull(message = "Profile ID is required")
    private Integer profileId;

    @NotBlank(message = "Customer ID is required")
    @Pattern(
        regexp = "^(ADM|MNG|CUS)\\d{4,}$",
        message = "Customer ID must start with ADM, MNG, or CUS followed by numbers"
    )
    private String customerId;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50, message = "Full name must be between 3 and 50 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String emailId;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
        regexp = "^[6-9]\\d{9}$",
        message = "Mobile number must be 10 digits and start with 6-9"
    )
    private String mobileNumber;

    @NotBlank(message = "Gender is required")
    @Pattern(
        regexp = "Male|Female|Other",
        message = "Gender must be Male, Female, or Other"
    )
    private String gender;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    @Size(max = 1000, message = "About section too long")
    private String about;

    @NotNull(message = "Date of birth is required")
    @Adult
    private LocalDate dateOfBirth;
    private String image;
	
    
}