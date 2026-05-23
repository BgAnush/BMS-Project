package com.bank.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {

    private String fullName;

    private String mobileNumber;

    private String address;

    private String about;
}