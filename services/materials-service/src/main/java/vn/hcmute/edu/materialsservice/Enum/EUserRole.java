package vn.hcmute.edu.materialsservice.Enum;

import vn.hcmute.edu.materialsservice.models.Admin;
import vn.hcmute.edu.materialsservice.models.Member;
import vn.hcmute.edu.materialsservice.models.Supporter;
import vn.hcmute.edu.materialsservice.models.User;

public enum EUserRole {
    ADMIN,
    MEMBER,
    SUPPORTER;

    /**
     * Lấy role từ User instance (dựa vào class type)
     */
    public static EUserRole fromUser(User user) {
        if (user instanceof Admin) {
            return ADMIN;
        } else if (user instanceof Supporter) {
            return SUPPORTER;
        } else if (user instanceof Member) {
            return MEMBER;
        }
        throw new IllegalArgumentException("Unknown user type: " + user.getClass().getSimpleName());
    }

    /**
     * Convert role string to enum
     */
    public static EUserRole fromString(String role) {
        try {
            return EUserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Convert to Spring Security role format
     */
    public String toSpringRole() {
        return "ROLE_" + this.name();
    }
}