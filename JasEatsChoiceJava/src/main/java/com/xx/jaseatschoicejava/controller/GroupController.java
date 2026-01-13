package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Group;
import com.xx.jaseatschoicejava.service.GroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群信息控制器
 */
@Api(tags = "群信息模块")
@RestController
@RequestMapping("/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 创建群
     */
    @ApiOperation("创建群")
    @PostMapping
    public ResponseResult<?> createGroup(@RequestBody Group group) {
        boolean success = groupService.save(group);
        if (success) {
            return ResponseResult.success(group);
        } else {
            return ResponseResult.fail("500", "创建群失败");
        }
    }

    /**
     * 根据群ID获取群信息
     */
    @ApiOperation("根据群ID获取群信息")
    @GetMapping("/{groupId}")
    public ResponseResult<?> getGroupById(@PathVariable String groupId) {
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
