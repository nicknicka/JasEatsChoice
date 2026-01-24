package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户画像Mapper接口
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    /**
     * 根据用户ID查询用户画像
     */
    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    UserProfile getByUserId(@Param("userId") String userId);

    /**
     * 获取需要更新的用户画像列表（根据最后更新时间）
     */
    @Select("SELECT * FROM user_profile WHERE last_updated < #{threshold}")
    List<UserProfile> getProfilesNeedUpdate(java.time.LocalDateTime threshold);

    /**
     * 批量更新用户画像
     */
    int batchUpdate(@Param("profiles") List<UserProfile> profiles);
}
