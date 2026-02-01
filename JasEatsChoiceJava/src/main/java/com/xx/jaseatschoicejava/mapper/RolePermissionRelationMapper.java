package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.RolePermissionRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

/**
 * 角色-权限关联Mapper接口
 */
@Mapper
public interface RolePermissionRelationMapper extends BaseMapper<RolePermissionRelation> {

    /**
     * 删除角色的所有权限
     */
    @Delete("DELETE FROM t_role_permission_relation WHERE role_id = #{roleId}")
    boolean deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除权限的所有角色关联
     */
    @Delete("DELETE FROM t_role_permission_relation WHERE permission_id = #{permissionId}")
    boolean deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 批量插入角色权限关联
     */
    @Insert("<script>" +
            "INSERT INTO t_role_permission_relation (role_id, permission_id, create_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.roleId}, #{item.permissionId}, NOW())" +
            "</foreach>" +
            "</script>")
    boolean batchInsert(@Param("list") java.util.List<RolePermissionRelation> list);
}
