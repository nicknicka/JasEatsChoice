package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.entity.ConsumeHistory;
import com.xx.jaseatschoicejava.entity.Wallet;
import com.xx.jaseatschoicejava.mapper.WalletMapper;
import com.xx.jaseatschoicejava.service.ConsumeHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 提现扣款回归测试（issue #568）：
 * 申请提现即扣减余额，同一余额不能被多笔提现重复占用，驳回退款不会凭空加钱。
 */
class WalletServiceImplTest {

    private WalletMapper walletMapper;
    private ConsumeHistoryService consumeHistoryService;
    private WalletServiceImpl walletService;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletMapper = mock(WalletMapper.class);
        consumeHistoryService = mock(ConsumeHistoryService.class);
        walletService = new WalletServiceImpl(walletMapper, consumeHistoryService);

        wallet = new Wallet();
        wallet.setId("wallet-1");
        wallet.setUserId("user-1");
        wallet.setBalance(new BigDecimal("100.00"));
        wallet.setTotalRecharge(BigDecimal.ZERO);
        wallet.setTotalConsume(BigDecimal.ZERO);
        wallet.setTotalWithdraw(BigDecimal.ZERO);
        wallet.setVersion(0);
        wallet.setStatus("active");

        when(walletMapper.selectOne(any())).thenReturn(wallet);
        when(walletMapper.updateById(any(Wallet.class))).thenReturn(1);
        when(consumeHistoryService.save(any(ConsumeHistory.class))).thenReturn(true);
    }

    @Test
    @DisplayName("余额 100 申请 100 提现后余额归零，第二笔 100 元提现被拒绝")
    void withdraw_shouldDeductImmediatelyAndRejectSecondRequest() {
        assertTrue(walletService.withdraw("user-1", new BigDecimal("100.00"), "WTH-1"));
        assertEquals(0, wallet.getBalance().compareTo(BigDecimal.ZERO), "申请提现后余额应被扣减");
        assertEquals(0, wallet.getTotalWithdraw().compareTo(new BigDecimal("100.00")), "累计提现应同步增加");

        assertFalse(walletService.withdraw("user-1", new BigDecimal("100.00"), "WTH-2"),
            "余额已被第一笔占用，第二笔必须被拒绝");
        assertEquals(0, wallet.getBalance().compareTo(BigDecimal.ZERO), "被拒绝的申请不应改变余额");
        assertEquals(0, wallet.getTotalWithdraw().compareTo(new BigDecimal("100.00")), "被拒绝的申请不应累计提现");

        verify(walletMapper, times(1)).updateById(any(Wallet.class));

        ArgumentCaptor<ConsumeHistory> captor = ArgumentCaptor.forClass(ConsumeHistory.class);
        verify(consumeHistoryService, times(1)).save(captor.capture());
        assertEquals("withdraw", captor.getValue().getType(), "提现应写 withdraw 类型流水");
        assertEquals(0, captor.getValue().getAmount().compareTo(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("提现驳回退款后余额回到申请前，不会高于原值")
    void refund_shouldRestoreBalanceToPreviousValue() {
        BigDecimal before = wallet.getBalance();

        assertTrue(walletService.withdraw("user-1", new BigDecimal("30.00"), "WTH-3"));
        assertTrue(walletService.refund("user-1", new BigDecimal("30.00"), "提现驳回退款 - WTH-3"));

        assertEquals(0, wallet.getBalance().compareTo(before), "驳回退款后余额必须回到申请前");
        assertFalse(wallet.getBalance().compareTo(before) > 0, "驳回退款不得让余额高于原值");
    }

    @Test
    @DisplayName("提现金额非法时直接拒绝，不产生扣款")
    void withdraw_shouldRejectNonPositiveAmount() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> walletService.withdraw("user-1", BigDecimal.ZERO, "WTH-4"));
        verify(walletMapper, times(0)).updateById(any(Wallet.class));
    }
}
