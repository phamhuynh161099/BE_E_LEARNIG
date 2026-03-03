package com.sakata.boilerplate.mapper.primary;

import com.sakata.boilerplate.dto.User.UpdateUserRequest;
import com.sakata.boilerplate.models.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * Thuộc về PRIMARY datasource (MySQL).
 * SQL phức tạp (JOIN roles + permissions) → UserMapper.xml
 * SQL đơn giản → annotation trực tiếp
 */
@Mapper
public interface UserMapper {

    // ── XML-mapped: trả về User đầy đủ với roles + permissions ──
    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    // ── Annotation: flat queries ──────────────────────────────────

    @Select("SELECT id, username, email, password, enabled, created_at, updated_at FROM users WHERE email = #{email}")
    @Results(id = "userFlatMap", value = {
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Optional<User> findByEmail(String email);

    @Select("SELECT COUNT(1) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    @Select("SELECT COUNT(1) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    @Insert("INSERT INTO users (username, email, password, enabled) VALUES (#{username}, #{email}, #{password}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(User user);

    @Update("UPDATE users SET username=#{username}, email=#{email}, password=#{password}, enabled=#{enabled} WHERE id=#{id}")
    int update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    void deleteUserRoles(Long userId);

    @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    // Trả về số lượng record đã được insert thành công
    int insertMultipleRoleForUser(String user_id, String[] roleIds);

    int updateUserWithPasswordCondition(@Param("user") UpdateUserRequest user,@Param("encodedPassword") String encodedPassword);
}
