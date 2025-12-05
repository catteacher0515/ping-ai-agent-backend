package com.pingyu.pingaiagent.config;

import com.pingyu.pingaiagent.tools.AgentTool;
import com.pingyu.pingaiagent.tools.FileOperationTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Slf4j
public class ToolConfig {

    // 1. 保留 FileOperationTool 的 Bean 定义 (因为它没有加 @Component，或者为了明确单例)
    // 如果其他工具类都加了 @Component，这里其实只需要这一个手动 Bean
    @Bean
    public FileOperationTool fileOperationTool() {
        return new FileOperationTool();
    }

    /**
     * ✅ 集中注册: 自动收集所有 AgentTool
     * <p>
     * 原理: Spring 会自动将所有实现了 AgentTool 接口的 Bean 注入到 toolList 中。
     * 优势: 以后新增工具，只需让工具类 implements AgentTool，无需修改此处代码。
     */
    @Bean
    public ToolCallback[] allTools(List<AgentTool> toolList) {
        log.info("正在注册 AI 工具，共扫描到 {} 个工具...", toolList.size());

        // 打印一下都注册了谁，方便调试
        toolList.forEach(tool -> log.info("  - 加载工具: {}", tool.getClass().getSimpleName()));

        // List 转 Array，注册给 Spring AI
        return ToolCallbacks.from(toolList.toArray());
    }
}