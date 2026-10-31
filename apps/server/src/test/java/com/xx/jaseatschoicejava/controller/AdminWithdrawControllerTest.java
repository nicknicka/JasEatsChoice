package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.xx.jaseatschoicejava.entity.WithdrawRecord;
import com.xx.jaseatschoicejava.service.WalletService;
import com.xx.jaseatschoicejava.service.WithdrawRecordService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminWithdrawControllerTest {

    @Mock
    private WithdrawRecordService withdrawRecordService;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private AdminWithdrawController controller;

    @BeforeAll
    static void initMybatisPlusTableInfo() {
        // controller 内部构造 LambdaUpdateWrapper，方法引用解析依赖 TableInfo 缓存
        TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(new MybatisConfiguration(), ""), WithdrawRecord.class);
    }

    @Test
    void fail_withSuccessfulRefund_shouldReturnSuccess() {
        WithdrawRecord record = processingRecord();
        when(withdrawRecordService.getById("withdraw-1")).thenReturn(record);
        when(withdrawRecordService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);
        when(walletService.refund("user-1", new BigDecimal("88.00"), "提现失败退款 - WTH-1"))
            .thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.failWithdraw(
            "withdraw-1", Map.of("reason", "渠道异常"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        verify(walletService).refund("user-1", new BigDecimal("88.00"), "提现失败退款 - WTH-1");
    }

    @Test
    void fail_whenRefundFails_shouldThrowAndNotReportSuccess() {
        WithdrawRecord record = processingRecord();
        when(withdrawRecordService.getById("withdraw-1")).thenReturn(record);
        when(withdrawRecordService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);
        when(walletService.refund("user-1", new BigDecimal("88.00"), "提现失败退款 - WTH-1"))
            .thenReturn(false);

        IllegalStateException error = org.junit.jupiter.api.Assertions.assertThrows(
            IllegalStateException.class,
            () -> controller.failWithdraw("withdraw-1", Map.of("reason", "渠道异常"))
        );

        assertTrue(error.getMessage().contains("退款失败"));
    }

    @Test
    void fail_whenStatusUpdateLosesRace_shouldThrowBeforeRefund() {
        WithdrawRecord record = processingRecord();
        when(withdrawRecordService.getById("withdraw-1")).thenReturn(record);
        when(withdrawRecordService.update(any(LambdaUpdateWrapper.class))).thenReturn(false);

        assertThrowsBeforeRefund();
    }

    private void assertThrowsBeforeRefund() {
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalStateException.class,
            () -> controller.failWithdraw("withdraw-1", Map.of("reason", "渠道异常"))
        );
        verify(walletService, never()).refund(any(), any(), any());
    }

    private WithdrawRecord processingRecord() {
        WithdrawRecord record = new WithdrawRecord();
        record.setId("withdraw-1");
        record.setWithdrawNo("WTH-1");
        record.setUserId("user-1");
        record.setAmount(new BigDecimal("88.00"));
        record.setWithdrawStatus("processing");
        return record;
    }
}
