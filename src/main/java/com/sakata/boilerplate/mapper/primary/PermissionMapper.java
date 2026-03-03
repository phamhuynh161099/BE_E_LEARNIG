package com.sakata.boilerplate.mapper.primary;

import com.sakata.boilerplate.models.Permission;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface PermissionMapper {

    @Select("SELECT id, name, resource, action, description FROM permissions WHERE id = #{id}")
    Optional<Permission> findById(Long id);

    @Select("SELECT id, name, resource, action, description, category FROM permissions")
    List<Permission> findAll();

    // XML-mapped (foreach)
    List<Permission> findByIds(Set<Long> ids);

    List<Permission> findByRoleId(Long roleId);

    @Insert("INSERT INTO permissions (name, resource, action, description) VALUES (#{name}, #{resource}, #{action}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Permission permission);

    @Delete("DELETE FROM permissions WHERE id = #{id}")
    int deleteById(Long id);
}