package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车项 Mapper。
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
