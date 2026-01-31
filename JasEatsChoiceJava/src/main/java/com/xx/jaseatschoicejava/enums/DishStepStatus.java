package com.xx.jaseatschoicejava.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜品步骤状态枚举
 *
 * @author Claude
 * @since 2025-01-30
 */
@Getter
@AllArgsConstructor
public enum DishStepStatus {

    /**
     * 待备菜
     */
    PENDING_PREPARATION(0, "待备菜"),

    /**
     * 备菜中
     */
    PREPARING(1, "备菜中"),

    /**
     * 预处理中
     */
    PRE_PROCESSING(2, "预处理中"),

    /**
     * 烹饪中
     */
    COOKING(3, "烹饪中"),

    /**
     * 摆盘中
     */
    PLATING(4, "摆盘中"),

    /**
     * 待上菜
     */
    WAITING_FOR_SERVING(5, "待上菜"),

    /**
     * 已上菜
     */
    SERVED(6, "已上菜"),

    /**
     * 快餐-制作中
     */
    FAST_FOOD_MAKING(10, "制作中"),

    /**
     * 快餐-打包中
     */
    FAST_FOOD_PACKING(11, "打包中"),

    /**
     * 快餐-待出餐
     */
    FAST_FOOD_READY(12, "待出餐"),

    /**
     * 快餐-已出餐
     */
    FAST_FOOD_SERVED(13, "已出餐");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static DishStepStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DishStepStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为快餐类步骤
     */
    public boolean isFastFoodStep() {
        return this.code >= 10;
    }

    /**
     * 判断是否为正餐类步骤
     */
    public boolean isFineDiningStep() {
        return this.code < 10;
    }

    /**
     * 获取下一步骤
     */
    public DishStepStatus getNextStep() {
        switch (this) {
            case PENDING_PREPARATION:
                return PREPARING;
            case PREPARING:
                return PRE_PROCESSING;
            case PRE_PROCESSING:
                return COOKING;
            case COOKING:
                return PLATING;
            case PLATING:
                return WAITING_FOR_SERVING;
            case WAITING_FOR_SERVING:
                return SERVED;
            case FAST_FOOD_MAKING:
                return FAST_FOOD_PACKING;
            case FAST_FOOD_PACKING:
                return FAST_FOOD_READY;
            case FAST_FOOD_READY:
                return FAST_FOOD_SERVED;
            default:
                return null;
        }
    }

    /**
     * 获取可回退到的步骤列表
     */
    public DishStepStatus[] getRollbackSteps() {
        switch (this) {
            case PRE_PROCESSING:
                return new DishStepStatus[]{PREPARING};
            case COOKING:
                return new DishStepStatus[]{PRE_PROCESSING, PREPARING};
            case PLATING:
                return new DishStepStatus[]{COOKING, PRE_PROCESSING, PREPARING};
            case WAITING_FOR_SERVING:
                return new DishStepStatus[]{PLATING, COOKING, PRE_PROCESSING, PREPARING};
            case FAST_FOOD_PACKING:
                return new DishStepStatus[]{FAST_FOOD_MAKING};
            case FAST_FOOD_READY:
                return new DishStepStatus[]{FAST_FOOD_PACKING, FAST_FOOD_MAKING};
            default:
                return new DishStepStatus[]{};
        }
    }
}
