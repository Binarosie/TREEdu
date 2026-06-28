package vn.hcmute.edu.materialsservice.dtos.request.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để xác thực OTP (Email Verification hoặc Password Reset)
 * 
 * POST /api/auth/verify-otp
 * Body: {
 * "email": "user@example.com",
 * "otp": "123456"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email là bắt buộc")
    private String email;

    @NotBlank(message = "OTP là bắt buộc")
    private String otp;
}
