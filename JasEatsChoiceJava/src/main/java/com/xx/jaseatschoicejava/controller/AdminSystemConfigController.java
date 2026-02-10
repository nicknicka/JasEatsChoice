package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.SystemConfig;
import com.xx.jaseatschoicejava.service.SystemConfigService;
import com.xx.jaseatschoicejava.util.SystemLogHelper;
import com.xx.jaseatschoicejava.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-系统设置控制器
 */
@Api(tags = "管理员-系统设置")
@RestController
@RequestMapping("/admin/settings/config")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemConfigController {

    private static final Logger log = LoggerFactory.getLogger(AdminSystemConfigController.class);

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取配置列表（分页）
     */
    @ApiOperation("获取配置列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:setting:config')")
    public ResponseEntity<IPage<SystemConfig>> getConfigList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String configGroup,
            @RequestParam(required = false) String status) {

        Page<SystemConfig> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        QueryWrapper<SystemConfig> queryWrapper = new QueryWrapper<>();

        // 配置分组筛选
        if (StringUtils.hasText(configGroup)) {
            queryWrapper.eq("config_group", configGroup);
        }

        // 状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }

        // 关键词搜索（配置键、配置名称）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like("config_key", keyword)
                .or()
                .like("config_name", keyword)
            );
        }

        // 按配置分组和创建时间排序
        queryWrapper.orderByAsc("config_group")
            .orderByDesc("create_time");

        IPage<SystemConfig> result = systemConfigService.page(pageParam, queryWrapper);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有配置分组
     */
    @ApiOperation("获取所有配置分组")
    @GetMapping("/groups")
    @PreAuthorize("hasAnyAuthority('admin:setting:config')")
    public ResponseEntity<Map<String, Object>> getConfigGroups() {
        List<SystemConfig> configs = systemConfigService.list(
            new QueryWrapper<SystemConfig>()
                .select("DISTINCT config_group")
                .orderByAsc("config_group")
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", configs);

        return ResponseEntity.ok(response);
    }

    /**
     * 根据分组获取配置
     */
    @ApiOperation("根据分组获取配置")
    @GetMapping("/group/{configGroup}")
    @PreAuthorize("hasAnyAuthority('admin:setting:config')")
    public ResponseEntity<Map<String, Object>> getConfigsByGroup(@PathVariable String configGroup) {
        Map<String, String> configs = systemConfigService.getConfigsByGroup(configGroup);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", configs);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取配置详情
     */
    @ApiOperation("获取配置详情")
    @GetMapping("/{configId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:config')")
    public ResponseEntity<Map<String, Object>> getConfigDetail(@PathVariable String configId) {
        SystemConfig config = systemConfigService.getById(configId);

        Map<String, Object> response = new HashMap<>();
        if (config != null) {
            response.put("success", true);
            response.put("data", config);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "配置不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 创建配置
     */
    @ApiOperation("创建配置")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('admin:setting:config:create')")
    public ResponseEntity<Map<String, Object>> createConfig(@RequestBody Map<String, Object> request) {
        String configKey = (String) request.get("configKey");
        String configValue = (String) request.get("configValue");
        String configName = (String) request.get("configName");
        String configGroup = (String) request.get("configGroup");
        String configType = (String) request.get("configType");
        String description = (String) request.get("description");

        if (!StringUtils.hasText(configKey)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置键不能为空");
            return ResponseEntity.status(400).body(response);
        }

        if (!StringUtils.hasText(configGroup)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置分组不能为空");
            return ResponseEntity.status(400).body(response);
        }

        // 检查配置键是否已存在
        SystemConfig existingConfig = systemConfigService.getConfigByKey(configKey);
        if (existingConfig != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置键已存在");
            return ResponseEntity.status(400).body(response);
        }

        SystemConfig config = new SystemConfig();
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        config.setConfigName(configName);
        config.setConfigGroup(configGroup);
        config.setConfigType(StringUtils.hasText(configType) ? configType : "string");
        config.setDescription(description);
        config.setIsSystem(false);
        config.setStatus("active");
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());

        boolean success = systemConfigService.save(config);

        // 记录操作日志
        if (success) {
            SystemLogHelper.logCreate(
                "系统配置",
                "创建系统配置：" + configName,
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("configKey", configKey, "configGroup", configGroup)
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "配置创建成功");
            response.put("data", config);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "配置创建失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新配置
     */
    @ApiOperation("更新配置")
    @PutMapping("/{configId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:config:update')")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable String configId,
            @RequestBody Map<String, Object> request) {

        SystemConfig config = systemConfigService.getById(configId);
        if (config == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置不存在");
            return ResponseEntity.status(404).body(response);
        }

        String configValue = (String) request.get("configValue");
        String configName = (String) request.get("configName");
        String description = (String) request.get("description");
        String status = (String) request.get("status");

        if (StringUtils.hasText(configValue)) config.setConfigValue(configValue);
        if (StringUtils.hasText(configName)) config.setConfigName(configName);
        if (StringUtils.hasText(description)) config.setDescription(description);
        if (StringUtils.hasText(status)) config.setStatus(status);

        config.setUpdateTime(LocalDateTime.now());

        boolean success = systemConfigService.updateById(config);

        // 更新缓存
        if (success && StringUtils.hasText(configValue)) {
            systemConfigService.refreshConfigCache();
        }

        // 记录操作日志
        if (success) {
            SystemLogHelper.logUpdate(
                "系统配置",
                "更新系统配置：" + config.getConfigName(),
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("configKey", config.getConfigKey())
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "配置更新成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "配置更新失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除配置
     */
    @ApiOperation("删除配置")
    @DeleteMapping("/{configId}")
    @PreAuthorize("hasAnyAuthority('admin:setting:config:delete')")
    public ResponseEntity<Map<String, Object>> deleteConfig(@PathVariable String configId) {
        SystemConfig config = systemConfigService.getById(configId);
        if (config == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置不存在");
            return ResponseEntity.status(404).body(response);
        }

        // 系统内置配置不允许删除
        if (Boolean.TRUE.equals(config.getIsSystem())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "系统内置配置不允许删除");
            return ResponseEntity.status(403).body(response);
        }

        boolean success = systemConfigService.removeById(configId);

        // 刷新缓存
        if (success) {
            systemConfigService.refreshConfigCache();
        }

        // 记录操作日志
        if (success) {
            SystemLogHelper.logDelete(
                "系统配置",
                "删除系统配置：" + config.getConfigName(),
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("configKey", config.getConfigKey())
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "配置删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "配置删除失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 批量更新配置（按分组）
     */
    @ApiOperation("批量更新配置")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyAuthority('admin:setting:config:update')")
    public ResponseEntity<Map<String, Object>> batchUpdateConfigs(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, String> configs = (Map<String, String>) request.get("configs");
        String configGroup = (String) request.get("configGroup");

        if (configs == null || configs.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "配置数据不能为空");
            return ResponseEntity.status(400).body(response);
        }

        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String configKey = entry.getKey();
            String configValue = entry.getValue();

            try {
                boolean success = systemConfigService.updateConfig(configKey, configValue);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                log.error("更新配置失败: {}", configKey, e);
            }
        }

        // 刷新缓存
        systemConfigService.refreshConfigCache();

        // 记录操作日志
        if (successCount > 0) {
            SystemLogHelper.logUpdate(
                "系统配置",
                "批量更新系统配置：" + configGroup + "，" + successCount + "个成功",
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("configGroup", configGroup, "totalCount", configs.size(), "successCount", successCount)
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "批量更新完成：成功" + successCount + "个，失败" + failCount + "个");
        response.put("successCount", successCount);
        response.put("failCount", failCount);
        return ResponseEntity.ok(response);
    }

    /**
     * 刷新配置缓存
     */
    @ApiOperation("刷新配置缓存")
    @PostMapping("/refresh")
    @PreAuthorize("hasAnyAuthority('admin:setting:config')")
    public ResponseEntity<Map<String, Object>> refreshConfigCache() {
        systemConfigService.refreshConfigCache();

        // 记录操作日志
        SystemLogHelper.logUpdate(
            "系统配置",
            "刷新系统配置缓存",
            AdminContext.getAdminId(),
            AdminContext.getAdminUsername(),
            null
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "配置缓存已刷新");
        return ResponseEntity.ok(response);
    }
}
