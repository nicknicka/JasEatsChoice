package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.migration.SessionIdMigration;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 迁移管理控制器
 *
 * 提供会话ID迁移的API接口
 *
 * @author xx
 * @date 2026-01-20
 */
@Api(tags = "数据迁移管理")
@RestController
@RequestMapping("/v1/admin/migration")
public class MigrationController {

    @Autowired
    private SessionIdMigration sessionIdMigration;

    /**
     * 迁移会话ID
     *
     * @return 迁移结果
     */
    @ApiOperation("迁移会话ID（将旧的MD5格式迁移为新的IdGenerator格式）")
    @PostMapping("/session-id")
    public ResponseResult<?> migrateSessionIds() {
        try {
            int totalCount = sessionIdMigration.migrateAll();
            return ResponseResult.success("成功迁移 " + totalCount + " 条记录");
        } catch (Exception e) {
            return ResponseResult.fail("500", "迁移失败: " + e.getMessage());
        }
    }

    /**
     * 仅迁移消息表的session_id
     *
     * @return 迁移结果
     */
    @ApiOperation("迁移消息表的session_id")
    @PostMapping("/session-id/messages")
    public ResponseResult<?> migrateMessageSessionIds() {
        try {
            int count = sessionIdMigration.migrateChatMsgSessionIds();
            return ResponseResult.success("成功迁移 " + count + " 条消息记录");
        } catch (Exception e) {
            return ResponseResult.fail("500", "迁移失败: " + e.getMessage());
        }
    }

    /**
     * 仅迁移会话表的session_id
     *
     * @return 迁移结果
     */
    @ApiOperation("迁移会话表的session_id")
    @PostMapping("/session-id/sessions")
    public ResponseResult<?> migrateSessionTable() {
        try {
            int count = sessionIdMigration.migrateChatSessionIds();
            return ResponseResult.success("成功迁移 " + count + " 条会话记录");
        } catch (Exception e) {
            return ResponseResult.fail("500", "迁移失败: " + e.getMessage());
        }
    }

    /**
     * 验证迁移结果
     *
     * @return 验证结果
     */
    @ApiOperation("验证会话ID迁移结果")
    @GetMapping("/session-id/validate")
    public ResponseResult<?> validateMigration() {
        try {
            sessionIdMigration.validateMigration();
            return ResponseResult.success("验证完成，请查看服务器日志");
        } catch (Exception e) {
            return ResponseResult.fail("500", "验证失败: " + e.getMessage());
        }
    }
}
