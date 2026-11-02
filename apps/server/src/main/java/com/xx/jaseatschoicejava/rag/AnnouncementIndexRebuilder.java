package com.xx.jaseatschoicejava.rag;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公告索引重建编排：换 Embedding 模型后异步全量重建，并维护公告 RAG 的就绪状态。
 *
 * 重建期间公告 RAG 处于「重建中」，检索失败关闭；重建失败停在「不可用」并保留原因，
 * 由管理员显式重试，不自动回滚已切换的配置（旧向量已不在新模型的向量空间里）。
 */
public class AnnouncementIndexRebuilder {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementIndexRebuilder.class);

    private final AnnouncementRagInfrastructure infrastructure;
    private final RagKnowledgeService knowledgeService;
    private final Executor executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public AnnouncementIndexRebuilder(
            AnnouncementRagInfrastructure infrastructure,
            RagKnowledgeService knowledgeService) {
        this(infrastructure, knowledgeService, singleThreadExecutor());
    }

    public AnnouncementIndexRebuilder(
            AnnouncementRagInfrastructure infrastructure,
            RagKnowledgeService knowledgeService,
            Executor executor) {
        this.infrastructure = infrastructure;
        this.knowledgeService = knowledgeService;
        this.executor = executor;
    }

    /**
     * 请求一次异步重建。已有重建在跑时忽略，避免并发重建互相覆盖。
     */
    public void requestRebuild() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        // 先同步进入「重建中」：配置已经切到新模型，此刻任何检索都必须失败关闭
        infrastructure.markRebuilding("正在重建公告索引");
        try {
            executor.execute(this::rebuildNow);
        }
        catch (RuntimeException exception) {
            running.set(false);
            infrastructure.markUnavailable("公告索引重建调度失败：" + exception.getMessage());
            throw exception;
        }
    }

    /**
     * 同步执行一次重建，返回重建的公告条数。供重试与测试使用。
     */
    public int rebuildNow() {
        running.set(true);
        infrastructure.markRebuilding("正在重建公告索引");
        try {
            if (infrastructure.embeddingStore() == null) {
                infrastructure.markUnavailable(infrastructure.status().reason());
                return 0;
            }
            int count = knowledgeService.rebuildAllAnnouncements();
            infrastructure.markReady("公告索引重建完成，共 " + count + " 条");
            knowledgeService.drainPendingAnnouncements();
            return count;
        }
        catch (Exception exception) {
            log.error("公告索引重建失败", exception);
            infrastructure.markUnavailable("公告索引重建失败：" + exception.getMessage());
            return 0;
        }
        finally {
            running.set(false);
        }
    }

    public AnnouncementRagStatus status() {
        return infrastructure.status();
    }

    private static Executor singleThreadExecutor() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "announcement-index-rebuild");
            thread.setDaemon(true);
            return thread;
        });
    }
}
