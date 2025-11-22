package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.MenuDish;
import com.xx.jaseatschoicejava.mapper.MenuDishMapper;
import com.xx.jaseatschoicejava.service.MenuDishService;
import org.springframework.stereotype.Service;

/**
 * 菜单菜品关联服务实现
 */
@Service
public class MenuDishServiceImpl extends ServiceImpl<MenuDishMapper, MenuDish> implements MenuDishService {
}
