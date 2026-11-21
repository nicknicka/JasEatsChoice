package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.CartItem;

import java.util.List;
import java.util.Map;

/**
 * 购物车服务。
 *
 * <p>所有方法都以「当前登录用户」为作用域，userId 由调用方从验签身份传入，
 * 不从请求体读取，避免越权操作他人购物车。
 */
public interface CartItemService extends IService<CartItem> {

    /**
     * 加入购物车。同一 merchant + dishId + spec 已存在时合并数量。
     *
     * @param userId              当前用户ID
     * @param dishId              菜品ID
     * @param merchantId          商家ID，可为空（由菜品反查补齐）
     * @param quantity            数量，空或非正数按 1 处理
     * @param optionalIngredients 可选食材列表，可为空
     * @param spec                规格，可为空
     * @param remark              备注，可为空
     * @return 落库后的购物车项
     * @throws com.xx.jaseatschoicejava.exception.BusinessException 菜品不存在或未上架
     */
    CartItem addItem(String userId, String dishId, String merchantId, Integer quantity,
                     List<String> optionalIngredients, String spec, String remark);

    /**
     * 查询该用户购物车，按商家分组，并带上菜品与商家展示字段。
     *
     * @param userId 当前用户ID
     * @return 分组后的购物车
     */
    List<Map<String, Object>> listGroupedByMerchant(String userId);

    /**
     * 修改数量 / 规格 / 备注。规格变更若与已有项重复，会并入已有项。
     *
     * @param userId 当前用户ID
     * @param itemId 购物车项ID
     * @param quantity 新数量，为空表示不改
     * @param spec 新规格，为 null 表示不改
     * @param remark 新备注，为 null 表示不改
     * @return 更新后的项；项不存在或不属于该用户时返回 null
     */
    CartItem updateItem(String userId, String itemId, Integer quantity, String spec, String remark);

    /**
     * 删除单个购物车项。
     *
     * @return 是否删除成功；项不存在或不属于该用户时为 false
     */
    boolean deleteItem(String userId, String itemId);

    /**
     * 按商家 + 菜品删除（同一菜品的所有规格一起删）。
     *
     * @return 实际删除的条数
     */
    int deleteByDish(String userId, String merchantId, String dishId);

    /**
     * 批量删除。
     *
     * @return 实际删除的条数
     */
    int batchDelete(String userId, List<String> itemIds);

    /**
     * 清空购物车，merchantId 为空时清空全部。
     *
     * @return 实际删除的条数
     */
    int clear(String userId, String merchantId);

    /**
     * 选中 / 取消选中单个项。
     *
     * @return 是否更新成功
     */
    boolean selectItem(String userId, String itemId, Boolean selected);

    /**
     * 全选 / 取消全选，merchantId 为空时作用于全部。
     *
     * @return 实际更新条数
     */
    int selectAll(String userId, String merchantId, Boolean selected);
}
