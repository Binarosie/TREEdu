package vn.hcmute.edu.materialsservice.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.Service.Impl.UserServiceImpl;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.security.JwtTokenUtil;

import java.io.IOException;
import java.net.URLEncoder;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserServiceImpl userService;
    private final JwtTokenUtil jwtTokenUtil;

    // ✅ FIX: Sử dụng Constructor Injection với @Lazy
    public OAuth2SuccessHandler(@Lazy UserServiceImpl userService,
                                JwtTokenUtil jwtTokenUtil) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        System.out.println("=== OAuth2 Success Handler ===");
        System.out.println("Email: " + email);
        System.out.println("Name: " + name);

        // Tìm hoặc tạo user
        User user = userService.createOAuthMember(email, name);

        System.out.println("User created/found: " + user.getEmail());
        System.out.println("User type: " + user.getClass().getSimpleName());

        // Tạo JWT token
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtTokenUtil.generateToken(userDetails);

        System.out.println("JWT token generated successfully");

        // Set cookie
        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        jwtCookie.setSecure(false); // Set true in production with HTTPS
        response.addCookie(jwtCookie);

        System.out.println("JWT cookie set successfully");

        // Lấy role từ CustomUserDetails
        String roleName = userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> {
                    String auth = authority.getAuthority();
                    // Remove "ROLE_" prefix if exists
                    return auth.startsWith("ROLE_") ? auth.substring(5) : auth;
                })
                .orElse("MEMBER");

        System.out.println("User role: " + roleName);

        // ✅ FIX: Đổi port thành 3000
        String redirectUrl = String.format(
                "http://localhost:3000/oauth2/redirect?email=%s&name=%s&role=%s",
                URLEncoder.encode(email, "UTF-8"),
                URLEncoder.encode(name, "UTF-8"),
                URLEncoder.encode(roleName, "UTF-8")
        );

        System.out.println("Redirecting to: " + redirectUrl);
        System.out.println("=== End OAuth2 Success Handler ===");

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}