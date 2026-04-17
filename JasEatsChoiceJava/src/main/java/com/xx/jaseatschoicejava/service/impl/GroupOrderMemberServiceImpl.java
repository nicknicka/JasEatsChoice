package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.GroupOrderMember;
import com.xx.jaseatschoicejava.mapper.GroupOrderMemberMapper;
import com.xx.jaseatschoicejava.service.GroupOrderMemberService;
import org.springframework.stereotype.Service;

/**
 * 群订单成员关系服务实现
 */
@Service
public class GroupOrderMemberServiceImpl extends ServiceImpl<GroupOrderMemberMapper, GroupOrderMember>
        implements GroupOrderMemberService {
}
