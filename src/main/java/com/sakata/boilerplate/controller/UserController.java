package com.sakata.boilerplate.controller;

import com.sakata.boilerplate.dto.*;
import com.sakata.boilerplate.service.UserService;
import com.sakata.boilerplate.dto.User.AddUserRequest;
import com.sakata.boilerplate.dto.User.AssignRolesRequest;
import com.sakata.boilerplate.dto.User.UpdateUserRequest;
import com.sakata.boilerplate.dto.User.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Requires permission "user:read" OR role ADMIN
    @PostMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<?> list() {
        try {
            var data = userService.getAll();

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public UserResponse get(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping("/add-user")
    // @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<?> addNewUser(@RequestBody AddUserRequest req) {
        try {
            System.out.println("CON_ADD_NEW_USER" + req);
            var data = userService.addNewUser(req);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/update-user")
    // @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest req) {
        try {
            System.out.println("CON_ADD_NEW_USER" + req);
            var data = userService.updateUser(req);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // @PutMapping("/{id}/roles")
    // @PreAuthorize("hasAuthority('user:update') and hasRole('ADMIN')")
    // public UserResponse assignRoles(@PathVariable Long id,
    // @Valid @RequestBody AssignRolesRequest req) {
    // return userService.assignRoles(id, req.roleIds());
    // }

    // @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('user:delete')")
    // public ResponseEntity<Void> delete(@PathVariable Long id) {
    // userService.deleteById(id);
    // return ResponseEntity.noContent().build();
    // }
}
