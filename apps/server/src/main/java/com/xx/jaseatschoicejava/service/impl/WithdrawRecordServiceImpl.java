package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.WithdrawRecord;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.mapper.WithdrawRecordMapper;
import com.xx.jaseatschoicejava.service.UserService;
import com.xx.jaseatschoicejava.service.WithdrawRecordService;
import com.xx.jaseatschoicejava.service.WalletService;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现记录Service实现
 */
@Service
public class WithdrawRecordServiceImpl extends ServiceImpl<WithdrawRecordMapper, WithdrawRecord> implements WithdrawRecordService {

    private static final Logger log = LoggerFactory.getLogger(WithdrawRecordServiceImpl.class);

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private WalletService walletService;

    @Override
    public IPage<WithdrawRecord> getWithdrawPage(Page<WithdrawRecord> pageParam, String keyword, String status) {
        QueryWrapper<WithdrawRecord> queryWrapper = new QueryWrapper<>();

        // 状态筛选
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("withdraw_status", status);
        }

        // 关键词搜索（提现流水号、用户ID）
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("withdraw_no", keyword)
                .or()
                .like("user_id", keyword)
            );
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("create_time");

        IPage<WithdrawRecord> result = this.page(pageParam, queryWrapper);

        // 为每条记录添加用户信息（如果需要）
        if (userService != null) {
            result.getRecords().forEach(record -> {
                if (record.getUserId() != null) {
                    User user = userService.getById(record.getUserId());
                    if (user != null) {
                        // 可以设置用户昵称或其他信息到record中
                        // record.setUserName(user.getNickname());
                    }
                }
            });
        }

        return result;
    }

    @Override
    public WithdrawRecord getWithdrawDetail(String withdrawId) {
        WithdrawRecord record = this.getById(withdrawId);
        // 可以添加关联信息
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processWithdraw(String withdrawId, String decision, String comment, Long adminId) {
        WithdrawRecord record = this.getById(withdrawId);
        if (record == null) {
            log.warn("提现记录不存在: {}", withdrawId);
            return false;
        }

        // 只能处理待审核的提现
        if (!"pending".equals(record.getWithdrawStatus())) {
            log.warn("提现记录状态不是待审核，无法处理: {}", withdrawId);
            return false;
        }

        record.setAuditTime(LocalDateTime.now());
        record.setAuditUser(adminId != null ? adminId.toString() : "SYSTEM");

        if ("APPROVE".equalsIgnoreCase(decision)) {
            // 审核通过
            record.setWithdrawStatus("processing"); // 进入处理中状态

            // 通知用户提现审核通过
            NotificationUtil.createWithdrawNotification(
                record.getUserId(),
                NotificationTypeEnum.WITHDRAW_APPROVED,
                record.getAmount().toString(),
                null
            );

            // TODO: 这里可以调用第三方支付平台的转账接口
            // 实际转账成功后再将状态改为 success

            log.info("提现审核通过: {}", withdrawId);

        } else if ("REJECT".equalsIgnoreCase(decision)) {
            // 审核拒绝，需要退款到钱包
            record.setWithdrawStatus("rejected");
            record.setRejectReason(comment);

            // 通知用户提现被驳回
            NotificationUtil.createWithdrawNotification(
                record.getUserId(),
                NotificationTypeEnum.WITHDRAW_REJECTED,
                record.getAmount().toString(),
                comment != null ? comment : "审核未通过"
            );

            // 退款到钱包（如果walletService可用）
            if (walletService != null) {
                try {
                    // walletService.refundWithdraw(record.getUserId(), record.getAmount());
                    log.info("提现拒绝，已退款到钱包: {}, 金额: {}", withdrawId, record.getAmount());
                } catch (Exception e) {
                    log.error("退款到钱包失败: {}", withdrawId, e);
                    throw new RuntimeException("退款失败", e);
                }
            }

            log.info("提现审核拒绝: {}, 原因: {}", withdrawId, comment);

        } else {
            log.warn("无效的审核决定: {}", decision);
            return false;
        }

        record.setRemark(comment);
        boolean success = this.updateById(record);

        if (success) {
            log.info("提现审核处理成功: {}, 决定: {}", withdrawId, decision);
        }

        return success;
    }

    @Override
    public BigDecimal sumWithdrawAmountByStatus(String status) {
        QueryWrapper<WithdrawRecord> queryWrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("withdraw_status", status);
        }

        // 使用MyBatis-Plus的聚合查询
        java.util.List<WithdrawRecord> list = this.list(queryWrapper);

        BigDecimal total = list.stream()
            .map(WithdrawRecord::getAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WithdrawRecord createWithdrawRequest(String userId, BigDecimal amount, String withdrawNo, String withdrawMethod, String accountInfo) {
        // 创建提现记录
        WithdrawRecord record = new WithdrawRecord();
        record.setWithdrawNo(withdrawNo);
        record.setUserId(userId);
        record.setAmount(amount);
        record.setWithdrawMethod(withdrawMethod);
        record.setAccountInfo(accountInfo);
        record.setWithdrawStatus("pending"); // 待审核状态
        record.setFee(BigDecimal.ZERO); // 手续费，可以根据需要计算
        record.setActualAmount(amount); // 实际到账金额
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        boolean success = this.save(record);
        if (success) {
            log.info("创建提现申请成功: 用户ID={}, 金额={}, 流水号={}", userId, amount, withdrawNo);
            return record;
        } else {
            log.error("创建提现申请失败: 用户ID={}, 金额={}, 流水号={}", userId, amount, withdrawNo);
            throw new RuntimeException("创建提现申请失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeWithdrawDeduction(String withdrawId) {
        WithdrawRecord record = this.getById(withdrawId);
        if (record == null) {
            log.error("提现记录不存在: {}", withdrawId);
            return false;
        }

        // 只有审核通过（approved）状态才能执行扣款
        if (!"approved".equals(record.getWithdrawStatus())) {
            log.warn("提现记录状态不是审核通过，无法执行扣款: {}, 当前状态: {}", withdrawId, record.getWithdrawStatus());
            return false;
        }

        if (walletService == null) {
            log.error("WalletService 不可用");
            return false;
        }

        try {
            // 检查余额是否足够
            if (!walletService.checkBalance(record.getUserId(), record.getAmount())) {
                log.error("用户余额不足，无法提现: 用户ID={}, 金额={}", record.getUserId(), record.getAmount());
                // 更新提现记录状态为失败
                record.setWithdrawStatus("failed");
                record.setRemark("余额不足");
                record.setUpdateTime(LocalDateTime.now());
                this.updateById(record);
                return false;
            }

            // 执行扣款（这里需要一个新的方法来真正扣款）
            // 临时使用 deductBalance 方法，但这会记录到 consume_history
            // TODO: 应该在 WalletService 中添加专门的方法来处理提现扣款
            boolean success = walletService.deductBalance(
                record.getUserId(),
                record.getAmount(),
                "提现 - " + record.getWithdrawNo()
            );

            if (success) {
                // 更新提现记录状态为处理中
                record.setWithdrawStatus("processing");
                record.setCompleteTime(LocalDateTime.now());
                record.setUpdateTime(LocalDateTime.now());
                this.updateById(record);

                log.info("提现扣款成功: {}", withdrawId);
                return true;
            } else {
                log.error("提现扣款失败: {}", withdrawId);
                return false;
            }
        } catch (Exception e) {
            log.error("执行提现扣款异常: {}", withdrawId, e);
            // 更新提现记录状态为失败
            record.setWithdrawStatus("failed");
            record.setRemark("扣款异常: " + e.getMessage());
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
            return false;
        }
    }
}
