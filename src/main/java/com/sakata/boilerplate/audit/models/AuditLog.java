package com.sakata.boilerplate.audit.models;

import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    private Long id;
    private Long actorId;
    private String actorName;

    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, ACCESS_DENIED
    private String resource; // user, role, permission
    private String resourceId;

    private String oldValue; // JSON string
    private String newValue; // JSON string

    private String ipAddress;
    private String userAgent;
    private String requestUri;
    private String httpMethod;

    private String status; // SUCCESS, FAILURE
    private String errorMsg;

    private OffsetDateTime createdAt;

    // ── Enum constants ──────────────────────────────
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_ACCESS_DENIED = "ACCESS_DENIED";

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
}