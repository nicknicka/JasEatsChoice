package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Group;
import com.xx.jaseatschoicejava.mapper.GroupMapper;
import com.xx.jaseatschoicejava.service.GroupService;
import com.xx.jaseatschoicejava.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 群信息Service实现类
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    /**
     * 创建新群并生成群ID
     * @param group 包含群信息的实体（群名称、创建者ID等）
     * @return 创建成功返回true，否则返回false
     */
    @Override
    public boolean save(Group group) {
        // 生成群ID
        String groupId = IdGenerator.toGroupIdString(IdGenerator.generateId());
        group.setId(groupId);

        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        group.setCreateTime(now);
        group.setUpdateTime(now);

        // 调用父类的save方法
        return super.save(group);
    }
}
