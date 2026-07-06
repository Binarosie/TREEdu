package vn.hcmute.edu.materialsservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;
import vn.hcmute.edu.materialsservice.exceptions.CustomAccessDeniedHandler;
import vn.hcmute.edu.materialsservice.exceptions.CustomAuthenticationEntryPoint;
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
import vn.hcmute.edu.materialsservice.utils.OAuth2SuccessHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Autowired
        private CustomAccessDeniedHandler customAccessDeniedHandler;

        @Autowired
        private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @Autowired
        private OAuth2SuccessHandler oAuth2SuccessHandler;

        @Value("${app.base-url}")
        private String frontendUrl;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Auth endpoints
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/auth/**",
                                                                "/oauth2/**",
                                                                "/login/oauth2/**",
                                                                "/error")
                                                .permitAll()
                                                .requestMatchers("/").permitAll()

                                                // Static files - file audio/video lưu trên server
                                                .requestMatchers("/uploads/**").permitAll()

                                                // Dictation - GET public cho Member, các method khác cần auth
                                                .requestMatchers(HttpMethod.GET, "/api/dictation").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/dictation/**").permitAll()

                                                // Flashcard endpoints
                                                .requestMatchers(
                                                                "/api/flashcards",
                                                                "/api/flashcards/*",
                                                                "/api/flashcards/*/words",
                                                                "/api/flashcards/*/words/*",
                                                                "/api/flashcards/*/details",
                                                                "/api/flashcards/level/*",
                                                                "/api/flashcards/topic/*")
                                                .permitAll()

                                                // Quiz endpoints
                                                .requestMatchers(HttpMethod.GET, "/api/quiz").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/quiz/*").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/quiz/*").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/quiz/topic/*").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/quiz/level/*").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/quiz/search").permitAll()

                                                // Static resources
                                                .requestMatchers(
                                                                "/assets/**",
                                                                "/templates/**",
                                                                "/static/**",
                                                                "/favicon.ico",
                                                                "/css/**", "/js/**", "/images/**")
                                                .permitAll()

                                                .requestMatchers("/api/users/newMember").permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                                .accessDeniedHandler(customAccessDeniedHandler))
                                .oauth2Login(login -> login
                                                .loginPage("/auth/login")
                                                .successHandler(oAuth2SuccessHandler)
                                                // .failureUrl("http://localhost:3000/login?error=oauth_failed")
                                                .failureUrl(frontendUrl.replaceAll("/$", "")
                                                                + "/login?error=oauth_failed")
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
