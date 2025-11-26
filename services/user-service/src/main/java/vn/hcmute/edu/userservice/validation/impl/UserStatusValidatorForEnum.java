package vn.hcmute.edu.userservice.validation.impl;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.hcmute.edu.userservice.enums.EUserStatus;
import vn.hcmute.edu.userservice.validation.validator.ValidUserStatus;

public class UserStatusValidatorForEnum implements ConstraintValidator<ValidUserStatus, EUserStatus> {
    @Override
    public boolean isValid(EUserStatus value, ConstraintValidatorContext context) {
        if (value == null) return true;
        EUserStatus.valueOf(value.name());
        return true;
    }
}