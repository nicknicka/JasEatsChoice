package com.xx.jaseatschoicejava.agent.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.Address;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.UserPreference;
import com.xx.jaseatschoicejava.service.AddressService;
import com.xx.jaseatschoicejava.service.UserPreferenceService;
import com.xx.jaseatschoicejava.service.UserService;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserTools {

    private static final Logger log = LoggerFactory.getLogger(UserTools.class);

    @Resource
    private UserService userService;

    @Resource
    private UserPreferenceService userPreferenceService;

    @Resource
    private AddressService addressService;

    @Tool("Get user basic information including nickname phone registration time etc")
    public String getUserProfile(String userId) {
        log.info("Executing tool: getUserProfile, user: {}", userId);

        try {
            User user = userService.getById(userId);
            if (user == null) {
                return "User information not found";
            }

            StringBuilder result = new StringBuilder();
            result.append("User Profile\n\n");
            result.append(String.format("Nickname: %s\n", user.getNickname() != null ? user.getNickname() : "Not set"));
            result.append(String.format("Phone: %s\n", maskPhoneNumber(user.getPhone())));

            if (user.getCreateTime() != null) {
                String registerTime = user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                result.append(String.format("Registration Time: %s\n", registerTime));
            }

            if (user.getEmail() != null) {
                result.append(String.format("Email: %s\n", user.getEmail()));
            }

            if (user.getLocation() != null) {
                result.append(String.format("Location: %s\n", user.getLocation()));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Failed to get user information", e);
            return "Failed to get user information: " + e.getMessage();
        }
    }

    @Tool("Get user dietary preferences including diet goals allergies preference tags etc")
    public String getUserPreferences(String userId) {
        log.info("Executing tool: getUserPreferences, user: {}", userId);

        try {
            QueryWrapper<UserPreference> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            UserPreference preference = userPreferenceService.getOne(queryWrapper);

            if (preference == null) {
                return "User has not set dietary preferences yet.";
            }

            StringBuilder result = new StringBuilder();
            result.append("Dietary Preferences\n\n");

            if (preference.getDietGoal() != null && !preference.getDietGoal().isEmpty()) {
                result.append(String.format("Diet Goal: %s\n", preference.getDietGoal()));
            }

            if (preference.getAllergies() != null && !preference.getAllergies().isEmpty()) {
                result.append(String.format("Allergies: %s\n", preference.getAllergies()));
            }

            if (preference.getTagWeights() != null && !preference.getTagWeights().isEmpty()) {
                result.append(String.format("Taste Preferences: %s\n", preference.getTagWeights()));
            }

            if (preference.getDisableWeatherRecommend() != null) {
                result.append(String.format("Weather Recommendation: %s\n", preference.getDisableWeatherRecommend() ? "Disabled" : "Enabled"));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Failed to get user preferences", e);
            return "Failed to get user preferences: " + e.getMessage();
        }
    }

    @Tool("获取用户的配送地址列表，包括收货人姓名、电话、详细地址等")
    public String getUserAddresses(String userId) {
        log.info("执行工具：getUserAddresses，用户：{}", userId);

        try {
            QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("is_default")
                    .orderByDesc("create_time");

            List<Address> addresses = addressService.list(queryWrapper);

            if (addresses == null || addresses.isEmpty()) {
                return "配送地址列表\n\n您还没有添加配送地址。";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("您的配送地址（共%d个）\n\n", addresses.size()));

            for (int i = 0; i < addresses.size(); i++) {
                Address address = addresses.get(i);
                result.append(String.format("%d. ", i + 1));

                if (address.getProvince() != null) {
                    result.append(address.getProvince());
                }
                if (address.getCity() != null) {
                    result.append(address.getCity());
                }
                if (address.getDistrict() != null) {
                    result.append(address.getDistrict());
                }
                result.append("\n");

                if (address.getReceiverName() != null) {
                    result.append(String.format("收货人：%s\n", address.getReceiverName()));
                }

                if (address.getReceiverPhone() != null) {
                    result.append(String.format("电话：%s\n", maskPhoneNumber(address.getReceiverPhone())));
                }

                if (address.getDetail() != null) {
                    result.append(String.format("详细地址：%s\n", address.getDetail()));
                }

                if (address.getIsDefault() != null && address.getIsDefault() == 1) {
                    result.append("【默认地址】");
                }

                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取用户地址失败", e);
            return "获取用户地址失败：" + e.getMessage();
        }
    }

    /**
     * 智能推荐最合适的配送地址
     * 优先级：默认地址 > 最近使用的地址
     *
     * @param userId 用户ID
     * @return 推荐的地址信息
     */
    @Tool("智能推荐用户最常用的配送地址，优先使用默认地址或最近使用的地址")
    public String getRecommendedAddress(String userId) {
        log.info("执行工具：getRecommendedAddress，用户：{}", userId);

        try {
            // 1. 首先查找默认地址
            QueryWrapper<Address> defaultQuery = new QueryWrapper<>();
            defaultQuery.eq("user_id", userId)
                    .eq("is_default", 1);

            Address recommendedAddress = addressService.getOne(defaultQuery);

            // 2. 如果没有默认地址，使用最近创建的地址
            if (recommendedAddress == null) {
                QueryWrapper<Address> recentQuery = new QueryWrapper<>();
                recentQuery.eq("user_id", userId)
                        .orderByDesc("create_time")
                        .last("LIMIT 1");

                recommendedAddress = addressService.getOne(recentQuery);
            }

            // 3. 如果没有任何地址，返回提示
            if (recommendedAddress == null) {
                return "您还没有添加配送地址，请先添加地址再下单。";
            }

            // 4. 构建推荐结果
            StringBuilder result = new StringBuilder();
            result.append("📍 **推荐配送地址**\n\n");

            // 完整地址
            StringBuilder fullAddress = new StringBuilder();
            if (recommendedAddress.getProvince() != null) {
                fullAddress.append(recommendedAddress.getProvince());
            }
            if (recommendedAddress.getCity() != null) {
                fullAddress.append(recommendedAddress.getCity());
            }
            if (recommendedAddress.getDistrict() != null) {
                fullAddress.append(recommendedAddress.getDistrict());
            }
            if (recommendedAddress.getDetail() != null) {
                fullAddress.append(recommendedAddress.getDetail());
            }

            result.append(String.format("**地址：** %s\n", fullAddress.toString()));
            result.append(String.format("**收货人：** %s\n", recommendedAddress.getReceiverName()));
            result.append(String.format("**电话：** %s\n", maskPhoneNumber(recommendedAddress.getReceiverPhone())));

            // 标记默认地址
            if (recommendedAddress.getIsDefault() != null && recommendedAddress.getIsDefault() == 1) {
                result.append("\n✅ 这是您的默认地址");
            } else {
                result.append("\n📅 这是您最近添加的地址");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取推荐地址失败", e);
            return "获取推荐地址失败：" + e.getMessage();
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 11) {
            return phoneNumber != null ? phoneNumber : "Not set";
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
    }
}
