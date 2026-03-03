package com.sakata.boilerplate.dto;

import java.util.List;

import com.sakata.boilerplate.dto.Role.RoleResponse;

import jakarta.validation.constraints.NotEmpty;

public class User {
    public record UserResponse(
            Long id, String username, String email, boolean enabled,
            List<RoleResponse> roles, List<String> permissions) {
    }

    public record AssignRolesRequest(@NotEmpty List<Long> roleIds) {
    }

    public record AddUserRequest(String username, String[] roleIDs, String email, String password) {
    }

    public record UpdateUserRequest(String id, String username, String[] roleIDs, String email, String password) {
    }
}
