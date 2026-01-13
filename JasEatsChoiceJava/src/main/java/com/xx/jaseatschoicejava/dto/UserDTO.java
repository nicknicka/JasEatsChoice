package com.xx.jaseatschoicejava.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.xx.jaseatschoicejava.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象，用于向前端传输用户信息，隐藏敏感字段
 */
@Data
public class UserDTO {
    private String userId;
    private String phone;
    private String nickname;
    private Double height;
    private Double weight;
    private String dietGoal;
    private JsonNode allergies;
    private JsonNode preferTags;
    private String email;
    private Boolean disableWeatherRecommend;
    private String merchantId; // 商家ID，如果不为空表示用户已注册为商家
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 支付密码相关
    private Boolean hasPaymentPassword; // 是否已设置支付密码

    // 钱包信息
    private WalletDTO wallet;

    /**
     * 从User实体转换为UserDTO
     * @param user 用户实体
     * @return UserDTO
     */
    public static UserDTO fromUser(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setPhone(user.getPhone());
        userDTO.setNickname(user.getNickname());
        userDTO.setHeight(user.getHeight());
        userDTO.setWeight(user.getWeight());
        userDTO.setDietGoal(user.getDietGoal());
        userDTO.setAllergies(user.getAllergies());
        userDTO.setPreferTags(user.getPreferTags());
        userDTO.setEmail(user.getEmail());
        userDTO.setDisableWeatherRecommend(user.getDisableWeatherRecommend());
        userDTO.setMerchantId(user.getMerchantId());
        userDTO.setAvatar(user.getAvatar());
        userDTO.setCreateTime(user.getCreateTime());
        userDTO.setUpdateTime(user.getUpdateTime());
        userDTO.setHasPaymentPassword(user.getHasPaymentPassword());
        // 敏感信息如password、paymentPassword已自动隐藏，不进行设置
        return userDTO;
    }
}
