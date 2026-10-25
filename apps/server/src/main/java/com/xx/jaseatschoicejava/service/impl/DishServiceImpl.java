package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.mapper.DishMapper;
import com.xx.jaseatschoicejava.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 菜品服务实现
 *
 * 缓存策略：
 * - 菜品详情：缓存30分钟
 * - 更新菜品：清除缓存
 * - 删除菜品：清除缓存
 */
@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    private static final String CACHE_NAME = "dish:detail";

    /**
     * 获取菜品详情（带缓存）
     *
     * @param id 菜品ID
     * @return 菜品详情
     */
    @Cacheable(value = CACHE_NAME, key = "#id", unless = "#result == null")
    public Dish getDishById(String id) {
        log.debug("从数据库查询菜品详情: dishId={}", id);
        return super.getById(id);
    }

    /**
     * 更新菜品并清除缓存
     *
     * @param dish 菜品信息
     * @return 是否成功
     */
    @CacheEvict(value = CACHE_NAME, key = "#dish.id")
    public boolean updateDish(Dish dish) {
        log.debug("更新菜品并清除缓存: dishId={}", dish.getId());
        return super.updateById(dish);
    }

    /**
     * 删除菜品并清除缓存
     *
     * @param id 菜品ID
     * @return 是否成功
     */
    @CacheEvict(value = CACHE_NAME, key = "#id")
    public boolean removeDishById(String id) {
        log.debug("删除菜品并清除缓存: dishId={}", id);
        return super.removeById(id);
    }
}
