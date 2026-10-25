package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Admin;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据用户名查询管理员（包含角色信息）
     */
    @Select("SELECT a.*, r.role_name, r.role_code FROM t_admin a " +
            "LEFT JOIN t_role r ON a.role_id = r.role_id " +
            "WHERE a.username = #{username} AND a.status != 'DELETED'")
    Admin selectByUsernameWithRole(@Param("username") String username);

    /**
     * 分页查询管理员列表（包含角色信息）
     */
    @Select("<script>" +
            "SELECT a.*, r.role_name, r.role_code FROM t_admin a " +
            "LEFT JOIN t_role r ON a.role_id = r.role_id " +
            "WHERE a.status != 'DELETED' " +
            "<if test='username != null and username != \"\"'>" +
            "AND a.username LIKE CONCAT('%', #{username}, '%') " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND a.status = #{status} " +
            "</if>" +
            "<if test='roleId != null'>" +
            "AND a.role_id = #{roleId} " +
            "</if>" +
            "ORDER BY a.create_time DESC" +
            "</script>")
    IPage<Admin> selectAdminPageWithRole(Page<Admin> page,
                                         @Param("username") String username,
                                         @Param("status") String status,
                                         @Param("roleId") Long roleId);
}
