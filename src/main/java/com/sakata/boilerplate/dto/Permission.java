package com.sakata.boilerplate.dto;

public class Permission {
    public record PermissionResponse(Long id, String name, String resource, String action, String description) {}
}
