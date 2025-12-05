package com.pingyu.pingaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebScraperToolTest {

    @Autowired
    private WebScraperTool scraperTool;

    @Test
    void testScrapeValidUrl() {
        // 测试抓取百度首页 (简单且稳定)
        String url = "https://www.baidu.com";
        String result = scraperTool.scrapeUrl(url);

        System.out.println(">>> 抓取结果片段: " + result.substring(0, Math.min(result.length(), 100)));

        Assertions.assertNotNull(result);
        // 百度首页通常会有 "百度" 或 "关于百度" 等字样
        Assertions.assertTrue(result.contains("百度") || result.contains("Baidu"));
        // 确保没有 HTML 标签残留 (简单检查)
        Assertions.assertFalse(result.contains("</div>"));
    }

    @Test
    void testScrapeInvalidUrl() {
        // 测试一个不存在的 URL
        String url = "https://this-site-does-not-exist-12345.com";
        String result = scraperTool.scrapeUrl(url);

        System.out.println(">>> 错误处理结果: " + result);

        // 验证是否优雅返回了 Error 信息
        Assertions.assertTrue(result.startsWith("Error:"));
    }
}