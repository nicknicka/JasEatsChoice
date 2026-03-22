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

    @Tool("Get user delivery address list including receiver name phone detailed address etc")
    public String getUserAddresses(String userId) {
        log.info("Executing tool: getUserAddresses, user: {}", userId);

        try {
            QueryWrapper<Address> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("is_default")
                    .orderByDesc("create_time");

            List<Address> addresses = addressService.list(queryWrapper);

            if (addresses == null || addresses.isEmpty()) {
                return "Delivery Addresses\n\nYou have not added any delivery address yet.";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Your Delivery Addresses (%d total)\n\n", addresses.size()));

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
                    result.append(String.format("Receiver: %s\n", address.getReceiverName()));
                }

                if (address.getReceiverPhone() != null) {
                    result.append(String.format("Phone: %s\n", maskPhoneNumber(address.getReceiverPhone())));
                }

                if (address.getDetail() != null) {
                    result.append(String.format("Address: %s\n", address.getDetail()));
                }

                if (address.getIsDefault() != null && address.getIsDefault() == 1) {
                    result.append("Default Address\n");
                }

                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Failed to get user addresses", e);
            return "Failed to get user addresses: " + e.getMessage();
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 11) {
            return phoneNumber != null ? phoneNumber : "Not set";
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
    }
}
