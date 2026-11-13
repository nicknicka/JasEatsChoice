package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
        queryWrapper.orderByDesc("created_at");

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
            // 审核通过：资金已在申请时从余额扣减，这里只需推进状态
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
            LocalDateTime updateTime = LocalDateTime.now();
            boolean claimed = this.update(
                new LambdaUpdateWrapper<WithdrawRecord>()
                    .eq(WithdrawRecord::getId, withdrawId)
                    .eq(WithdrawRecord::getWithdrawStatus, "pending")
                    .set(WithdrawRecord::getWithdrawStatus, "rejected")
                    .set(WithdrawRecord::getAuditTime, record.getAuditTime())
                    .set(WithdrawRecord::getAuditUser, record.getAuditUser())
                    .set(WithdrawRecord::getRejectReason, comment)
                    .set(WithdrawRecord::getRemark, comment)
                    .set(WithdrawRecord::getCompleteTime, updateTime)
                    .set(WithdrawRecord::getUpdatedAt, updateTime)
            );

            if (!claimed) {
                throw new IllegalStateException("提现记录更新失败");
            }

            record.setWithdrawStatus("rejected");
            record.setRejectReason(comment);
            record.setCompleteTime(updateTime);

            // 通知用户提现被驳回
            NotificationUtil.createWithdrawNotification(
                record.getUserId(),
                NotificationTypeEnum.WITHDRAW_REJECTED,
                record.getAmount().toString(),
                comment != null ? comment : "审核未通过"
            );

            if (walletService == null) {
                throw new IllegalStateException("钱包服务不可用，无法退款");
            }

            try {
                boolean refunded = walletService.refund(
                    record.getUserId(),
                    record.getAmount(),
                    "提现驳回退款 - " + record.getWithdrawNo()
                );
                if (!refunded) {
                    throw new IllegalStateException("退款到钱包失败");
                }
            } catch (Exception e) {
                log.error("退款到钱包失败: {}", withdrawId, e);
                throw new IllegalStateException("退款失败：" + e.getMessage(), e);
            }

            log.info("提现审核拒绝: {}, 原因: {}", withdrawId, comment);

        } else {
            log.warn("无效的审核决定: {}", decision);
            return false;
        }

        if ("APPROVE".equalsIgnoreCase(decision)) {
            record.setRemark(comment);
            boolean success = this.updateById(record);

            if (!success) {
                throw new IllegalStateException("提现记录更新失败");
            }
        }

        log.info("提现审核处理成功: {}, 决定: {}", withdrawId, decision);
        return true;
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
        // 申请即扣减余额，保证同一余额不能被多笔提现重复占用；记录保存失败时随事务回滚
        if (walletService == null) {
            throw new IllegalStateException("钱包服务不可用，无法办理提现");
        }
        if (!walletService.withdraw(userId, amount, withdrawNo)) {
            throw new IllegalArgumentException("余额不足");
        }

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
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        boolean success = this.save(record);
        if (success) {
            log.info("创建提现申请成功: 用户ID={}, 金额={}, 流水号={}", userId, amount, withdrawNo);
            return record;
        } else {
            log.error("创建提现申请失败: 用户ID={}, 金额={}, 流水号={}", userId, amount, withdrawNo);
            throw new RuntimeException("创建提现申请失败");
        }
    }
}
