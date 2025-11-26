package vn.hcmute.edu.userservice.validation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import vn.hcmute.edu.userservice.validation.impl.UserStatusValidatorForEnum;
import vn.hcmute.edu.userservice.validation.impl.UserStatusValidatorForString;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {UserStatusValidatorForString.class, UserStatusValidatorForEnum.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserStatus {
    String message() default "Invalid status, must be one of: ACTIVE, PENDING, DEACTIVATED, DELETED";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}