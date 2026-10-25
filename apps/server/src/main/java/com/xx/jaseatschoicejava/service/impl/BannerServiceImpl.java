package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Banner;
import com.xx.jaseatschoicejava.mapper.BannerMapper;
import com.xx.jaseatschoicejava.service.BannerService;
import org.springframework.stereotype.Service;

/**
 * 轮播图服务实现类
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
}
