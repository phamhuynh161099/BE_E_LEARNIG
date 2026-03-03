package com.sakata.boilerplate.controller;

import com.sakata.boilerplate.audit.AuditService;
import com.sakata.boilerplate.audit.models.AuditLog;
import com.sakata.boilerplate.dto.*;
import com.sakata.boilerplate.dto.Auth.LoginRequest;
import com.sakata.boilerplate.dto.Auth.RegisterRequest;
import com.sakata.boilerplate.dto.Auth.TokenResponse;
import com.sakata.boilerplate.dto.User.UserResponse;
import com.sakata.boilerplate.security.*;
import com.sakata.boilerplate.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuditService auditService;
    private final PermissionCacheService cacheService;

    /**
     * POST /api/auth/login
     * → JWT token từ MySQL, audit log → PostgreSQL
     */
    @PostMapping("/login")
    // TokenResponse
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest req
    // ,HttpServletRequest request

    ) {
        try {
            // System.out.println(">>>req" + req);
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password()));
            String token = jwtUtil.generate(auth);
            // Audit log → PostgreSQL (async)
            // auditService.log(req.username(), null, AuditLog.ACTION_LOGIN,
            // "auth", null, null, null, request);
            var accountInfo = userService.getByUsername(auth.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("tokenInfo", TokenResponse.of(token, jwtUtil.getExpirationMs()));
            response.put("accountInfo", accountInfo);

            return ResponseEntity.ok(response);

            // return ResponseEntity.ok(TokenResponse.of(token,
            // jwtUtil.getExpirationMs()),data);

        } catch (BadCredentialsException ex) {
            // Log thất bại → PostgreSQL (async)
            // auditService.logFailure(req.username(), null, AuditLog.ACTION_LOGIN,
            // "auth", "Bad credentials", request);
            System.out.println(">>>ex" + ex);
            throw ex;
        }
    }

    /**
     * POST /api/auth/logout
     * → Thêm token vào Redis blacklist
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication auth,
            @RequestHeader("Authorization") String bearerToken,
            HttpServletRequest request) {
        String token = bearerToken.substring(7);
        String jti = jwtUtil.extractJti(token); // JWT ID
        cacheService.blacklistToken(jti); // → Redis

        auditService.log(auth.getName(), null, AuditLog.ACTION_LOGOUT,
                "auth", null, null, null, request); // → PostgreSQL (async)

        // Xóa permission cache
        cacheService.evictPermissions(auth.getName()); // → Redis

        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(req, request));
    }

    /**
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication auth) {
        return ResponseEntity.ok(userService.getByUsername(auth.getName()));
    }
}
