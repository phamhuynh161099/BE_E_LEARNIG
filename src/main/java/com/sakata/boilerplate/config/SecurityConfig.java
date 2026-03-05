package com.sakata.boilerplate.config;

import com.sakata.boilerplate.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    // @Value("${app.cors.allowed-origins}")
    // private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // .cors(c -> c.configurationSource(corsSource()))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        // .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ← cho phép preflight
                        // .requestMatchers("/api/videos/stream/**").permitAll() // ⛽ NEED REMOVE
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // @Bean
    // CorsConfigurationSource corsSource() {
    // var cfg = new CorsConfiguration();
    // cfg.setAllowedOrigins(List.of(allowedOrigins.split(",")));
    // cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS",
    // "PATCH"));
    // cfg.setAllowedHeaders(List.of("*"));
    // cfg.setAllowCredentials(true);
    // var src = new UrlBasedCorsConfigurationSource();
    // src.registerCorsConfiguration("/**", cfg);
    // return src;
    // }

    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    // CorsConfiguration config = new CorsConfiguration();
    // config.setAllowedOriginPatterns(List.of("*"));
    // config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    // config.setAllowedHeaders(List.of("*"));
    // config.setAllowCredentials(true);

    // UrlBasedCorsConfigurationSource source = new
    // UrlBasedCorsConfigurationSource();
    // source.registerCorsConfiguration("/**", config);
    // return source;
    // }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*")); // hoặc chỉ định: "http://localhost:3000"
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Range", // ← quan trọng cho byte-range streaming
                "Origin",
                "X-Requested-With"));
        config.setExposedHeaders(List.of(
                "Content-Range", // ← cần expose để client đọc được
                "Accept-Ranges",
                "Content-Length",
                "Content-Type"));
        config.setAllowCredentials(false); // ← để false nếu dùng allowedOriginPatterns("*")

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}