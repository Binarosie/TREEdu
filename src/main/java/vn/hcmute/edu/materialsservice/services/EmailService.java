package vn.hcmute.edu.materialsservice.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.*;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final ConcurrentHashMap<String, OtpEntry> emailOtpMap = new ConcurrentHashMap<>();

    // Dùng SecureRandom thay Math.random() — không thể đoán trước
    private final SecureRandom secureRandom = new SecureRandom();

    private static final long OTP_EXPIRY_MINUTES = 5;

    // Scheduler tự dọn entry hết hạn mỗi 10 phút (tránh memory leak)
    private final ScheduledExecutorService cleaner =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "OTP-Cleaner");
                t.setDaemon(true);
                return t;
            });

    @PostConstruct
    public void startCleaner() {
        cleaner.scheduleAtFixedRate(this::evictExpiredEntries, 10, 10, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void stopCleaner() {
        cleaner.shutdownNow();
    }

    // ─── Inner class ──────────────────────────────────────────────────────────

    private static class OtpEntry {
        final String code;
        final LocalDateTime expiresAt;
        volatile boolean used = false;   // ← flag chống replay attack

        OtpEntry(String code) {
            this.code = code;
            this.expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }

        /**
         * Đánh dấu đã dùng — trả về false nếu entry đã bị đánh dấu trước đó
         * (atomic check-and-set để tránh race condition)
         */
        synchronized boolean markUsed() {
            if (used) return false;  // đã dùng rồi → từ chối
            used = true;
            return true;
        }
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /** Tạo OTP 6 chữ số bằng SecureRandom */
    public String generateOtp() {
        int code = 100_000 + secureRandom.nextInt(900_000);
        String otp = String.valueOf(code);
        System.out.println("[OTP DEBUG] Generated new OTP: " + otp);
        return otp;
    }

    /** Gửi email xác thực tài khoản */
    public void sendVerificationEmail(String to, String otp) throws MessagingException {
        System.out.println("[OTP DEBUG] Sending verification email to: " + to + " with OTP: " + otp);
        String html = buildEmailHtml(
                "Xác thực tài khoản TREEdu",
                "Mã xác thực của bạn là:",
                otp,
                "#1D9E75",
                "Nếu bạn không đăng ký tài khoản, hãy bỏ qua email này."
        );
        sendEmail(to, "Mã xác thực tài khoản - TREEdu", html);
        storeOtp(to, otp);
    }

    /** Gửi email đặt lại mật khẩu — KHÔNG chứa mật khẩu mới */
    public void sendResetPasswordEmail(String to, String otp) throws MessagingException {
        System.out.println("[OTP DEBUG] Sending reset password email to: " + to + " with OTP: " + otp);
        String html = buildEmailHtml(
                "Đặt lại mật khẩu TREEdu",
                "Mã đặt lại mật khẩu của bạn là:",
                otp,
                "#D85A30",
                "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này."
        );
        sendEmail(to, "Mã đặt lại mật khẩu - TREEdu", html);
        storeOtp(to, otp);
    }

    /**
     * Xác thực OTP — kết hợp kiểm tra hết hạn + đã dùng + xoá luôn sau khi dùng.
     *
     * @return OtpResult chứa trạng thái cụ thể để controller trả lỗi rõ ràng
     */
    public OtpResult verifyAndConsumeOtp(String email, String inputOtp) {
        OtpEntry entry = emailOtpMap.get(email);

        // 1. Không tồn tại
        if (entry == null) {
            System.out.println("[OTP DEBUG] NOT_FOUND - Email: " + email + ", Input OTP: " + inputOtp + 
                    ", Entries in map: " + emailOtpMap.keySet());
            return OtpResult.NOT_FOUND;
        }

        // 2. Hết hạn → xoá luôn
        if (entry.isExpired()) {
            System.out.println("[OTP DEBUG] EXPIRED - Email: " + email + ", Stored OTP: " + entry.code + 
                    ", Input OTP: " + inputOtp + ", Expired At: " + entry.expiresAt);
            emailOtpMap.remove(email);
            return OtpResult.EXPIRED;
        }

        // 3. Sai mã
        if (!entry.code.equals(inputOtp)) {
            System.out.println("[OTP DEBUG] WRONG_CODE - Email: " + email + ", Stored OTP: " + entry.code + 
                    ", Input OTP: " + inputOtp);
            return OtpResult.WRONG_CODE;
        }

        // 4. Đúng nhưng đã dùng rồi (replay attack) → markUsed() trả false
        if (!entry.markUsed()) {
            System.out.println("[OTP DEBUG] ALREADY_USED - Email: " + email + ", OTP: " + entry.code);
            emailOtpMap.remove(email);   // dọn luôn
            return OtpResult.ALREADY_USED;
        }

        // 5. Hợp lệ → xoá khỏi map ngay lập tức
        System.out.println("[OTP DEBUG] SUCCESS - Email: " + email + ", OTP verified and consumed");
        emailOtpMap.remove(email);
        return OtpResult.SUCCESS;
    }

    public enum OtpResult {
        SUCCESS,
        NOT_FOUND,    // không tồn tại hoặc chưa gửi OTP
        EXPIRED,      // quá 5 phút
        WRONG_CODE,   // sai mã
        ALREADY_USED  // đã dùng rồi (replay)
    }

    /** Xoá OTP thủ công nếu cần (ví dụ: user đăng ký lại) */
    public void removeVerificationCode(String email) {
        emailOtpMap.remove(email);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void storeOtp(String email, String otp) {
        // put() tự ghi đè entry cũ — đảm bảo mỗi email chỉ có 1 OTP active
        emailOtpMap.put(email, new OtpEntry(otp));
        System.out.println("[OTP DEBUG] Stored OTP - Email: " + email + ", OTP: " + otp + 
                ", Current map size: " + emailOtpMap.size());
    }

    private void evictExpiredEntries() {
        emailOtpMap.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    private void sendEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    private String buildEmailHtml(String title, String subtitle, String otp,
                                   String color, String footer) {
        return "<div style='font-family:sans-serif;max-width:480px;margin:0 auto'>"
                + "<div style='text-align:center;padding:32px'>"
                + "<h2 style='color:#1a1a1a;margin:0 0 24px'>" + title + "</h2>"
                + "<p style='color:#555;margin:0 0 32px;line-height:1.6'>" + subtitle + "</p>"
                + "<div style='font-size:36px;font-weight:700;letter-spacing:8px;"
                + "color:" + color + ";margin:24px 0;text-align:center;font-family:monospace'>" + otp + "</div>"
                + "<p style='color:#666;margin:24px 0;line-height:1.6'>"
                + "Mã này có hiệu lực trong <strong>" + OTP_EXPIRY_MINUTES + " phút</strong>.</p>"
                + "<p style='color:#999;font-size:12px;margin:16px 0;line-height:1.6'>"
                + "⚠️ Không chia sẻ mã này cho bất kỳ ai.</p>"
                + "<p style='color:#999;font-size:12px;margin:0;line-height:1.6'>" + footer + "</p>"
                + "</div>"
                + "<div style='border-top:1px solid #ddd;padding:16px;text-align:center;color:#999;font-size:11px'>"
                + "<p>© 2026 TREEdu. Trang web được phục vụ cho mục đích học tập.</p>"
                + "</div></div>";
    }
}
