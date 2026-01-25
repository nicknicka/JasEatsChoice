package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.ChatMsg;
import com.xx.jaseatschoicejava.entity.Group;
import com.xx.jaseatschoicejava.service.ChatMsgService;
import com.xx.jaseatschoicejava.service.GroupService;
import com.xx.jaseatschoicejava.util.ChatSessionIdGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群信息控制器
 */
@Api(tags = "群信息模块")
@RestController
@RequestMapping("/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final ChatMsgService chatMsgService;

    /**
     * 创建群
     */
    @ApiOperation("创建群")
    @PostMapping
    public ResponseResult<?> createGroup(@RequestBody Group group) {
        boolean success = groupService.save(group);
        if (success) {
            // ⭐ 生成并返回 sessionId（用于前端查找会话）
            String sessionId = ChatSessionIdGenerator.getGroupChatSessionId(group.getId());

            // 构建返回数据，包含群信息和 sessionId
            Map<String, Object> result = new HashMap<>();
            result.put("group", group);
            result.put("groupId", group.getId());
            result.put("sessionId", sessionId);

            return ResponseResult.success(result);
        } else {
            return ResponseResult.fail("500", "创建群失败");
        }
    }

    /**
     * 根据群ID或会话ID获取群信息
     */
    @ApiOperation("根据群ID或会话ID获取群信息")
    @GetMapping("/{groupIdOrSessionId}")
    public ResponseResult<?> getGroupById(@PathVariable String groupIdOrSessionId) {
        String groupId = groupIdOrSessionId;

        // 如果传入的是 sessionId（S开头），需要查找对应的 groupId
        if (groupIdOrSessionId != null && groupIdOrSessionId.startsWith("S")) {
            // 通过 sessionId 查询对应的 groupId
            LambdaQueryWrapper<ChatMsg> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ChatMsg::getSessionId, groupIdOrSessionId);
            queryWrapper.eq(ChatMsg::getSessionType, "group");
            queryWrapper.orderByDesc(ChatMsg::getCreateTime);
            queryWrapper.last("LIMIT 1");

            ChatMsg chatMsg = chatMsgService.getOne(queryWrapper);
            if (chatMsg != null && chatMsg.getToId() != null) {
                groupId = chatMsg.getToId();
            } else {
                return ResponseResult.fail("404", "找不到对应的群信息");
            }
        }

        Group group = groupService.getById(groupId);
        if (group != null) {
            return ResponseResult.success(group);
        } else {
            return ResponseResult.fail("404", "群不存在");
        }
    }

    /**
     * 更新群信息
     */
    @ApiOperation("更新群信息")
    @PutMapping("/{groupId}")
    public ResponseResult<?> updateGroup(@PathVariable String groupId, @RequestBody Group group) {
        group.setId(groupId);
        boolean success = groupService.updateById(group);
        if (success) {
            return ResponseResult.success(group);
        } else {
            return ResponseResult.fail("500", "更新群失败");
        }
    }

    /**
     * 删除群
     */
    @ApiOperation("删除群")
    @DeleteMapping("/{groupId}")
    public ResponseResult<?> deleteGroup(@PathVariable String groupId) {
        boolean success = groupService.removeById(groupId);
        if (success) {
            return ResponseResult.success("删除群成功");
        } else {
            return ResponseResult.fail("500", "删除群失败");
        }
    }

    /**
     * 获取我的所有群
     */
    @ApiOperation("获取我的所有群")
    @GetMapping("/my")
    public ResponseResult<?> getMyGroups(@RequestParam Long userId) {
        // 这里需要根据实际情况实现，获取该用户作为成员的所有群
        // 暂时返回所有群，需要修改为根据成员关系查询
        List<Group> groups = groupService.list();
        return ResponseResult.success(groups);
    }
}
