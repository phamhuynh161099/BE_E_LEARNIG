package com.sakata.boilerplate.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakata.boilerplate.audit.AuditService;
import com.sakata.boilerplate.audit.models.AuditLog;
import com.sakata.boilerplate.dto.Auth.RegisterRequest;
import com.sakata.boilerplate.dto.Permission.PermissionResponse;
import com.sakata.boilerplate.dto.Role.RoleResponse;
import com.sakata.boilerplate.dto.User.AddUserRequest;
import com.sakata.boilerplate.dto.User.UpdateUserRequest;
import com.sakata.boilerplate.dto.User.UserResponse;
import com.sakata.boilerplate.mapper.primary.RoleMapper;
import com.sakata.boilerplate.mapper.primary.UserMapper;
import com.sakata.boilerplate.models.Permission;
import com.sakata.boilerplate.models.Role;
import com.sakata.boilerplate.models.User;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true, transactionManager = "primaryTransactionManager")
public class UserService {

    // MySQL (primary)
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    // Redis
    private final PermissionCacheService cacheService;

    // PostgreSQL (audit) — async, không block
    private final AuditService auditService;

    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // ── Register ─────────────────────────────────────────────────

    @Transactional(transactionManager = "primaryTransactionManager")
    public UserResponse register(RegisterRequest req, HttpServletRequest request) {
        if (userMapper.existsByUsername(req.username()))
            throw new IllegalArgumentException("Username đã tồn tại: " + req.username());
        if (userMapper.existsByEmail(req.email()))
            throw new RuntimeException("Email đã được sử dụng");

        Role defaultRole = roleMapper.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER không tồn tại"));

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .enabled(true)
                .build();

        userMapper.insert(user); // → MySQL
        userMapper.insertUserRole(user.getId(), defaultRole.getId());

        User saved = userMapper.findById(user.getId()).orElseThrow();

        // Ghi audit log → PostgreSQL (async)
        auditService.log("system", null, AuditLog.ACTION_CREATE,
                "user", String.valueOf(user.getId()), null, toJson(saved), request);

        return toResponse(saved);
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public Object addNewUser(AddUserRequest req) {
        try {

            if (userMapper.existsByUsername(req.username()))
                throw new IllegalArgumentException("Username đã tồn tại: " + req.username());
            if (userMapper.existsByEmail(req.email()))
                throw new RuntimeException("Email đã được sử dụng");

            User user = User.builder()
                    .username(req.username())
                    .email(req.email())
                    .password(passwordEncoder.encode(req.password()))
                    .enabled(true)
                    .build();

            userMapper.insert(user);

            System.out.println("ADD_NEW_USER" + req.roleIDs() + user.getId());
            var affectedRowsAssignRole = userMapper.insertMultipleRoleForUser(user.getId().toString(), req.roleIDs());

            return true;
        } catch (Exception e) {
            // TODO: handle exception
            System.err.println("Lỗi khi insert dữ liệu: " + e.getMessage());

            return false;
        }
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public Object updateUser(UpdateUserRequest req) {
        try {

            // var findedUser = userMapper.findByEmail(req.email());
            // System.out.println("findedUser" + findedUser.get());

            var encodedPassword = "";
            if (req.password() != null && !req.password().isBlank()) {
                encodedPassword = passwordEncoder.encode(req.password());
            }

            userMapper.updateUserWithPasswordCondition(req, encodedPassword);

            /**
             * 👏
             * Xóa các role cũ
             * Thêm các role mới 
             */
            userMapper.deleteUserRoles(Long.parseLong(req.id()));
            userMapper.insertMultipleRoleForUser(req.id().toString(), req.roleIDs());

            // System.out.println("ADD_NEW_USER" + req.roleIDs() + user.getId());
            // var affectedRowsAssignRole =
            // userMapper.insertMultipleRoleForUser(user.getId().toString(), req.roleIDs());

            return true;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            System.err.println("EROR: " + e);

            return false;
        }
    }

    // ── Queries ───────────────────────────────────────────────────

    public UserResponse getById(Long id) {
        return toResponse(userMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tìm thấy: id=" + id)));
    }

    public UserResponse getByUsername(String username) {
        return toResponse(userMapper.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User không tìm thấy: " + username)));
    }

    public List<UserResponse> getAll() {
        return userMapper.findAll().stream().map(this::toResponse).toList();
    }

    // ── Assign Roles ──────────────────────────────────────────────

    @Transactional(transactionManager = "primaryTransactionManager")
    public UserResponse assignRoles(Long userId, List<Long> roleIds,
            String actorName, Long actorId,
            HttpServletRequest request) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tìm thấy: id=" + userId));

        String oldSnapshot = toJson(user);

        List<Role> roles = roleMapper.findByIds(new HashSet<>(roleIds));
        if (roles.size() != roleIds.size()) {
            throw new IllegalArgumentException("Một số roleId không hợp lệ");
        }

        // Replace user roles → MySQL
        userMapper.deleteUserRoles(userId);
        roles.forEach(r -> userMapper.insertUserRole(userId, r.getId()));

        // Xóa permission cache của user → Redis
        // cacheService.evictPermissions(user.getUsername());

        User updated = userMapper.findById(userId).orElseThrow();

        // Audit log → PostgreSQL (async)
        auditService.log(actorName, actorId, AuditLog.ACTION_UPDATE,
                "user", String.valueOf(userId), oldSnapshot, toJson(updated), request);

        return toResponse(updated);
    }

    // ── Delete ────────────────────────────────────────────────────

    @Transactional(transactionManager = "primaryTransactionManager")
    public void deleteById(Long id, String actorName, Long actorId, HttpServletRequest request) {
        User user = userMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tìm thấy: id=" + id));

        String snapshot = toJson(user);
        userMapper.deleteById(id); // → MySQL

        // cacheService.evictPermissions(user.getUsername()); // → Redis

        auditService.log(actorName, actorId, AuditLog.ACTION_DELETE,
                "user", String.valueOf(id), snapshot, null, request); // → PostgreSQL
    }

    // ── Permission loading (with Redis cache) ─────────────────────

    public List<String> loadPermissions(String username) {
        // 1. Check Redis cache
        // List<String> cached = cacheService.getPermissions(username);
        List<String> cached = null;
        if (cached != null)
            return cached;

        // 2. Load từ MySQL
        User user = userMapper.findByUsername(username).orElse(null);
        if (user == null)
            return List.of();

        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .distinct().sorted().toList();

        // 3. Set vào Redis
        // cacheService.setPermissions(username, permissions);

        return permissions;
    }

    // ── Helpers ───────────────────────────────────────────────────

    UserResponse toResponse(User user) {
        var roleResponses = user.getRoles().stream()
                .map(r -> new RoleResponse(
                        r.getId(), r.getName(), r.getDescription(),
                        r.getPermissions().stream()
                                .map(p -> new PermissionResponse(p.getId(), p.getName(), p.getResource(), p.getAction(),
                                        p.getDescription()))
                                .sorted(Comparator.comparing(PermissionResponse::name)).toList()))
                .sorted(Comparator.comparing(RoleResponse::name)).toList();

        var flatPerms = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .distinct().sorted().toList();

        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.isEnabled(), roleResponses, flatPerms);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}