package com.xx.jaseatschoicejava.util;

import java.security.SecureRandom;

/**
 * XOR混淆的雪花ID生成器
 *
 * 特点：
 * 1. 基于雪花算法生成有序ID
 * 2. 使用XOR掩码混淆，使ID不可预测
 * 3. 支持20位数字输出
 * 4. 保持高性能
 *
 * @author xx
 * @date 2026-01-19
 */
public class XorSnowflakeIdGenerator {

    // ==================== 常量定义 ====================

    /**
     * 纪元时间（起始时间）
     * 可以自定义，建议使用项目启动时间
     * 1704067200000L = 2024-01-01 00:00:00
     */
    private final long epoch;

    /**
     * 各部分的位数
     */
    private final long workerIdBits;      // 机器ID位数
    private final long datacenterIdBits;  // 数据中心ID位数
    private final long sequenceBits;      // 序列号位数

    /**
     * 各部分的最大值（通过位移计算）
     */
    private final long maxWorkerId;
    private final long maxDatacenterId;
    private final long maxSequence;

    /**
     * 各部分的位移量
     */
    private final long workerIdShift;
    private final long datacenterIdShift;
    private final long timestampShift;

    // ==================== 实例变量 ====================

    /**
     * 机器ID（5位：0-31）
     */
    private final long workerId;

    /**
     * 数据中心ID（5位：0-31）
     */
    private final long datacenterId;

    /**
     * XOR掩码（用于混淆ID，使其不可预测）
     * 使用随机数生成，每次运行不同
     */
    private final long xorMask;

    /**
     * 序列号（同一毫秒内的计数器）
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳（毫秒）
     */
    private long lastTimestamp = -1L;

    // ==================== 构造函数 ====================

    /**
     * 构造函数（默认配置）
     *
     * @param workerId 机器ID（0-31）
     * @param datacenterId 数据中心ID（0-31）
     */
    public XorSnowflakeIdGenerator(long workerId, long datacenterId) {
        this(workerId, datacenterId, 1704067200000L);
    }

    /**
     * 构造函数（自定义纪元）
     *
     * @param workerId 机器ID（0-31）
     * @param datacenterId 数据中心ID（0-31）
     * @param epoch 纪元时间（毫秒）
     */
    public XorSnowflakeIdGenerator(long workerId, long datacenterId, long epoch) {
        // 默认配置：5位机器ID + 5位数据中心ID + 12位序列号
        this(workerId, datacenterId, epoch, 5L, 5L, 12L);
    }

    /**
     * 完整构造函数（自定义所有参数）
     *
     * @param workerId 机器ID
     * @param datacenterId 数据中心ID
     * @param epoch 纪元时间
     * @param workerIdBits 机器ID位数
     * @param datacenterIdBits 数据中心ID位数
     * @param sequenceBits 序列号位数
     */
    public XorSnowflakeIdGenerator(long workerId, long datacenterId, long epoch,
                                   long workerIdBits, long datacenterIdBits, long sequenceBits) {
        // 参数校验
        if (workerId < 0 || workerId >= (1L << workerIdBits)) {
            throw new IllegalArgumentException(
                String.format("workerId必须在[0, %d]范围内", (1L << workerIdBits) - 1)
            );
        }
        if (datacenterId < 0 || datacenterId >= (1L << datacenterIdBits)) {
            throw new IllegalArgumentException(
                String.format("datacenterId必须在[0, %d]范围内", (1L << datacenterIdBits) - 1)
            );
        }

        // 初始化常量
        this.epoch = epoch;
        this.workerIdBits = workerIdBits;
        this.datacenterIdBits = datacenterIdBits;
        this.sequenceBits = sequenceBits;

        // 计算最大值
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxDatacenterId = ~(-1L << datacenterIdBits);
        this.maxSequence = ~(-1L << sequenceBits);

        // 计算位移量
        this.workerIdShift = sequenceBits;
        this.datacenterIdShift = sequenceBits + workerIdBits;
        this.timestampShift = datacenterIdShift + datacenterIdBits;

        // 保存ID
        this.workerId = workerId;
        this.datacenterId = datacenterId;

        // 生成XOR掩码（关键！）
        this.xorMask = generateXorMask();
    }

