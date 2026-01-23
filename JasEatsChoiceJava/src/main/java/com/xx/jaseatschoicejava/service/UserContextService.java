package com.xx.jaseatschoicejava.service;

/**
 * 用户上下文服务接口
 * 用于构建AI聊天时需要的用户个性化信息上下文
 */
public interface UserContextService {

    /**
     * 构建用户上下文字符串用于AI提示词
     * @param userId 用户ID
     * @param enablePersonalData 是否启用个人数据
     * @return 上下文字符串，如果未启用或用户数据缺失则返回空字符串
     */
    String buildUserContext(String userId, boolean enablePersonalData);
}
