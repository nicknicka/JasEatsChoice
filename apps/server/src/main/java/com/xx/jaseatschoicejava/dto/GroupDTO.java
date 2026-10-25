package com.xx.jaseatschoicejava.dto;

import com.xx.jaseatschoicejava.entity.Group;
import com.xx.jaseatschoicejava.util.IdPrefixUtil;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群数据传输对象，用于向前端传输群信息
 */
@Data
public class GroupDTO {
    private String id; // 群ID，带G前缀
    private String creatorId; // 创建者ID，带U前缀
    private String groupName; // 群名称
    private String description; // 群描述
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间

    /**
     * 从Group实体转换为GroupDTO，自动添加前缀
     * @param group 群实体
     * @return GroupDTO
     */
    public static GroupDTO fromGroup(Group group) {
        if (group == null) {
            return null;
        }

        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(IdPrefixUtil.addGroupPrefix(group.getId()));
        groupDTO.setCreatorId(IdPrefixUtil.addUserPrefix(group.getCreatorId()));
        groupDTO.setGroupName(group.getGroupName());
        groupDTO.setDescription(group.getDescription());
        groupDTO.setCreateTime(group.getCreateTime());
        groupDTO.setUpdateTime(group.getUpdateTime());

        return groupDTO;
    }
}
