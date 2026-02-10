package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户地址Mapper接口
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    /**
     * 查询用户的默认地址
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} AND is_default = true LIMIT 1")
    UserAddress findDefaultAddress(@Param("userId") String userId);

    /**
     * 查询用户的所有地址
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} ORDER BY is_default DESC, create_time DESC")
    java.util.List<UserAddress> findByUserId(@Param("userId") String userId);
}
