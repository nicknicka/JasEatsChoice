package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.MerchantMapper;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 商家服务实现
 *
 * 缓存策略：
 * - 商家详情：缓存1小时
 * - 更新商家信息：清除缓存
 */
@Slf4j
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    private static final String CACHE_NAME = "merchant:detail";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Merchant register(Merchant merchant, String userId) {
        // 生成商家ID
        String merchantId = IdGenerator.toMerchantIdString(IdGenerator.generateId());
        merchant.setId(merchantId);
        // 默认新注册商家为营业状态
        merchant.setStatus(true);
        // 商家登录走用户账号；该字段为数据库遗留必填，写入随机密码避免空值。
        merchant.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        // 设置创建时间
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        // 地址、经纬度等字段可以为空，无需默认值
        // 数据库已修改为允许这些字段为空
        if (!save(merchant)) {
            throw new IllegalStateException("商家注册失败");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        user.setMerchantId(merchant.getId());
        if (userMapper.updateById(user) != 1) {
            throw new IllegalStateException("用户与商家关联失败");
        }

        return merchant;
    }

    /**
     * 获取商家详情（带缓存）
     *
     * @param merchantId 商家ID
     * @return 商家详情
     */
    @Cacheable(value = CACHE_NAME, key = "#merchantId", unless = "#result == null")
    public Merchant getMerchantById(String merchantId) {
        log.debug("从数据库查询商家详情: merchantId={}", merchantId);
        return getById(merchantId);
    }

    /**
     * 更新商家信息并清除缓存
     *
     * @param merchant 商家信息
     * @return 是否成功
     */
    @CacheEvict(value = CACHE_NAME, key = "#merchant.id")
    public boolean updateMerchantInfo(Merchant merchant) {
        log.debug("更新商家信息并清除缓存: merchantId={}", merchant.getId());
        merchant.setUpdatedAt(LocalDateTime.now());
        return updateById(merchant);
    }

    /**
     * 清除商家缓存
     *
     * @param merchantId 商家ID
     */
    @CacheEvict(value = CACHE_NAME, key = "#merchantId")
    public void evictMerchantCache(String merchantId) {
        log.debug("清除商家缓存: merchantId={}", merchantId);
    }
}
