-- 收藏对象 ID 跟随菜品主键类型。
-- mch_dish.id 已是 varchar(64)，推荐菜品还包含 seed_* 字符串 ID；
-- bigint 会导致收藏真实推荐菜品时插入失败。
ALTER TABLE `soc_collection`
    MODIFY COLUMN `collectable_id` VARCHAR(64) NOT NULL COMMENT '收藏对象ID';
