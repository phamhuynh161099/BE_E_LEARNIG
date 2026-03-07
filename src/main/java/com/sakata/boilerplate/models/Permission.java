package com.sakata.boilerplate.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Database table = permission
public class Permission {
    private Long id;
    private String name; // "user:read"
    private String resource; // "user"
    private String action; // "read"
    private String description;
    private String category;
}