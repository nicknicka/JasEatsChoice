package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Group;

/**
 * 群信息Service接口
 */
public interface GroupService extends IService<Group> {
    // 继承IService后已经包含了丰富的CRUD方法，如save、remove、update、page等
    // 可以在这里添加自定义的业务方法
}
