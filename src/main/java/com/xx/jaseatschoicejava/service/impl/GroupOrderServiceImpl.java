package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderDish;
import com.xx.jaseatschoicejava.mapper.GroupOrderDishMapper;
import com.xx.jaseatschoicejava.mapper.GroupOrderMapper;
import com.xx.jaseatschoicejava.service.GroupOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群订单服务实现
 */
@Service
public class GroupOrderServiceImpl extends ServiceImpl<GroupOrderMapper, GroupOrder> implements GroupOrderService {

    @Autowired
    private GroupOrderDishMapper groupOrderDishMapper;

    /**
     * 创建群订单，使用事务确保数据一致性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createGroupOrder(GroupOrder groupOrder, List<GroupOrderDish> dishItems) {
        // 设置默认值
        groupOrder.setCreateTime(LocalDateTime.now());
        groupOrder.setUpdateTime(LocalDateTime.now());
        groupOrder.setStatus(0); // 默认待支付状态

        // 保存群订单
        boolean orderSaved = save(groupOrder);
        if (!orderSaved) {
            return false;
        }

        // 保存群订单菜品
        boolean dishesSaved = true;
        for (GroupOrderDish dish : dishItems) {
            dish.setGroupOrderId(groupOrder.getId());
            if (groupOrderDishMapper.insert(dish) <= 0) {
                dishesSaved = false;
                break;
            }
        }
        if (!dishesSaved) {
            // 事务会自动回滚
            return false;
        }

        return true;
    }

    @Override
    public List<GroupOrder> getGroupOrdersByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrder::getGroupId, groupId);
        return list(queryWrapper);
    }

    @Override
    public GroupOrder getGroupOrderDetail(Long groupOrderId) {
        return getById(groupOrderId);
    }

    @Override
    public List<GroupOrderDish> getGroupOrderDishes(Long groupOrderId) {
        LambdaQueryWrapper<GroupOrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupOrderDish::getGroupOrderId, groupOrderId);
        return groupOrderDishMapper.selectList(queryWrapper);
    }
}
