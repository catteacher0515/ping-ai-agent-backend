package com.pingyu.pingaiagent.config;

import com.pingyu.pingaiagent.tools.FileOperationTool;
import com.pingyu.pingaiagent.tools.TerminalTool;
import com.pingyu.pingaiagent.tools.WebScraperTool;
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
     * ✅ 唯一合法的全家桶 Bean
     * 包含所有工具：File + Search + Scraper + Terminal
     */
    @Bean
    public ToolCallback[] allTools(FileOperationTool fileTool,
                                   WebSearchTool searchTool,
                                   WebScraperTool scraperTool,
                                   TerminalTool terminalTool) { // <--- 注入新工具

        // 将四个工具打包在一起
        return ToolCallbacks.from(fileTool, searchTool, scraperTool, terminalTool);
    }
}