package com.sakata.boilerplate.mapper.audit;

import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilter {
    private Long actorId;
    private String action;
    private String resource;
    private String resourceId;
    private String status;
    private OffsetDateTime fromDate;
    private OffsetDateTime toDate;
    private int page;
    private int size;

    public int getOffset() {
        return page * size;
    }
}
