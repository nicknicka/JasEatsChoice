package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.xx.jaseatschoicejava.entity.WithdrawRecord;
import com.xx.jaseatschoicejava.mapper.WithdrawRecordMapper;
import com.xx.jaseatschoicejava.service.WalletService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawRecordServiceImplTest {

    @Mock
    private WithdrawRecordMapper withdrawRecordMapper;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WithdrawRecordServiceImpl withdrawRecordService;

    @BeforeEach
    void setUpMybatisPlus() {
        // 生产代码内部构造 LambdaUpdateWrapper，方法引用解析依赖 TableInfo 缓存；
        // baseMapper 平时由 MyBatis-Plus 容器注入，纯单测需手动补齐。
        TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(new MybatisConfiguration(), ""), WithdrawRecord.class);
        ReflectionTestUtils.setField(withdrawRecordService, "baseMapper", withdrawRecordMapper);
    }

    @Test
    void reject_withSuccessfulRefund_shouldUpdateStatusAndRefund() {
        WithdrawRecord record = pendingRecord();
        when(withdrawRecordMapper.selectById("withdraw-1")).thenReturn(record);
        when(withdrawRecordMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(walletService.refund("user-1", new BigDecimal("88.00"), "提现驳回退款 - WTH-1"))
            .thenReturn(true);

        boolean result = withdrawRecordService.processWithdraw("withdraw-1", "REJECT", "资料不完整", 9L);

        assertTrue(result);
        verify(walletService).refund("user-1", new BigDecimal("88.00"), "提现驳回退款 - WTH-1");
    }

    @Test
    void reject_whenRefundFails_shouldThrowAndNotReportSuccess() {
        WithdrawRecord record = pendingRecord();
        when(withdrawRecordMapper.selectById("withdraw-1")).thenReturn(record);
        when(withdrawRecordMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(walletService.refund("user-1", new BigDecimal("88.00"), "提现驳回退款 - WTH-1"))
            .thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> withdrawRecordService.processWithdraw("withdraw-1", "REJECT", "资料不完整", 9L));
    }

    @Test
    void reject_whenStatusUpdateLosesRace_shouldThrowBeforeRefund() {
        WithdrawRecord record = pendingRecord();
        when(withdrawRecordMapper.selectById("withdraw-1")).thenReturn(record);
        when(withdrawRecordMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(0);

        assertThrows(IllegalStateException.class,
            () -> withdrawRecordService.processWithdraw("withdraw-1", "REJECT", "资料不完整", 9L));
        verify(walletService, never()).refund(any(), any(), any());
    }

    private WithdrawRecord pendingRecord() {
        WithdrawRecord record = new WithdrawRecord();
        record.setId("withdraw-1");
        record.setWithdrawNo("WTH-1");
        record.setUserId("user-1");
        record.setAmount(new BigDecimal("88.00"));
        record.setWithdrawStatus("pending");
        return record;
    }
}
