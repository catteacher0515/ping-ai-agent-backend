package com.pingyu.pingaiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 案件 #009: 云端 RAG 配置中心
 * <p>
 * 核心目标：初始化检索增强顾问 (RetrievalAugmentationAdvisor)。
 * 它将连接阿里云百炼平台的云知识库。
 */
@Configuration
@Slf4j
public class LoveAppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    // [警示] 这里的名称必须与你在阿里云百炼控制台创建的知识库名称完全一致！
    private static final String KNOWLEDGE_INDEX_NAME = "恋爱大师";

    @Bean
    public Advisor loveAppRagCloudAdvisor() {
        log.info("正在初始化云端知识库顾问，目标索引: {}", KNOWLEDGE_INDEX_NAME);

        // 1. 初始化 DashScope API 客户端
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);

        // 2. 创建文档检索器 (Retriever)
        // 它的工作是：拿着用户的问题，去阿里云百炼的知识库里找答案
        DashScopeDocumentRetriever documentRetriever = new DashScopeDocumentRetriever(
                dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX_NAME) // 核心配置：按名称检索
                        .build()
        );

        // 3. 组装检索增强顾问 (Advisor)
        // 它的工作是：协调检索器，将检索到的文档拼接到 Prompt 中
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }
}