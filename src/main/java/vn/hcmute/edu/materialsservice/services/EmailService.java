package vn.hcmute.edu.materialsservice.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final RestClient restClient = RestClient.create("https://api.resend.com");

    private final ConcurrentHashMap<String, OtpEntry> emailOtpMap = new ConcurrentHashMap<>();

    private final SecureRandom secureRandom = new SecureRandom();

    private static final long OTP_EXPIRY_MINUTES = 5;

    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
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

    private static class OtpEntry {
        final String code;
        final LocalDateTime expiresAt;
        volatile boolean used = false;

        OtpEntry(String code) {
            this.code = code;
            this.expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }

        synchronized boolean markUsed() {
            if (used)
                return false;
            used = true;
            return true;
        }
    }

    public String generateOtp() {
        int code = 100_000 + secureRandom.nextInt(900_000);
        String otp = String.valueOf(code);
        log.debug("[OTP] Generated new OTP: {}", otp);
        return otp;
    }

    /**
     * Gửi email xác thực tài khoản — CHẠY BẤT ĐỒNG BỘ, không block request.
     * Lưu OTP trước, gửi mail sau (kể cả gửi mail fail thì OTP vẫn được lưu để
     * retry).
     */
    public void sendVerificationEmail(String to, String otp) {
        storeOtp(to, otp);
        String html = buildEmailHtml(
                "Xác thực tài khoản TREEdu",
                "Mã xác thực của bạn là:",
                otp,
                "#1D9E75",
                "Nếu bạn không đăng ký tài khoản, hãy bỏ qua email này.");
        sendEmailAsync(to, "Mã xác thực tài khoản - TREEdu", html);
    }

    public void sendResetPasswordEmail(String to, String otp) {
        storeOtp(to, otp);
        String html = buildEmailHtml(
                "Đặt lại mật khẩu TREEdu",
                "Mã đặt lại mật khẩu của bạn là:",
                otp,
                "#D85A30",
                "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.");
        sendEmailAsync(to, "Mã đặt lại mật khẩu - TREEdu", html);
    }

    public OtpResult verifyAndConsumeOtp(String email, String inputOtp) {
        OtpEntry entry = emailOtpMap.get(email);

        if (entry == null) {
            log.debug("[OTP] NOT_FOUND - Email: {}", email);
            return OtpResult.NOT_FOUND;
        }

        if (entry.isExpired()) {
            log.debug("[OTP] EXPIRED - Email: {}", email);
            emailOtpMap.remove(email);
            return OtpResult.EXPIRED;
        }

        if (!entry.code.equals(inputOtp)) {
            log.debug("[OTP] WRONG_CODE - Email: {}", email);
            return OtpResult.WRONG_CODE;
        }

        if (!entry.markUsed()) {
            log.debug("[OTP] ALREADY_USED - Email: {}", email);
            emailOtpMap.remove(email);
            return OtpResult.ALREADY_USED;
        }

        log.debug("[OTP] SUCCESS - Email: {}", email);
        emailOtpMap.remove(email);
        return OtpResult.SUCCESS;
    }

    public enum OtpResult {
        SUCCESS, NOT_FOUND, EXPIRED, WRONG_CODE, ALREADY_USED
    }

    public void removeVerificationCode(String email) {
        emailOtpMap.remove(email);
    }

    private void storeOtp(String email, String otp) {
        emailOtpMap.put(email, new OtpEntry(otp));
        log.debug("[OTP] Stored OTP - Email: {}, Current map size: {}", email, emailOtpMap.size());
    }

    private void evictExpiredEntries() {
        emailOtpMap.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /**
     * Gửi email qua Resend HTTP API (port 443) — chạy ở thread riêng nhờ @Async,
     * không làm timeout request register/login.
     */
    @Async
    public void sendEmailAsync(String to, String subject, String htmlBody) {
        try {
            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", List.of(to),
                    "subject", subject,
                    "html", htmlBody);

            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.debug("[EMAIL] Sent successfully to: {}", to);
        } catch (RestClientException e) {
            log.error("[EMAIL] Failed to send to {}: {}", to, e.getMessage());
            // Không throw lại — vì đây là async, throw sẽ không ai catch được.
            // OTP đã lưu sẵn nên user có thể bấm "Resend OTP" để thử lại.
        }
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