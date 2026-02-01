package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.AdminRoleRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 管理员-角色关联Mapper接口
 */
@Mapper
public interface AdminRoleRelationMapper extends BaseMapper<AdminRoleRelation> {

    /**
     * 删除管理员的所有角色
     */
    @Delete("DELETE FROM t_admin_role_relation WHERE admin_id = #{adminId}")
    boolean deleteByAdminId(@Param("adminId") Long adminId);

    /**
     * 删除角色的所有管理员关联
     */
    @Delete("DELETE FROM t_admin_role_relation WHERE role_id = #{roleId}")
    boolean deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据管理员ID查询角色ID列表
     */
    @Select("SELECT role_id FROM t_admin_role_relation WHERE admin_id = #{adminId}")
    List<Long> selectRoleIdsByAdminId(@Param("adminId") Long adminId);
}
