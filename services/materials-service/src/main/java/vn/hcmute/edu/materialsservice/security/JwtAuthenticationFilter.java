package vn.hcmute.edu.materialsservice.security;

import vn.hcmute.edu.materialsservice.Dto.response.InternalServerError;
import vn.hcmute.edu.materialsservice.Service.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        System.out.println("=== JWT FILTER === " + request.getRequestURI());

        if (EXCLUDED_PATHS.contains(request.getRequestURI())) {
            System.out.println("Path is EXCLUDED, skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // Extract token from cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println("Found cookie: " + cookie.getName());
                if ("JWT".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println("JWT token found: " + token.substring(0, Math.min(20, token.length())) + "...");
                    break;
                }
            }
        }

        System.out.println("Token from cookie JWT: " + (token != null ? "FOUND" : "NOT FOUND"));

        // Only process if token exists
        if (token != null) {
            try {
                final String username = jwtTokenUtil.getUsernameFromToken(token);
                System.out.println("Username from token: " + username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = customUserDetailsService.loadUserByUsername(username);
                    System.out.println("UserDetails loaded: " + userDetails.getUsername());
                    System.out.println("Authorities: " + userDetails.getAuthorities());

                    if (jwtTokenUtil.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("Authentication SET successfully");
                    } else {
                        System.out.println("Token is INVALID");
                    }
                } else if (username == null) {
                    System.out.println("Username is NULL from token");
                } else {
                    System.out.println("Authentication already exists in SecurityContext");
                }
            } catch (Exception e) {
                // throw new InternalServerError("Internal server error occurred");
                // Tùy chọn: log lại lỗi nếu muốn debug
                System.out.println("!!! JWT FILTER EXCEPTION: " + e.getMessage());
                e.printStackTrace();

                // Redirect về trang lỗi (HTML)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            System.out.println("No JWT token found, proceeding as GUEST");
        }

        System.out.println("=== END JWT FILTER ===");
        // Always continue filter chain
        filterChain.doFilter(request, response);
    }

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/login/oauth2",
            "/api/users/newMember",
            "/api/auth/verify-email-link",
            "/api/auth/request-reset-password",
            "/api/auth/reset-password");
    // Không include /api/quiz - để SecurityConfig permitAll xử lý
}
