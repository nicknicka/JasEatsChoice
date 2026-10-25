package com.xx.jaseatschoicejava.dto;

import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.util.IdPrefixUtil;

/**
 * DTO转换示例
 * 展示如何将数据库实体转换为前端需要的带前缀ID的DTO
 */
public class ExampleDTO {

    /**
     * 将User实体转换为带前缀ID的DTO
     * @param user 数据库查询得到的User实体
     * @return 带前缀ID的UserDTO
     */
    public static UserDTO convertUserToDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userDTO = UserDTO.fromUser(user);

        // 自动添加前缀（示例，实际根据需要调用）
        // userDTO.setUserId(IdPrefixUtil.addUserPrefix(user.getUserId()));

        return userDTO;
    }

    /**
     * 将Merchant实体转换为带前缀ID的DTO
     * @param merchant 数据库查询得到的Merchant实体
     * @return 带前缀ID的MerchantDTO
     */
    public static MerchantDTO convertMerchantToDTO(Merchant merchant) {
        if (merchant == null) {
            return null;
        }

        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setId(IdPrefixUtil.addMerchantPrefix(merchant.getId()));
        merchantDTO.setName(merchant.getName());
        merchantDTO.setAddress(merchant.getAddress());
        merchantDTO.setPhone(merchant.getPhone());
        merchantDTO.setStatus(merchant.getStatus());
        // 其他字段...

        return merchantDTO;
    }

    // 其他实体的转换方法...
}
