package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Menu;

/**
 * 菜单Mapper接口
 */


@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
}
