package com.abnamro.recipes_api.controller.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating UUID fields.
 */
@Constraint(validatedBy = UUIDListValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUUIDList {
    String message() default "Invalid UUID format in list";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
