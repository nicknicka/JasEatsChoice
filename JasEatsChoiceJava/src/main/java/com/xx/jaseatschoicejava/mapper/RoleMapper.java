package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色ID查询权限ID列表
     */
    @Select("SELECT permission_id FROM t_role_permission_relation WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据管理员ID查询角色列表
     */
    @Select("SELECT r.* FROM t_role r " +
            "INNER JOIN t_admin_role_relation arr ON r.role_id = arr.role_id " +
            "WHERE arr.admin_id = #{adminId} AND r.status = 'ACTIVE' " +
            "ORDER BY r.sort_order")
    List<Role> selectRolesByAdminId(@Param("adminId") Long adminId);

    /**
     * 根据角色ID查询权限数量
     */
    @Select("SELECT COUNT(*) FROM t_role_permission_relation WHERE role_id = #{roleId}")
    Integer countPermissionsByRoleId(@Param("roleId") Long roleId);
}
