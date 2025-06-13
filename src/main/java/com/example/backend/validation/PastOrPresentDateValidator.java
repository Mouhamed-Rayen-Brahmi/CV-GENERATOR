package com.example.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class PastOrPresentDateValidator implements ConstraintValidator<PastOrPresentDate, LocalDate> {
    
    @Override
    public void initialize(PastOrPresentDate constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // null values are handled by @NotNull if required
        }
        return !date.isAfter(LocalDate.now());
    }
}