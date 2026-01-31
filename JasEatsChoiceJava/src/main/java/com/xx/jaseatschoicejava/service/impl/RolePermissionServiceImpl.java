package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.RolePermission;
import com.xx.jaseatschoicejava.mapper.RolePermissionMapper;
import com.xx.jaseatschoicejava.service.RolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 角色权限关联服务实现
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
