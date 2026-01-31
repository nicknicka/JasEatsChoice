package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Admin;

/**
 * 管理员服务接口
 */
public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录
     * @param username 用户名
     * @param password 密码
     * @return JWT token if successful, null otherwise
     */
    String login(String username, String password);

    /**
     * 根据用户名查询管理员（包含角色信息）
     * @param username 用户名
     * @return 管理员对象
     */
    Admin getByUsernameWithRole(String username);

    /**
     * 分页查询管理员列表
     * @param page 分页对象
     * @param username 用户名（可选）
     * @param status 状态（可选）
     * @param roleId 角色ID（可选）
     * @return 分页结果
     */
    IPage<Admin> getAdminPage(Page<Admin> page, String username, String status, Long roleId);

    /**
     * 创建管理员
     * @param admin 管理员对象
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean createAdmin(Admin admin, Long operatorId);

    /**
     * 更新管理员状态
     * @param adminId 管理员ID
     * @param status 状态
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean updateStatus(Long adminId, String status, Long operatorId);

    /**
     * 重置管理员密码
     * @param adminId 管理员ID
     * @param newPassword 新密码
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean resetPassword(Long adminId, String newPassword, Long operatorId);
}
