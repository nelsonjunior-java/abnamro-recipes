package com.abnamro.recipes_api.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.UUID;

public class UUIDListValidator implements ConstraintValidator<ValidUUIDList, List<String>> {

    @Override
    public void initialize(ValidUUIDList constraintAnnotation) {
        // initialization code if needed
    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null cases
        }
        return value.stream().allMatch(this::isValidUUIDString);
    }

    private boolean isValidUUIDString(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
