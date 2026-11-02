package com.xx.jaseatschoicejava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 小红书登录态存储：按用户隔离，key 就是 userId。内容不进入数据库、Redis 或日志。
 * 登录态本体与“失效标记”分别用不同的 key 复用 FileSecretStore。
 */
@Component
public class XhsSessionStore {

    /** 失效标记的 key 前缀，避免与登录态本体（key=userId）的存储文件冲突。 */
    private static final String EXPIRED_KEY_PREFIX = "expired::";
    private static final String EXPIRED_VALUE = "expired";

    private final FileSecretStore store;

    public XhsSessionStore(@Value("${XHS_SESSION_DIR:/app/runtime/secrets/xhs}") String directory) {
        this.store = FileSecretStore.directory(directory);
    }

    public String read(String userId) {
        return store.read(userId);
    }

    public void write(String userId, String cookie) {
        store.write(userId, cookie);
        // 重新绑定即视为新的登录态，清除此前的失效标记。
        store.delete(expiredKey(userId));
    }

    public void delete(String userId) {
        store.delete(userId);
        store.delete(expiredKey(userId));
    }

    public boolean exists(String userId) {
        return store.exists(userId);
    }

    public String updatedAt(String userId) {
        return store.updatedAt(userId);
    }

    /**
     * 标记该用户的登录态已失效。失效标记独立于登录态本体存储，不写入库、Redis 或日志。
     */
    public void markExpired(String userId) {
        store.write(expiredKey(userId), EXPIRED_VALUE);
    }

    /**
     * 该用户的登录态是否已被标记为失效。
     */
    public boolean isExpired(String userId) {
        return store.exists(expiredKey(userId));
    }

    private String expiredKey(String userId) {
        return EXPIRED_KEY_PREFIX + userId;
    }
}
