package com.abnamro.recipes_api.controller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.UUID;

/**
 * Validator class for validating a list of UUID strings.
 * Implements the {@link ConstraintValidator} interface to provide custom validation logic.
 */
public class UUIDListValidator implements ConstraintValidator<ValidUUIDList, List<String>> {

    /**
     * Initializes the validator. This method can be used for custom initialization logic if needed.
     * In this implementation, no initialization is required.
     *
     * @param constraintAnnotation the annotation instance for a given constraint declaration
     */
    @Override
    public void initialize(ValidUUIDList constraintAnnotation) {
        // initialization code if needed
    }

    /**
     * Validates a list of strings to ensure each string is a valid UUID.
     *
     * @param value   the list of strings to validate
     * @param context context in which the constraint is evaluated
     * @return {@code true} if all strings in the list are valid UUIDs, {@code false} otherwise
     */
    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null cases
        }
        return value.stream().allMatch(this::isValidUUIDString);
    }

    /**
     * Helper method to check if a given string is a valid UUID.
     *
     * @param uuidString the string to validate as a UUID
     * @return {@code true} if the string is a valid UUID, {@code false} otherwise
     */
    private boolean isValidUUIDString(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
