package com.sakata.boilerplate.mapper.audit;

import org.apache.ibatis.annotations.*;

import com.sakata.boilerplate.audit.models.AuditLog;

import java.util.List;

/**
 * Thuộc về AUDIT datasource (PostgreSQL).
 * Hoàn toàn tách biệt với primary datasource.
 */
@Mapper
public interface AuditLogMapper {

    @Insert("""
            INSERT INTO audit_logs
                (actor_id, actor_name, action, resource, resource_id,
                 old_value, new_value, ip_address, user_agent,
                 request_uri, http_method, status, error_msg)
            VALUES
                (#{actorId}, #{actorName}, #{action}, #{resource}, #{resourceId},
                 #{oldValue}, #{newValue}, #{ipAddress}, #{userAgent},
                 #{requestUri}, #{httpMethod}, #{status}, #{errorMsg})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog log);

    // Queries phức tạp (filter, pagination) → XML mapper
    List<AuditLog> findByFilter(AuditLogFilter filter);

    @Select("SELECT COUNT(*) FROM audit_logs WHERE actor_id = #{actorId}")
    long countByActorId(Long actorId);
}