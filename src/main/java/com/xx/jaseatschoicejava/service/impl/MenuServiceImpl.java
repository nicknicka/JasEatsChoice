package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Menu;
import com.xx.jaseatschoicejava.mapper.MenuMapper;
import com.xx.jaseatschoicejava.service.MenuService;
import org.springframework.stereotype.Service;

/**
 * 菜单服务实现
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
}
