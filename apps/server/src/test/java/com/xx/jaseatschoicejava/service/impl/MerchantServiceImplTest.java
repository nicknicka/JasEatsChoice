package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.mapper.MerchantMapper;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {

    @Mock
    private MerchantMapper merchantMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private MerchantServiceImpl merchantService;

    @Test
    void register_whenUserAssociationFails_shouldThrowForRollback() {
        Merchant merchant = new Merchant();
        merchant.setName("测试商家");
        User user = new User();
        user.setUserId("user-1");

        when(merchantMapper.insert(any(Merchant.class))).thenReturn(1);
        when(userMapper.selectById("user-1")).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> merchantService.register(merchant, "user-1"));
        assertNotNull(merchant.getPassword());
    }

    @Test
    void register_whenWritesSucceed_shouldAssociateUser() {
        Merchant merchant = new Merchant();
        merchant.setName("测试商家");
        User user = new User();
        user.setUserId("user-1");

        when(merchantMapper.insert(any(Merchant.class))).thenReturn(1);
        when(userMapper.selectById("user-1")).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        Merchant saved = merchantService.register(merchant, "user-1");

        assertNotNull(saved.getId());
        assertNotNull(saved.getPassword());
        assertEquals(saved.getId(), user.getMerchantId());
    }
}
