package com.xx.jaseatschoicejava.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 防止「分页插件又丢了」的回归测试。
 *
 * <p>缺 PaginationInnerInterceptor 时，MyBatis-Plus 的 page() 不会改写下发 LIMIT、
 * 也不会执行 count，所有列表接口会 total=0 且一次返回全表（issue #433）。
 */
class MyBatisPlusConfigTest {

    @Test
    @DisplayName("分页插件与乐观锁插件都要注册，且分页在前")
    void registersPaginationAndOptimisticLocker() {
        MybatisPlusInterceptor interceptor = new MyBatisPlusConfig().mybatisPlusInterceptor();
        List<InnerInterceptor> inner = interceptor.getInterceptors();

        assertTrue(inner.stream().anyMatch(PaginationInnerInterceptor.class::isInstance),
                "缺少 PaginationInnerInterceptor：所有分页接口会返回 total=0 且一次吐全表");
        assertTrue(inner.stream().anyMatch(OptimisticLockerInnerInterceptor.class::isInstance),
                "乐观锁插件不应被分页插件挤掉");

        int pageIndex = indexOf(inner, PaginationInnerInterceptor.class);
        int lockIndex = indexOf(inner, OptimisticLockerInnerInterceptor.class);
        assertTrue(pageIndex < lockIndex, "官方建议顺序：分页 -> 乐观锁");
    }

    private static int indexOf(List<InnerInterceptor> list, Class<?> type) {
        for (int i = 0; i < list.size(); i++) {
            if (type.isInstance(list.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
