package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Contact;
import com.xx.jaseatschoicejava.mapper.ContactMapper;
import com.xx.jaseatschoicejava.service.ContactService;
import org.springframework.stereotype.Service;

/**
 * 联系人关系Service实现类
 */
@Service
public class ContactServiceImpl extends ServiceImpl<ContactMapper, Contact> implements ContactService {
    // 继承ServiceImpl后已经包含了IService接口中定义的所有方法实现
    // 可以在这里添加自定义的业务方法实现
}
