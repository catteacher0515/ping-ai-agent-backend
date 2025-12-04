package com.pingyu.pingaiagent.config;

import com.pingyu.pingaiagent.tools.FileOperationTool;
import com.pingyu.pingaiagent.tools.WebSearchTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    // 1. 注册文件工具 (保持不变)
    @Bean
    public FileOperationTool fileOperationTool() {
        return new FileOperationTool();
    }

    // 2. [新增] 注册搜索工具 (Spring 会自动注入 application.yml 里的 api-key)
    // 注意：WebSearchTool 类上必须有 @Component 注解
    // 如果你没有在 WebSearchTool 加 @Component，这里就需要手动 new WebSearchTool(apiKey)
    // 建议去检查一下 WebSearchTool.java，确保类名上面有一行 @Component

    /**
     * 3. [关键修改] 打包所有工具
     * 将 fileOperationTool 和 webSearchTool 一起打包成一个数组
     * 我们给这个 Bean 起个新名字叫 "allTools"
     */
    @Bean
    public ToolCallback[] allTools(FileOperationTool fileTool, WebSearchTool searchTool) {
        // ToolCallbacks.from(...) 可以接受多个参数，把它们统统变成 AI 能用的工具
        return ToolCallbacks.from(fileTool, searchTool);
    }
}