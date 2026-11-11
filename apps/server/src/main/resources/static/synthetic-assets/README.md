# 合成素材（Synthetic Assets）

本目录只存放**人工构造、与真实用户无关**的演示/测试素材，可以进入 Git。

规则：

- 不接收由真实数据脱敏或直接复制而来的文件；合成素材不能由生产数据生成。
- 不接收用户上传的原始附件、头像、聊天图片或任何运行时上传文件。
- 每个文件都要能被种子数据或测试明确引用，且文件名保持可读。

当前内容：

| 文件 | 用途 | 引用位置 |
| --- | --- | --- |
| `demo-user-avatar.png` | 演示账号 asd(17322222222) 的合成头像 | `db/migration/V15__synthetic_asset_avatar_path.sql`、`db/init/02_seed.sql` |

运行时上传目录（`uploads/` 以及 `static/uploads/` 下的任何文件）不入 Git，由 `make privacy-check` 与 CI 静态门强制。
