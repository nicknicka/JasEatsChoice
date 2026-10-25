package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Merchant;

/**
 * 商家服务接口
 */
public interface MerchantService extends IService<Merchant> {
    /**
     * 商家注册
     * @param merchant 商家信息
     * @return 是否成功
     */
    Merchant register(Merchant merchant);

}
