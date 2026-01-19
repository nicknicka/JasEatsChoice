package com.xx.jaseatschoicejava.config;

import com.xx.jaseatschoicejava.util.XorSnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ID生成器配置类
 *
 * 配置XOR雪花算法ID生成器，替换原有的简单ID生成方式
 *
 * @author xx
 * @date 2026-01-19
 */
@Configuration
public class IdGeneratorConfig {

    /**
     * 机器ID（从环境变量或配置文件读取）
     * 默认值：1
     *
     * 环境变量示例：WORKER_ID=1
     * 配置文件示例：id.generator.worker-id=1
     */
    @Value("${id.generator.worker-id:1}")
    private Long workerId;

    /**
     * 数据中心ID（从环境变量或配置文件读取）
     * 默认值：1
     *
     * 环境变量示例：DATACENTER_ID=1
     * 配置文件示例：id.generator.datacenter-id=1
     */
    @Value("${id.generator.datacenter-id:1}")
    private Long datacenterId;

    /**
     * 纪元时间（可选）
     * 默认值：2024-01-01 00:00:00（1704067200000毫秒）
     *
     * 可以自定义为自己项目启动的时间
     */
    @Value("${id.generator.epoch:1704067200000}")
    private Long epoch;

    /**
     * 配置XOR雪花算法ID生成器
     *
     * @return XorSnowflakeIdGenerator实例
     */
    @Bean
    public XorSnowflakeIdGenerator xorSnowflakeIdGenerator() {
        long worker = validateWorkerId(workerId);
        long datacenter = validateDatacenterId(datacenterId);

        XorSnowflakeIdGenerator generator = new XorSnowflakeIdGenerator(
            worker,
            datacenter,
            epoch
        );

        // 打印配置信息
        System.out.println("========================================");
        System.out.println("  XOR雪花ID生成器已启动");
        System.out.println("========================================");
        System.out.println("  机器ID: " + worker);
        System.out.println("  数据中心ID: " + datacenter);
        System.out.println("  纪元时间: " + new java.util.Date(epoch));
        System.out.println("  掩码: " + generator.getXorMask());
        System.out.println("========================================");

        return generator;
    }

    /**
     * 验证机器ID
     *
     * @param workerId 机器ID
     * @return 有效的机器ID（0-31）
     */
    private long validateWorkerId(Long workerId) {
        if (workerId == null) {
            workerId = 1L;
        }
        if (workerId < 0 || workerId > 31) {
            throw new IllegalArgumentException(
                String.format("workerId必须在[0, 31]范围内，当前值：%d", workerId)
            );
        }
        return workerId;
    }

    /**
     * 验证数据中心ID
     *
     * @param datacenterId 数据中心ID
     * @return 有效的数据中心ID（0-31）
     */
    private long validateDatacenterId(Long datacenterId) {
        if (datacenterId == null) {
            datacenterId = 1L;
        }
        if (datacenterId < 0 || datacenterId > 31) {
            throw new IllegalArgumentException(
                String.format("datacenterId必须在[0, 31]范围内，当前值：%d", datacenterId)
            );
        }
        return datacenterId;
    }
}
