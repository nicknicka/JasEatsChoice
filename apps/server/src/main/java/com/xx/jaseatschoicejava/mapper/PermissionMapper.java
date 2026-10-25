package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查询权限编码列表
     */
    @Select("SELECT p.permission_code FROM t_permission p " +
            "INNER JOIN t_role_permission_relation rp ON p.permission_id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 'ACTIVE'")
    List<String> selectPermissionCodesByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID查询权限列表
     */
    @Select("SELECT p.* FROM t_permission p " +
            "INNER JOIN t_role_permission_relation rp ON p.permission_id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 'ACTIVE' " +
            "ORDER BY p.sort_order")
    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询所有顶级权限（parentId为0或null）
     */
    @Select("SELECT * FROM t_permission WHERE (parent_id = 0 OR parent_id IS NULL) AND status = 'ACTIVE' ORDER BY sort_order")
    List<Permission> selectTopLevelPermissions();

    /**
     * 根据父级ID查询子权限列表
     */
    @Select("SELECT * FROM t_permission WHERE parent_id = #{parentId} AND status = 'ACTIVE' ORDER BY sort_order")
    List<Permission> selectByParentId(@Param("parentId") Long parentId);
}
