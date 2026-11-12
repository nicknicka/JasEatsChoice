-- 合成素材迁移：演示用户头像。
--
-- 背景：V2 种子里 asd(17322222222) 的头像指向
--   /api/uploads/3384650106421960/3f3346f8-....png
-- 该文件是人工构造的合成头像，却长期放在运行时上传目录 static/uploads 下，
-- 使「真实上传目录不入 Git」无法成立（.gitignore 被迫用 ! 反选把它重新纳入跟踪）。
--
-- 处理：
--   1. 头像文件已移动到明确的合成素材目录 static/synthetic-assets/demo-user-avatar.png，
--      由 Spring Boot 默认静态资源处理器暴露为 /api/synthetic-assets/demo-user-avatar.png；
--   2. 这里把种子引用改到新地址，保证演示账号头像仍能正常展示。
--
-- 说明：V2 已被 Flyway 应用，validate-on-migrate=true 会校验 checksum，不能回改，
--       因此以新增迁移的方式修正（与 V13 同一约定）。

UPDATE `usr_user`
SET `avatar` = '/api/synthetic-assets/demo-user-avatar.png'
WHERE `user_id` = '3384650106421960';
