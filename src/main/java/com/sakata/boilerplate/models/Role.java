
package com.sakata.boilerplate.models;

import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Database table = permission
public class Role {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}