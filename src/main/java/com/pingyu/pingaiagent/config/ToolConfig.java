package com.pingyu.pingaiagent.config;

import com.pingyu.pingaiagent.tools.FileOperationTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 案件 Tool-002: 工具注册中心
 * <p>
 * 作用：将我们编写的 Java 工具类注册为 Spring Bean，
 * 并包装成 AI 可识别的 ToolCallback。
 */
@Configuration
public class ToolConfig {

    /**
     * 1. 注册工具实例
     */
    @Bean
    public FileOperationTool fileOperationTool() {
        return new FileOperationTool();
    }

    /**
     * 2. 将工具包装为 ToolCallback
     * 这样 ChatClient 才能识别并调用它们
     */
    @Bean
    public ToolCallback[] fileOperationTools(FileOperationTool fileOperationTool) {
        return ToolCallbacks.from(fileOperationTool);
    }
}