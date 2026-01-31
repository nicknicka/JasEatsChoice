package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Admin;
import com.xx.jaseatschoicejava.entity.AdminOperationLog;
import com.xx.jaseatschoicejava.mapper.AdminMapper;
import com.xx.jaseatschoicejava.service.AdminService;
import com.xx.jaseatschoicejava.service.AdminOperationLogService;
import com.xx.jaseatschoicejava.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理员服务实现
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AdminOperationLogService operationLogService;

    @Override
    public String login(String username, String password) {
        Admin admin = baseMapper.selectByUsernameWithRole(username);

        // 检查管理员是否存在且状态正常
        if (admin == null || !"ACTIVE".equals(admin.getStatus())) {
            return null;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return null;
        }

        // 更新最后登录时间和IP（从请求上下文获取）
        admin.setLastLoginTime(LocalDateTime.now());
        updateById(admin);

        // 生成JWT令牌（使用管理员ID和用户名）
        return jwtUtil.generateToken(String.valueOf(admin.getAdminId()), admin.getUsername());
    }

    @Override
    public Admin getByUsernameWithRole(String username) {
        return baseMapper.selectByUsernameWithRole(username);
    }

    @Override
    public IPage<Admin> getAdminPage(Page<Admin> page, String username, String status, Long roleId) {
        return baseMapper.selectAdminPageWithRole(page, username, status, roleId);
    }

    @Override
    @Transactional
    public boolean createAdmin(Admin admin, Long operatorId) {
        // 加密密码
        String encryptedPassword = passwordEncoder.encode(admin.getPassword());
        admin.setPassword(encryptedPassword);
        admin.setCreateBy(operatorId);
        admin.setStatus("ACTIVE");

        boolean result = save(admin);

        // 记录操作日志
        if (result && operatorId != null) {
            AdminOperationLog log = new AdminOperationLog();
            log.setAdminId(operatorId);
            log.setOperationType("CREATE");
            log.setModuleName("管理员管理");
            log.setOperationDesc("创建管理员: " + admin.getUsername());
            log.setStatus("SUCCESS");
            operationLogService.save(log);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long adminId, String status, Long operatorId) {
        Admin admin = new Admin();
        admin.setAdminId(adminId);
        admin.setStatus(status);
        admin.setUpdateBy(operatorId);

        boolean result = updateById(admin);

        // 记录操作日志
        if (result && operatorId != null) {
            AdminOperationLog log = new AdminOperationLog();
            log.setAdminId(operatorId);
            log.setOperationType("UPDATE");
            log.setModuleName("管理员管理");
            log.setOperationDesc("修改管理员状态: ID=" + adminId + ", 状态=" + status);
            log.setStatus("SUCCESS");
            operationLogService.save(log);
        }

        return result;
    }

    @Override
    @Transactional
    public boolean resetPassword(Long adminId, String newPassword, Long operatorId) {
        Admin admin = new Admin();
        admin.setAdminId(adminId);
        admin.setPassword(passwordEncoder.encode(newPassword));
        admin.setUpdateBy(operatorId);

        boolean result = updateById(admin);

        // 记录操作日志
        if (result && operatorId != null) {
            AdminOperationLog log = new AdminOperationLog();
            log.setAdminId(operatorId);
            log.setOperationType("UPDATE");
            log.setModuleName("管理员管理");
            log.setOperationDesc("重置管理员密码: ID=" + adminId);
            log.setStatus("SUCCESS");
            operationLogService.save(log);
        }

        return result;
    }
}
