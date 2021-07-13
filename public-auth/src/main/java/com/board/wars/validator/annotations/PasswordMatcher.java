package com.board.wars.validator.annotations;

import com.board.wars.validator.PasswordMatcherValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatcherValidator.class)
public @interface PasswordMatcher {
    String message() default "password does not match with replicate";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
