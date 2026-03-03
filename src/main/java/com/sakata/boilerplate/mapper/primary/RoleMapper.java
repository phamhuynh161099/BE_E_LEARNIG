package com.sakata.boilerplate.mapper.primary;

import com.sakata.boilerplate.dto.Role.UpdateRoleRequest;
import com.sakata.boilerplate.models.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface RoleMapper {

    // XML-mapped (JOIN permissions)
    Optional<Role> findById(Long id);

    Optional<Role> findByName(String name);

    List<Role> findAll();

    List<Role> findByIds(Set<Long> ids); // <foreach> trong XML

    @Select("SELECT COUNT(1) > 0 FROM roles WHERE name = #{name}")
    boolean existsByName(String name);

    @Insert("INSERT INTO roles (name, description) VALUES (#{name}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Role role);

    @Update("UPDATE roles SET name=#{name}, description=#{description} WHERE id=#{roleId}")
    int update(UpdateRoleRequest role);

    @Delete("DELETE FROM roles WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId}")
    int deleteRolePermissions(Long roleId);

    @Insert("INSERT INTO role_permissions (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    void insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    // Trả về số lượng record đã được insert thành công
    int insertMultiplePermissionsForRole(
            @Param("roleId") String roleId,
            @Param("permissionIds") String[] permissionIds);
}
