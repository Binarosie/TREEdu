package vn.hcmute.edu.userservice.validation.impl;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.hcmute.edu.userservice.enums.EUserRole;
import vn.hcmute.edu.userservice.validation.validator.ValidUserRole;

public class UserRoleValidatorForString implements ConstraintValidator<ValidUserRole, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return EUserRole.fromJsonSafe(value).isPresent();
    }
}