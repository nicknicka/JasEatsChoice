package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Permission;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查询权限编码列表
     * @param roleId 角色ID
     * @return 权限编码列表
     */
    @Select("SELECT p.permission_code FROM t_permission p " +
            "INNER JOIN t_role_permission rp ON p.permission_id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 'ACTIVE'")
    List<String> selectPermissionCodesByRoleId(@Param("roleId") Long roleId);
}
