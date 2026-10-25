package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Group;
import org.apache.ibatis.annotations.Mapper;

/**
 * 群信息Mapper接口
 */
@Mapper
public interface GroupMapper extends BaseMapper<Group> {
    // 继承BaseMapper后已经包含了基本的CRUD方法，如insert、selectById、updateById、deleteById等
    // 可以在这里添加自定义的查询方法
}
