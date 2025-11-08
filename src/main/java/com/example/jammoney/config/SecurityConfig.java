package com.example.jammoney.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.jammoney.auth.jwt.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 엔드포인트
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test/gpt/**").permitAll()
                        .requestMatchers("/test/**").permitAll()

                        // 보호 엔드포인트
                        .requestMatchers("/api/protected").authenticated()
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 정확한 오리진을 HTTPS/HTTP 모두 추가
        cfg.setAllowedOrigins(List.of(
                "http://127.0.0.1:3000",
                "http://127.0.0.1:8080",
                "http://127.0.0.1:5500",
                "http://127.0.0.1:5501",
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:5500",
                "http://localhost:5501",
                "https://127.0.0.1:3000",
                "https://127.0.0.1:8080",
                "https://127.0.0.1:5500",
                "https://127.0.0.1:5501",
                "https://localhost:3000",
                "https://localhost:8080",
                "https://localhost:5500",
                "https://localhost:5501",
                "https://jm-money.com",
                "https://www.jm-money.com",
                "https://jammoney.netlify.app"
        ));

        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of(
                "Authorization","Content-Type","X-Requested-With",
                "Accept","Origin","Cache-Control","Pragma"
        ));

        // 응답 헤더 노출(토큰을 헤더로 돌릴 때만 필요)
        cfg.setExposedHeaders(List.of("Authorization","Location"));

        // 쿠키/인증 포함 요청 허용
        cfg.setAllowCredentials(true);

        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

}
