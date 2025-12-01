package com.pingyu.pingaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 案件 #008: RAG 存储配置
 * <p>
 * 使用 SimpleVectorStore (内存) 存储向量。
 * 核心逻辑：Bean 初始化时立即调用 Loader 读取文档并写入库中。
 */
@Configuration
@Slf4j
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore loveAppVectorStore(@Qualifier("dashscopeEmbeddingModel") EmbeddingModel embeddingModel) {
        // 1. 创建基于内存的简单向量库
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 2. (核心) 启动时立即加载文档
        // 解决 "挑战1: 内存数据库重启即丢" 的问题
        log.info("正在初始化本地向量知识库...");
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();

        if (!documents.isEmpty()) {
            simpleVectorStore.add(documents);
            log.info("本地知识库初始化完成，共存入 {} 个片段。", documents.size());
        } else {
            log.warn("未发现任何文档，知识库为空！请检查 resources/document 目录。");
        }

        return simpleVectorStore;
    }
}