package com.xx.jaseatschoicejava.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Merchant;

/**
 * 商家Mapper接口
 */


@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
}
