package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.AgentRuntimeConfig;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Agent 运行时配置 Mapper。
 */
@Mapper
public interface AgentRuntimeConfigMapper extends BaseMapper<AgentRuntimeConfig> {

    /**
     * 追加一条配置版本历史。
     */
    @Insert("INSERT INTO ai_agent_runtime_config_history "
            + "(history_id, config_id, version, provider, model, endpoint, data_policy_ack, updated_by, created_at) "
            + "VALUES (#{historyId}, #{configId}, #{version}, #{provider}, #{model}, #{endpoint}, "
            + "#{dataPolicyAck}, #{updatedBy}, CURRENT_TIMESTAMP)")
    int insertHistory(@Param("historyId") String historyId,
                      @Param("configId") String configId,
                      @Param("version") Integer version,
                      @Param("provider") String provider,
                      @Param("model") String model,
                      @Param("endpoint") String endpoint,
                      @Param("dataPolicyAck") Boolean dataPolicyAck,
                      @Param("updatedBy") String updatedBy);
}
