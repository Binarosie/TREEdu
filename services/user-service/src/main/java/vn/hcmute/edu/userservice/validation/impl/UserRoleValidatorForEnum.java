package vn.hcmute.edu.userservice.validation.impl;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import vn.hcmute.edu.userservice.enums.EUserRole;
import vn.hcmute.edu.userservice.validation.validator.ValidUserRole;

public class UserRoleValidatorForEnum implements ConstraintValidator<ValidUserRole, EUserRole> {
    @Override
    public boolean isValid(EUserRole value, ConstraintValidatorContext context) {
        if (value == null) return true;
        EUserRole.valueOf(value.name());
        return true;
    }
}