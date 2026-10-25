package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.UserCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper接口
 */
@Mapper
public interface CollectionMapper extends BaseMapper<UserCollection> {
}
