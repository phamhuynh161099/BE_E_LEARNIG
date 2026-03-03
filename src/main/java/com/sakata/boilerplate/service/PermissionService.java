package com.sakata.boilerplate.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakata.boilerplate.mapper.primary.PermissionMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true, transactionManager = "primaryTransactionManager")
public class PermissionService {
    // MySQL (primary)
    private final PermissionMapper permissionMapper;

    public List<?> getAll() {
        var listPermision = permissionMapper.findAll();
        if (listPermision == null)
            return List.of();
        return listPermision;
    }
}
