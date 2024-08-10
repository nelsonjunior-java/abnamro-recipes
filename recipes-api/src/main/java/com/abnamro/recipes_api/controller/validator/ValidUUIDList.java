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

    /**
     * The default error message that is shown when the validation fails.
     *
     * @return the error message
     */
    String message() default "Invalid UUID format in list";

    /**
     * Allows the specification of validation groups, to which this constraint belongs.
     *
     * @return the groups the constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * Can be used by clients of the Jakarta Bean Validation API to assign custom payload objects to a constraint.
     *
     * @return the payload associated with the constraint
     */
    Class<? extends Payload>[] payload() default {};
}
