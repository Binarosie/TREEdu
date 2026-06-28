package vn.hcmute.edu.materialsservice.dtos.request.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để đặt lại mật khẩu với OTP xác thực
 * 
 * POST /api/auth/reset-password
 * Body: {
 * "email": "user@example.com",
 * "otp": "123456",
 * "newPassword": "newPassword123"
 * }
 * 
 * ⚠️ HTTPS ONLY - Mật khẩu được truyền qua request body an toàn, không phải URL
 * parameter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email là bắt buộc")
    private String email;

    @NotBlank(message = "OTP là bắt buộc")
    private String otp;

    @NotBlank(message = "Mật khẩu mới là bắt buộc")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String newPassword;
}
