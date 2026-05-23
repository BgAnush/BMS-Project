package com.bank.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<Adult, LocalDate> {

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {

        if (dob == null) return false;

        int age = Period.between(dob, LocalDate.now()).getYears();

        return age >= 18;
    }
}