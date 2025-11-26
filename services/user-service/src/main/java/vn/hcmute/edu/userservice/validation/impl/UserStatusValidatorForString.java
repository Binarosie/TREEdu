package vn.hcmute.edu.userservice.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.hcmute.edu.userservice.enums.EUserStatus;
import vn.hcmute.edu.userservice.validation.validator.ValidUserStatus;

public class UserStatusValidatorForString implements ConstraintValidator<ValidUserStatus, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return EUserStatus.fromJsonSafe(value).isPresent();
    }
}