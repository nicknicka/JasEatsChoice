package com.xx.jaseatschoicejava.dto;

import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.util.IdPrefixUtil;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商家数据传输对象，用于向前端传输商家信息
 */
@Data
public class MerchantDTO {
    private String id; // 商家ID，带M前缀
    private String name; // 商家名称
    private String address; // 商家地址
    private BigDecimal longitude; // 经度
    private BigDecimal latitude; // 纬度
    private String phone; // 联系电话
    private boolean status; // 营业状态：true-营业中，false-已关闭
    private String businessLicense; // 营业执照
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private String avatar; // 商家头像URL

    /**
     * 从Merchant实体转换为MerchantDTO，自动添加前缀
     * @param merchant 商家实体
     * @return MerchantDTO
     */
    public static MerchantDTO fromMerchant(Merchant merchant) {
        if (merchant == null) {
            return null;
        }

        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setId(IdPrefixUtil.addMerchantPrefix(merchant.getId()));
        merchantDTO.setName(merchant.getName());
        merchantDTO.setAddress(merchant.getAddress());
        merchantDTO.setLongitude(merchant.getLongitude());
        merchantDTO.setLatitude(merchant.getLatitude());
        merchantDTO.setPhone(merchant.getPhone());
        merchantDTO.setStatus(merchant.getStatus());
        merchantDTO.setBusinessLicense(merchant.getBusinessLicense());
        merchantDTO.setCreateTime(merchant.getCreateTime());
        merchantDTO.setUpdateTime(merchant.getUpdateTime());
        merchantDTO.setAvatar(merchant.getAvatar());

        return merchantDTO;
    }
}
