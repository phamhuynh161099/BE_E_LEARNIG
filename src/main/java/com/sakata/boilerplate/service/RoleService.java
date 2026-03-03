package com.sakata.boilerplate.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakata.boilerplate.dto.Role.AddRoleRequest;
import com.sakata.boilerplate.dto.Role.UpdateRoleRequest;
import com.sakata.boilerplate.mapper.primary.RoleMapper;
import com.sakata.boilerplate.models.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
// @Transactional(readOnly = true, transactionManager =
// "primaryTransactionManager")
@Transactional(transactionManager = "primaryTransactionManager")
public class RoleService {
    // MySQL (primary)
    private final RoleMapper roleMapper;

    public List<?> getAll() {
        var listRole = roleMapper.findAll();
        if (listRole == null)
            return List.of();
        return listRole;
    }

    public boolean addNewRole(AddRoleRequest param) {
        try {

            Role newRole = new Role();
            newRole.setName(param.name());
            newRole.setDescription(param.description());

            int rowsAffectedAdd = roleMapper.insert(newRole);
            Long generatedRoleId = newRole.getId();
            int rowsAffectedUpdatePermissionForRole = roleMapper.insertMultiplePermissionsForRole(generatedRoleId.toString(),
                    param.permissionIds());
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi insert dữ liệu: " + e.getMessage());
            // Log lỗi hoặc xử lý thêm tùy logic của bạn
            throw e; // Ném ra để Spring Rollback lại transaction
        }

    }

    public boolean updateRole(UpdateRoleRequest param) {
        try {

            int rowsAffectedDelete = roleMapper.deleteRolePermissions(Long.parseLong(param.roleId()));
            int rowsAffectedRole = roleMapper.update(param);
            int rowsAffectedUpdatePermissionForRole = roleMapper.insertMultiplePermissionsForRole(param.roleId(),
                    param.permissionIds());
            return true;
            // Gọi hàm và lấy số dòng bị ảnh hưởng
            // int rowsAffected =
            // rolePermissionMapper.insertMultiplePermissionsForRole(roleId, permissionIds);

            // Kiểm tra xem số dòng insert thành công có bằng kích thước của list truyền vào
            // không
            // if (rowsAffected == permissionIds.size()) {
            // System.out.println("Insert thành công toàn bộ " + rowsAffected + " quyền!");
            // return true;
            // } else {
            // System.out.println("Có lỗi: Chỉ insert được " + rowsAffected + "/" +
            // permissionIds.size());
            // return false;
            // }

        } catch (Exception e) {
            System.err.println("Lỗi khi insert dữ liệu: " + e.getMessage());
            // Log lỗi hoặc xử lý thêm tùy logic của bạn
            throw e; // Ném ra để Spring Rollback lại transaction
        }

    }
}
