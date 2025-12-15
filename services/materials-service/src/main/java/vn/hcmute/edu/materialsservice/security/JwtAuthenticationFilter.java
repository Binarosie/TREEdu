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

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // In JwtAuthenticationFilter - improve error handling
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (EXCLUDED_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // Extract token from cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Only process if token exists
        if (token != null) {
            try {
                final String username = jwtTokenUtil.getUsernameFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = customUserDetailsService.loadUserByUsername(username);

                    if (jwtTokenUtil.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
//                throw new InternalServerError("Internal server error occurred");
                // Tùy chọn: log lại lỗi nếu muốn debug
                e.printStackTrace();

                // Redirect về trang lỗi (HTML)
                response.sendRedirect("/error");
                return;
            }
        }

        // Always continue filter chain
        filterChain.doFilter(request, response);
    }

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/auth/logi",
            "/api/auth/logout",
            "/api/users/newMember",
            "/api/auth/verify-email",
            "/api/auth/request-reset-password",
            "/api/auth/reset-password"
    );
}