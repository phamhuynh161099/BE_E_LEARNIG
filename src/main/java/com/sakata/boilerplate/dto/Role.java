package com.sakata.boilerplate.dto;

import java.util.List;
import java.util.Set;

import com.sakata.boilerplate.dto.Permission.PermissionResponse;

import jakarta.validation.constraints.NotBlank;

public class Role {
    public record RoleResponse(Long id, String name, String description, List<PermissionResponse> permissions) {
    }

    public record CreateRoleRequest(@NotBlank String name, String description) {
    }

    public record AssignPermissionsRequest(Set<Long> permissionIds) {
    }

    public record UpdateRoleRequest(String roleId, String[] permissionIds, String name, String description) {
    }

    public record AddRoleRequest(String roleId, String[] permissionIds, String name, String description) {
    }
}
