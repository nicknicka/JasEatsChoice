package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 群成员关系Mapper接口
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
}
