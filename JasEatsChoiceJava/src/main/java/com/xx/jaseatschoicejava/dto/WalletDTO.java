package com.xx.jaseatschoicejava.dto;

import com.xx.jaseatschoicejava.entity.Wallet;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包数据传输对象
 */
@Data
public class WalletDTO {
    private String id;
    private String userId;
    private BigDecimal balance;
    private BigDecimal totalRecharge;
    private BigDecimal totalConsume;
    private BigDecimal totalWithdraw;
    private Integer version;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 从Wallet实体转换为WalletDTO
     */
    public static WalletDTO fromWallet(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        WalletDTO dto = new WalletDTO();
        dto.setId(wallet.getId());
        dto.setUserId(wallet.getUserId());
        dto.setBalance(wallet.getBalance());
        dto.setTotalRecharge(wallet.getTotalRecharge());
        dto.setTotalConsume(wallet.getTotalConsume());
        dto.setTotalWithdraw(wallet.getTotalWithdraw());
        dto.setVersion(wallet.getVersion());
        dto.setStatus(wallet.getStatus());
        dto.setCreateTime(wallet.getCreateTime());
        dto.setUpdateTime(wallet.getUpdateTime());
        return dto;
    }
}
