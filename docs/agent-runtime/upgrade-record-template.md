# dsh 升级 / 回退验证记录模板

> 记录写到运行机器上的 `$DSH_LOG_DIR/upgrade-records/`（仓库外），**不要提交进 Git**。
> 本文件只是模板，供人工核对用。

```text
时间：
操作人：
变更类型：升级 / 模型切换 / 插件切换 / 回退
范围：最多影响后续 Ticket（正在运行的任务不受影响）

dsh-superagent commit：
DSH 版本：
Node 主版本：
pnpm 版本：

升级前备份目录：
  state.tgz / profile.tgz 是否都生成：

探针结果：
  [ ] 健康  [ ] 认证  [ ] Source  [ ] Policy  [ ] 模型

结果：成功 / 回退
回退方式：scripts/agent-runtime/rollback.sh <备份目录>（不原地降级）
异常与处置：
```
