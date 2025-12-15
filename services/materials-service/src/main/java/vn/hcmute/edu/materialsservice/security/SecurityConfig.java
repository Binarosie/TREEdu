package vn.hcmute.edu.materialsservice.security;

import vn.hcmute.edu.materialsservice.Model.User;
import vn.hcmute.edu.materialsservice.exception.CustomAccessDeniedHandler;
import vn.hcmute.edu.materialsservice.exception.CustomAuthenticationEntryPoint;
import vn.hcmute.edu.materialsservice.Dto.request.users.CreateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/api/auth/**",
                                        "/auth/**",
                                        "/oauth2/authorization/google",
                                        "/error"
                                ).permitAll()
                                .requestMatchers("/").permitAll()
//                        .requestMatchers(
//                                "/member/payment/vn-pay-callback").permitAll()
                                .requestMatchers(
                                        "/assets/**",
                                        "/templates/**",
                                        "/static/**",
                                        "/favicon.ico",
                                        "/css/**", "/js/**", "/images/**"
                                ).permitAll()
                                .requestMatchers("/api/users/newMember").permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .oauth2Login(login -> login
                        .loginPage("/au/login")
                        .successHandler((request, response, authentication)
                                -> request.getRequestDispatcher("/auth/login/oauth2Google-submit").forward(request, response))
                        .permitAll());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}