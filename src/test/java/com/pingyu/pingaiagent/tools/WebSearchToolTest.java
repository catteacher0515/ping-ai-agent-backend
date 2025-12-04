package com.pingyu.pingaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebSearchToolTest {

    @Autowired
    private WebSearchTool webSearchTool;

    @Test
    @Disabled("需配置真实 SearchAPI Key 才能运行，否则会报错或消耗额度")
    void testSearchWeb() {
        // 1. 准备关键词
        String query = "Spring AI Alibaba 最新版本";

        // 2. 执行搜索
        String result = webSearchTool.searchWeb(query);

        // 3. 验证结果
        System.out.println(">>> 搜索结果:\n" + result);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.startsWith("Error"));
        // 验证是否包含 Markdown 链接格式 (简单验证清洗逻辑)
        Assertions.assertTrue(result.contains("http"), "结果应包含链接");
    }
}