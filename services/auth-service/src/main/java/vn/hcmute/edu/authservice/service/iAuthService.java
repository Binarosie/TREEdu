package vn.hcmute.edu.authservice.service;

import vn.hcmute.edu.authservice.dto.IssuedTokens;
import vn.hcmute.edu.authservice.dto.LoginRequest;
import vn.hcmute.edu.authservice.dto.RegisterRequest;

public interface iAuthService {
    IssuedTokens register(RegisterRequest req, String deviceId);
    IssuedTokens login(LoginRequest req, String deviceId);
    IssuedTokens refresh(String refreshTokenFromCookie, String deviceId);
    void logout(String deviceId, String username);
}