package vn.hcmute.edu.authservice.controller;

import java.security.Principal;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import vn.hcmute.edu.authservice.constants.Messages;
import vn.hcmute.edu.authservice.dto.LoginRequest;
import vn.hcmute.edu.authservice.dto.RegisterRequest;
import vn.hcmute.edu.authservice.dto.TokensResponse;
import vn.hcmute.edu.authservice.response.ResponseData;
import vn.hcmute.edu.authservice.security.JwtService;
import vn.hcmute.edu.authservice.service.iAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class AuthController {

    private final iAuthService authService;
    private final JwtService jwtService;

    @GetMapping()
    public ResponseData<String> healthCheck() {
        return ResponseData.success(Messages.Success.AUTH_SERVICE_RUNNING,
                Messages.Success.AUTH_SERVICE_RUNNING);
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseData<TokensResponse>> register(
            @Valid @RequestBody RegisterRequest req,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        var issued = authService.register(req, deviceId);
        var cookie = ResponseCookie.from(Messages.Misc.REFRESH_TOKEN_COOKIE_NAME, issued.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(jwtService.getRefreshExpMs()))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ResponseData.success(
                        Messages.Success.USER_REGISTERED,
                        new TokensResponse(issued.getAccessToken(), issued.getExpiresIn())));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseData<TokensResponse>> login(
            @Valid @RequestBody LoginRequest req,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        var issued = authService.login(req, deviceId);
        var cookie = ResponseCookie.from(Messages.Misc.REFRESH_TOKEN_COOKIE_NAME, issued.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(jwtService.getRefreshExpMs()))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ResponseData.success(
                        Messages.Success.TOKENS_ISSUED,
                        new TokensResponse(issued.getAccessToken(), issued.getExpiresIn())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseData<Void>> logout(
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, Messages.Error.UNAUTHORIZED);
        }
        authService.logout(deviceId, principal.getName());

        // Xoá cookie refresh token (RT)
        ResponseCookie deleteCookie = ResponseCookie.from(Messages.Misc.REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0) // hết hạn ngay
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ResponseData.success(Messages.Success.LOGGED_OUT));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseData<TokensResponse>> refresh(
            @CookieValue(name = Messages.Misc.REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        var issued = authService.refresh(refreshToken, deviceId);
        var cookie = ResponseCookie.from(Messages.Misc.REFRESH_TOKEN_COOKIE_NAME, issued.getRefreshToken())// rotate
                .httpOnly(true).secure(true).sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMillis(jwtService.getRefreshExpMs()))
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ResponseData.success(
                        Messages.Success.TOKENS_REFRESHED,
                        new TokensResponse(issued.getAccessToken(), issued.getExpiresIn())));
    }
}