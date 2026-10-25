package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Contact;

/**
 * 联系人关系Service接口
 */
public interface ContactService extends IService<Contact> {
    // 继承IService后已经包含了丰富的CRUD方法，如save、remove、update、page等
    // 可以在这里添加自定义的业务方法
}
