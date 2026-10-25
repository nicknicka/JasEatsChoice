package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.User;

/**
 * 用户Mapper接口
 */


@Mapper
public interface UserMapper extends BaseMapper<User> {
}
