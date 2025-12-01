package com.pingyu.pingaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 案件 #008: ETL 组件 - 文档加载器
 * <p>
 * 负责从 classpath:document/ 目录下读取所有 .md 文件，
 * 并将其转换为 Spring AI 可识别的 Document 对象列表。
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载所有 Markdown 文档
     *
     * @return 文档列表
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            // 1. 扫描 document 目录下所有 .md 文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");

            for (Resource resource : resources) {
                // 2. 配置 Reader：提取文件名作为元数据 (Metadata)，便于后续检索追踪
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true) // 按水平线分割
                        .withIncludeCodeBlock(false)            // 忽略代码块（视需求而定）
                        .withAdditionalMetadata("filename", resource.getFilename())
                        .build();

                // 3. 执行读取
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
            log.info("成功加载 {} 个文档片段", allDocuments.size());
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
            throw new RuntimeException("文档加载失败", e);
        }
        return allDocuments;
    }
}