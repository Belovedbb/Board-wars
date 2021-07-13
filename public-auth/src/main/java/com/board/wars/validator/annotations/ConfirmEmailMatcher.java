package com.board.wars.validator.annotations;

import com.board.wars.validator.ConfirmEmailValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Email;
import java.lang.annotation.*;


@Email(message="Please provide a valid email address")
@Target( { ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConfirmEmailValidator.class)
@Documented
public @interface ConfirmEmailMatcher {
    String message() default "Please provide a valid email address";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String tokenKey();
}

