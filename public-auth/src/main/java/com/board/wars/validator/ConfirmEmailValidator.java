package com.board.wars.validator;

import com.board.wars.payload.UserPayload;
import com.board.wars.validator.annotations.ConfirmEmailMatcher;
import com.mailboxvalidator.MBVResult;
import com.mailboxvalidator.SingleValidation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.IOException;

public class ConfirmEmailValidator implements ConstraintValidator<ConfirmEmailMatcher, Object> {
    private String tokenKey;

    @Override
    public void initialize(final ConfirmEmailMatcher constraintAnnotation) {
        tokenKey = constraintAnnotation.tokenKey();
    }

    @Override
    public boolean isValid(final Object object, final ConstraintValidatorContext context) {
        String email = (String)object;
        SingleValidation mbv = new SingleValidation(tokenKey);
        try {
            MBVResult rec = mbv.ValidateEmail(email);
            if ("".equals(rec.getErrorMessage())) {
                return rec.getStatus().equals("True");
            }
        } catch (Exception e) {
            //log
            e.printStackTrace();
        }
        return true;
    }

}