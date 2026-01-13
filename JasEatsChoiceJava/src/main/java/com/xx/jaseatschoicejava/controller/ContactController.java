package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Contact;
import com.xx.jaseatschoicejava.service.ContactService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 联系人关系控制器
 */
@Api(tags = "联系人关系模块")
@RestController
@RequestMapping("/v1/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    /**
     * 获取我的好友列表
     */
    @ApiOperation("获取我的好友列表")
    @GetMapping("/friends")
    public ResponseResult<?> getMyFriends(@RequestParam String userId) {
        List<Contact> friends = contactService.lambdaQuery()
                .eq(Contact::getUserId, userId)
                .eq(Contact::getRelationType, "friend")
                .eq(Contact::getStatus, "normal")
                .list();
        return ResponseResult.success(friends);
    }

    /**
     * 获取群成员列表
     */
    @ApiOperation("获取群成员列表")
    @GetMapping("/groups/{groupId}/members")
    public ResponseResult<?> getGroupMembers(@PathVariable Long groupId) {
        List<Contact> members = contactService.lambdaQuery()
                .eq(Contact::getTargetId, groupId)
                .eq(Contact::getRelationType, "group")
                .list();
        return ResponseResult.success(members);
    }

    /**
     * 加入群
     */
    @ApiOperation("加入群")
    @PostMapping("/groups/join")
    public ResponseResult<?> joinGroup(@RequestBody Contact contact) {
        contact.setRelationType("group");
        contact.setStatus("normal");
        boolean success = contactService.save(contact);
        if (success) {
            return ResponseResult.success("加入群成功");
        } else {
            return ResponseResult.fail("500", "加入群失败");
        }
    }

    /**
     * 退出群
     */
    @ApiOperation("退出群")
    @DeleteMapping("/groups/leave")
    public ResponseResult<?> leaveGroup(@RequestParam String userId, @RequestParam String groupId) {
        boolean success = contactService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Contact>()
                .eq(Contact::getUserId, userId)
                .eq(Contact::getTargetId, groupId)
                .eq(Contact::getRelationType, "group"));
        if (success) {
            return ResponseResult.success("退出群成功");
        } else {
            return ResponseResult.fail("500", "退出群失败");
        }
    }

    /**
     * 发送好友请求
     */
    @ApiOperation("发送好友请求")
    @PostMapping("/friends/request")
    public ResponseResult<?> sendFriendRequest(@RequestBody Contact contact) {
        contact.setRelationType("friend");
        contact.setStatus("pending");
        boolean success = contactService.save(contact);
        if (success) {
            return ResponseResult.success("好友请求发送成功");
        } else {
            return ResponseResult.fail("500", "好友请求发送失败");
        }
    }

    /**
     * 删除好友
     */
    @ApiOperation("删除好友")
    @DeleteMapping("/friends")
    public ResponseResult<?> deleteFriend(@RequestParam String userId, @RequestParam String friendId) {
        // 删除双向好友关系
        boolean success1 = contactService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Contact>()
                .eq(Contact::getUserId, userId)
                .eq(Contact::getTargetId, friendId)
                .eq(Contact::getRelationType, "friend"));

        boolean success2 = contactService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Contact>()
                .eq(Contact::getUserId, friendId)
                .eq(Contact::getTargetId, userId)
                .eq(Contact::getRelationType, "friend"));

        if (success1 && success2) {
            return ResponseResult.success("删除好友成功");
        } else {
            return ResponseResult.fail("500", "删除好友失败");
        }
    }
}
