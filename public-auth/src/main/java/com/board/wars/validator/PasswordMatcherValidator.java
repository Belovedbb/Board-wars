package com.board.wars.validator;

import com.board.wars.payload.UserPayload;
import com.board.wars.validator.annotations.PasswordMatcher;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatcherValidator implements ConstraintValidator<PasswordMatcher, Object> {

    @Override
    public void initialize(final PasswordMatcher constraintAnnotation) {
    }

    @Override
    public boolean isValid(final Object object, final ConstraintValidatorContext context) {
        final UserPayload user = (UserPayload) object;
        return StringUtils.hasText(user.getPassword()) && user.getPassword().equals(user.getConfirmPassword());
    }

}
