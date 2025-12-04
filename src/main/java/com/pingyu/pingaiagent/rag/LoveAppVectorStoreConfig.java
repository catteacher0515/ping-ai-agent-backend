package com.pingyu.pingaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore; // 变回 Simple
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 案件#010 (回滚版): 内存向量存储配置
 * <p>
 * 状态: 临时回滚
 * 原因: PGVector 依赖环境冲突，优先保证主线业务运行。
 * 策略: 使用 SimpleVectorStore (重启后数据丢失，但在开发期足够用)
 */
@Configuration
@Slf4j
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore loveAppVectorStore(
            @Qualifier("dashscopeEmbeddingModel") EmbeddingModel embeddingModel) {

        log.info("正在初始化 SimpleVectorStore (内存版)...");

        // 1. 构建内存向量库
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 2. 启动时立即加载文档 (因为内存库重启就空了，所以每次必须重新加载)
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        if (!documents.isEmpty()) {
            vectorStore.add(documents);
            log.info("内存知识库初始化完成，共存入 {} 个片段。", documents.size());
        } else {
            log.warn("未发现任何文档。");
        }

        return vectorStore;
    }
}