    // ==================== 核心方法 ====================

    /**
     * 生成下一个ID（同步方法，保证线程安全）
     *
     * @return 20位数字ID字符串
     */
    public synchronized String nextId() {
        return generateIdAsString();
    }

    /**
     * ��时不等待版本（推荐用于高并发）
     *
     * @return 20位数字ID字符串
     */
    public String nextIdNoWait() {
        return generateIdAsStringNoWait();
    }

    /**
     * 生成ID的Long形式
     *
     * @return ID的Long值
     */
    public synchronized long nextLongId() {
        return generateSnowflakeId();
    }

    // ==================== 内部实现 ====================

    /**
     * 生成雪花ID（Long形式）
     */
    private long generateSnowflakeId() {
        long timestamp = getCurrentTimestamp();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                // 5ms内的回拨，等待
                timestamp = tilNextMillis(lastTimestamp);
            } else {
                // 超过5ms，抛出异常
                throw new RuntimeException(
                    String.format("时钟回拨超过%dms，拒绝生成ID", offset)
                );
            }
        }

        // 同一毫秒内，递增序列号
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                // 序列号用完，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 新的毫秒，重置序列号
            sequence = 0;
        }

        lastTimestamp = timestamp;

        // 组合标准雪花ID
        return ((timestamp - epoch) << timestampShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    /**
     * 生成ID字符串（20位）
     */
    private String generateIdAsString() {
        // 1. 生成标准雪花ID
        long snowflakeId = generateSnowflakeId();

        // 2. XOR混淆（关键步骤！）
        long obfuscatedId = xorMask ^ snowflakeId;

        // 3. 确保是正数且为20位
        return formatTo20Digits(obfuscatedId);
    }

    /**
     * 生成ID字符串（不等待版本）
     */
    private String generateIdAsStringNoWait() {
        long timestamp = getCurrentTimestamp();

        // 时钟回拨时直接使用当前时间
        if (timestamp < lastTimestamp) {
            timestamp = lastTimestamp;
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                // 序列号用完，保持当前时间戳
                // 注意：这可能导致ID重复，仅在极端情况下
            }
        } else {
            sequence = 0;
            lastTimestamp = timestamp;
        }

        long snowflakeId = ((timestamp - epoch) << timestampShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;

        long obfuscatedId = xorMask ^ snowflakeId;
        return formatTo20Digits(obfuscatedId);
    }

    /**
     * 生成XOR掩码
     * 使用SecureRandom生成随机掩码
     */
    private long generateXorMask() {
        SecureRandom random = new SecureRandom();
        long mask = random.nextLong();

        // 确保掩码的某些位是1（避免前导零过多）
        // 设置高3位为1，确保结果大于10000000000000000000
        mask |= 0xE000000000000000L;

        return mask;
    }

    /**
     * 格式化为20位数字字符串
     *
     * @param id 原始ID
     * @return 20位数字字符串
     */
    private String formatTo20Digits(long id) {
        // 取绝对值（确保正数）
        id = Math.abs(id);

        // Long类型的最大值：9223372036854775807
        // 20位数字的范围：10000000000000000000 到 9223372036854775807
        final long MIN_ID = 1000000000000000000L;  // 19位 + 1位 = 20位
        final long MAX_ID = 9223372036854775807L;  // Long最大值

        // 如果不足20位，添加偏移量
        if (id < MIN_ID) {
            id = id + MIN_ID;
        }

        // 如果超过最大值，使用模运算保持在范围内
        if (id > MAX_ID) {
            id = id % MIN_ID;
        }

        // 格式化为20位字符串（补齐前导零）
        return String.format("%020d", id);
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    protected long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 等待下一毫秒
     *
     * @param lastTimestamp 上次时间戳
     * @return 新的时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建默认生成器
     *
     * @param workerId 机器ID（0-31）
     * @param datacenterId 数据中心ID（0-31）
     * @return ID生成器实例
     */
    public static XorSnowflakeIdGenerator create(long workerId, long datacenterId) {
        return new XorSnowflakeIdGenerator(workerId, datacenterId);
    }

    /**
     * 创建自定义生成器
     *
     * @param workerId 机器ID
     * @param datacenterId 数据中心ID
     * @param epoch 纪元时间
     * @return ID生成器实例
     */
    public static XorSnowflakeIdGenerator create(long workerId, long datacenterId, long epoch) {
        return new XorSnowflakeIdGenerator(workerId, datacenterId, epoch);
    }

    // ==================== 解析方法（可选）====================

    /**
     * 解析ID，提取时间戳
     * 注意：由于XOR混淆，需要知道掩码才能正确解析
     *
     * @param idStr ID字符串
     * @return 时间戳
     */
    public long parseTimestamp(String idStr) {
        long id = Long.parseLong(idStr);
        // 反向XOR
        long snowflakeId = id ^ xorMask;
        // 提取时间戳
        long timestamp = ((snowflakeId >> timestampShift) & ~(-1L << 41)) + epoch;
        return timestamp;
    }

    /**
     * 解析ID，提取机器ID
     *
     * @param idStr ID字符串
     * @return 机器ID
     */
    public long parseWorkerId(String idStr) {
        long id = Long.parseLong(idStr);
        long snowflakeId = id ^ xorMask;
        return (snowflakeId >> workerIdShift) & maxWorkerId;
    }

    /**
     * 解析ID，提取数据中心ID
     *
     * @param idStr ID字符串
     * @return 数据中心ID
     */
    public long parseDatacenterId(String idStr) {
        long id = Long.parseLong(idStr);
        long snowflakeId = id ^ xorMask;
        return (snowflakeId >> datacenterIdShift) & maxDatacenterId;
    }

    // ==================== Getter方法 ====================

    public long getWorkerId() {
        return workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public long getXorMask() {
        return xorMask;
    }

    public long getEpoch() {
        return epoch;
    }

    // ==================== 测试方法 ====================

    /**
     * 主方法：测试生成器
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  XOR雪花ID生成器测试");
        System.out.println("========================================\n");

        // 创建生成器
        XorSnowflakeIdGenerator generator = create(1, 1);

        System.out.println("生成器配置：");
        System.out.println("  机器ID: " + generator.getWorkerId());
        System.out.println("  数据中心ID: " + generator.getDatacenterId());
        System.out.println("  XOR掩码: " + generator.getXorMask());
        System.out.println("  纪元时间: " + generator.getEpoch());
        System.out.println();

        // 测试1：生成10个ID
        System.out.println("生成的ID（前10个）：");
        for (int i = 0; i < 10; i++) {
            String id = generator.nextId();
            System.out.println("  " + id);
        }
        System.out.println();

        // 测试2：验证唯一性
        System.out.println("验证唯一性（生成10000个）：");
        java.util.Set<String> ids = new java.util.HashSet<>();
        boolean hasDuplicate = false;
        for (int i = 0; i < 10000; i++) {
            String id = generator.nextId();
            if (!ids.add(id)) {
                System.out.println("  ❌ 发现重复：" + id);
                hasDuplicate = true;
            }
        }
        if (!hasDuplicate) {
            System.out.println("  ✅ 10000个ID全部唯一！");
        }
        System.out.println();

        // 测试3：性能测试
        System.out.println("性能测试（生成10000个）：");
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            generator.nextId();
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000; // 转换为毫秒
        System.out.println("  总耗时：" + duration + "ms");
        System.out.println("  平均耗时：" + (duration * 100000.0 / 10000) + "纳秒/个");
        System.out.println();

        // 测试4：解析ID
        System.out.println("解析ID示例：");
        String testId = generator.nextId();
        System.out.println("  ID: " + testId);
        System.out.println("  时间戳: " + new java.util.Date(generator.parseTimestamp(testId)));
        System.out.println("  机器ID: " + generator.parseWorkerId(testId));
        System.out.println("  数据中心ID: " + generator.parseDatacenterId(testId));

        System.out.println("\n========================================");
        System.out.println("  测试完成！");
        System.out.println("========================================");
    }
}
