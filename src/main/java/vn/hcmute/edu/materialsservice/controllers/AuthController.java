package vn.hcmute.edu.materialsservice.controllers;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.UserInfoDTO;
import vn.hcmute.edu.materialsservice.dtos.request.users.LoginRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.ResetPasswordRequest;
import vn.hcmute.edu.materialsservice.dtos.request.users.VerifyOtpRequest;
import vn.hcmute.edu.materialsservice.dtos.response.BadRequestError;
import vn.hcmute.edu.materialsservice.dtos.response.InternalServerError;
import vn.hcmute.edu.materialsservice.dtos.response.SuccessResponse;
import vn.hcmute.edu.materialsservice.dtos.response.UnauthorizedError;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.services.EmailService;
import vn.hcmute.edu.materialsservice.services.impl.UserServiceImpl;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.security.JwtTokenUtil;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ─── Email Verification (OTP) ────────────────────────────────────────────────
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Verifying OTP for email: {}", request.getEmail());

        // Một lời gọi duy nhất — kiểm tra hết hạn + replay + xoá luôn sau khi dùng
        EmailService.OtpResult result = emailService.verifyAndConsumeOtp(request.getEmail(), request.getOtp());

        switch (result) {
            case EXPIRED:
                log.warn("OTP expired for email: {}", request.getEmail());
                return ResponseEntity.badRequest().body(
                        new BadRequestError("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới."));
            case WRONG_CODE:
                log.warn("OTP wrong code for email: {}", request.getEmail());
                return ResponseEntity.badRequest().body(
                        new BadRequestError("Mã OTP không chính xác."));
            case NOT_FOUND:
            case ALREADY_USED:
                log.warn("OTP invalid or already used for email: {}", request.getEmail());
                return ResponseEntity.badRequest().body(
                        new BadRequestError("Mã OTP không hợp lệ hoặc đã được sử dụng."));
            default:
                break; // SUCCESS — tiếp tục
        }

        // Tìm user
        var optUser = userServiceImpl.findByEmail(request.getEmail());
        if (optUser.isEmpty()) {
            log.error("User not found for email: {}", request.getEmail());
            return ResponseEntity.badRequest().body(
                    new BadRequestError("Không tìm thấy tài khoản."));
        }
        var user = optUser.get();

        // Đã active rồi
        if (user.isActive()) {
            log.info("User already active for email: {}", request.getEmail());
            return ResponseEntity.ok(new SuccessResponse(
                    "Tài khoản đã được kích hoạt trước đó.",
                    HttpStatus.OK.value(), null, LocalDateTime.now()));
        }

        // Xóa các bản ghi inactive trùng email rồi active user
        userRepository.deleteByEmailAndIsActive(request.getEmail(), false);
        user.setActive(true);
        userRepository.save(user);

        log.info("Email verified successfully for: {}", request.getEmail());

        return ResponseEntity.ok(new SuccessResponse(
                "Xác thực email thành công! Bạn có thể đăng nhập ngay bây giờ.",
                HttpStatus.OK.value(), null, LocalDateTime.now()));
    }

    // ─── Forgot / Reset Password (OTP) ───────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        log.info("Forgot password request for email: {}", email);

        var optUser = userServiceImpl.findByEmail(email);
        if (optUser.isEmpty()) {
            // Trả về 200 dù không tìm thấy email để tránh email enumeration attack
            log.warn("Email not found (returning generic response): {}", email);
            return ResponseEntity.ok(new SuccessResponse(
                    "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi.",
                    HttpStatus.OK.value(), null, LocalDateTime.now()));
        }


        String otp = emailService.generateOtp();
        emailService.sendResetPasswordEmail(email, otp);
        log.info("Reset password OTP sent to: {}", email);


        return ResponseEntity.ok(new SuccessResponse(
                "Mã OTP đặt lại mật khẩu đã được gửi về email của bạn. Mã có hiệu lực trong 5 phút.",
                HttpStatus.OK.value(), null, LocalDateTime.now()));
    }

    // ─── Resend OTP (for signup or reset password)
    // ────────────────────────────────
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(
            @RequestParam("email") String email,
            @RequestParam("type") String type) {
        log.info("Resend OTP request for email: {} with type: {}", email, type);

        // Validate type
        if (!type.equals("SIGNUP") && !type.equals("RESET_PASSWORD")) {
            log.warn("Invalid OTP type: {}", type);
            return ResponseEntity.badRequest().body(
                    new BadRequestError("Loại OTP không hợp lệ. Vui lòng sử dụng SIGNUP hoặc RESET_PASSWORD."));
        }


            // 1. Kiểm tra email có tồn tại không (tùy theo type)
            var optUser = userServiceImpl.findByEmail(email);

            if (type.equals("SIGNUP")) {
                // Cho phép resend nếu account chưa active
                if (optUser.isEmpty()) {
                    log.warn("Email not found for signup resend: {}", email);
                    return ResponseEntity.badRequest().body(
                            new BadRequestError("Email này chưa được đăng ký trong hệ thống."));
                }
                var user = optUser.get();
                if (user.isActive()) {
                    log.info("User already active (no need resend): {}", email);
                    return ResponseEntity.badRequest().body(
                            new BadRequestError("Tài khoản này đã được kích hoạt rồi. Hãy đăng nhập."));
                }
            } else if (type.equals("RESET_PASSWORD")) {
                // Cho phép resend nếu account tồn tại
                if (optUser.isEmpty()) {
                    log.warn("Email not found for reset password resend: {}", email);
                    // Trả về generic message để tránh email enumeration attack
                    return ResponseEntity.ok(new SuccessResponse(
                            "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi.",
                            HttpStatus.OK.value(), null, LocalDateTime.now()));
                }
            }

            // 2. Tạo OTP mới (dùng EmailService.generateOtp())
            String otp = emailService.generateOtp();

            // 3. Gửi email theo type
            if (type.equals("SIGNUP")) {
                emailService.sendVerificationEmail(email, otp);
                log.info("Signup verification OTP resent to: {}", email);
                return ResponseEntity.ok(new SuccessResponse(
                        "Mã OTP xác thực email đã được gửi lại. Mã có hiệu lực trong 5 phút.",
                        HttpStatus.OK.value(), null, LocalDateTime.now()));
            } else {
                emailService.sendResetPasswordEmail(email, otp);
                log.info("Reset password OTP resent to: {}", email);
                return ResponseEntity.ok(new SuccessResponse(
                        "Mã OTP đặt lại mật khẩu đã được gửi lại. Mã có hiệu lực trong 5 phút.",
                        HttpStatus.OK.value(), null, LocalDateTime.now()));
            }


    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String email = request.getEmail();
        String newPassword = request.getNewPassword();

        log.info("Reset password request for email: {}", email);

        // Xác thực OTP
        EmailService.OtpResult result = emailService.verifyAndConsumeOtp(email, request.getOtp());

        switch (result) {
            case EXPIRED:
                log.warn("OTP expired for password reset: {}", email);
                return ResponseEntity.badRequest().body(
                        new BadRequestError("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới."));
            case WRONG_CODE:
                log.warn("OTP wrong code for password reset: {}", email);
                return ResponseEntity.badRequest().body(
                        new BadRequestError("Mã OTP không chính xác."));
            case NOT_FOUND:
            case ALREADY_USED:
                log.warn("OTP invalid or already used for password reset: {}", email);
                return ResponseEntity.badRequest().body(
                        new BadRequestError("Mã OTP không hợp lệ hoặc đã được sử dụng."));
            default:
                break; // SUCCESS
        }

        // Tìm user
        var optUser = userServiceImpl.findByEmail(email);
        if (optUser.isEmpty()) {
            log.error("User not found for password reset: {}", email);
            return ResponseEntity.badRequest().body(
                    new BadRequestError("Không tìm thấy tài khoản."));
        }
        var user = optUser.get();

        // Cập nhật mật khẩu
        userRepository.deleteByEmailAndIdNot(email, user.getId());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password reset successfully for: {}", email);

        return ResponseEntity.ok(new SuccessResponse(
                "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.",
                HttpStatus.OK.value(), null, LocalDateTime.now()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtTokenUtil.generateToken(userDetails);

        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setHttpOnly(true); // Chỉ JavaScript engine có thể truy cập
        jwtCookie.setSecure(true); // Production: bắt buộc HTTPS
        jwtCookie.setPath("/"); // Áp dụng cho toàn bộ ứng dụng
        jwtCookie.setMaxAge(3600); // 1 hour
        response.addCookie(jwtCookie);

        log.info("User logged in: {}", userDetails.getUsername());

        return ResponseEntity.ok(new SuccessResponse(
                "Login successful", HttpStatus.OK.value(), token, LocalDateTime.now()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("JWT", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        log.info("User logged out");

        return ResponseEntity.ok(new SuccessResponse(
                "Logout successful", HttpStatus.OK.value(), null, LocalDateTime.now()));
    }

    // ─── Change Password (authenticated) ─────────────────────────────────────────
    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_ADMIN', 'ROLE_SUPPORTER')")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("Chưa xác thực người dùng!");
        }

        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        log.info("Password change request for user: {}", user.getEmail());

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Old password incorrect for user: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new UnauthorizedError("Mật khẩu cũ không chính xác!"));
        }

        userRepository.deleteByEmailAndIdNot(user.getEmail(), user.getId());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getEmail());

        return ResponseEntity.ok(new SuccessResponse(
                "Đổi mật khẩu thành công!", HttpStatus.OK.value(), null, LocalDateTime.now()));
    }

    @PostMapping("/current-user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        CustomUserDetails currentUserDetails = (CustomUserDetails) authentication.getPrincipal();
        var user = currentUserDetails.getUser();

        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng!");
        }

        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId().toString());
        dto.setEmail(user.getEmail());
        dto.setName(user.getFullName());
        dto.setRole(currentUserDetails.getAuthorities().iterator().next().getAuthority());

        log.info("Retrieved current user info: {}", user.getEmail());
        // Log để debug
        log.info("Authorities: {}", authentication.getAuthorities());

        return ResponseEntity.ok(new SuccessResponse(
                "Lấy thông tin người dùng thành công!",
                HttpStatus.OK.value(), dto, LocalDateTime.now()));
    }
}
