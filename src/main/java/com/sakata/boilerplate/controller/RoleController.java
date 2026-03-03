package com.sakata.boilerplate.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sakata.boilerplate.dto.Auth.LoginRequest;
import com.sakata.boilerplate.dto.Auth.TokenResponse;
import com.sakata.boilerplate.dto.Role.AddRoleRequest;
import com.sakata.boilerplate.dto.Role.AssignPermissionsRequest;
import com.sakata.boilerplate.dto.Role.CreateRoleRequest;
import com.sakata.boilerplate.dto.Role.RoleResponse;
import com.sakata.boilerplate.dto.Role.UpdateRoleRequest;
import com.sakata.boilerplate.service.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<?> list() {
        try {
            var data = roleService.getAll();

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/add-role")
    // @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<?> addRole(@RequestBody AddRoleRequest req) {
        try {
            // var data = roleService.assignPermissionsToRole(req.roleId(), req.permissionIds());
            var data = roleService.addNewRole(req);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/update-role")
    // @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<?> updateRole(@RequestBody UpdateRoleRequest req) {
        try {
            // var data = roleService.assignPermissionsToRole(req.roleId(), req.permissionIds());
            var data = roleService.updateRole(req);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // @PostMapping
    // @PreAuthorize("hasAuthority('role:write')")
    // public RoleResponse create(@Valid @RequestBody CreateRoleRequest req) {
    // return roleService.create(req);
    // }

    // @PutMapping("/{id}/permissions")
    // @PreAuthorize("hasAuthority('role:update')")
    // public RoleResponse assignPermissions(@PathVariable Long id,
    // @RequestBody AssignPermissionsRequest req) {
    // return roleService.assignPermissions(id, req.permissionIds());
    // }

    // @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('role:delete')")
    // public ResponseEntity<Void> delete(@PathVariable Long id) {
    // roleService.delete(id);
    // return ResponseEntity.noContent().build();
    // }
}
