package com.pingyu.pingaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 案件 Tool-004: 网页抓取工具 (Web Scraper)
 * <p>
 * 目标: 赋予 AI "阅读" 网页详情的能力。
 * 核心逻辑: 访问 URL -> 移除噪音标签 (script/style) -> 提取纯文本 -> 截断过长内容。
 */
@Slf4j
@Component
public class WebScraperTool implements AgentTool{

    // SOP 1 决策: 硬性截断阈值，防止 Token 爆炸
    private static final int MAX_CHARS = 4000;

    /**
     * 抓取指定 URL 的文本内容
     *
     * @param url 目标网页地址
     * @return 清洗后的网页文本
     */
    @Tool(description = "Scrape and read the text content of a specific webpage URL. Use this when you need to read a full article, documentation, or news report.")
    public String scrapeUrl(@ToolParam(description = "The URL of the webpage to read (e.g. https://example.com/article)") String url) {
        try {
            log.info("AI 正在尝试抓取网页: {}", url);

            // 1. 发起请求并解析 DOM (设置 10秒超时)
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 2. 清洗噪音 (SOP 1 挑战2)
            // 移除 脚本、样式、导航、页脚、头部元数据
            doc.select("script, style, nav, footer, head, iframe, meta").remove();

            // 3. 提取纯文本
            // doc.body().text() 会智能处理换行和空格
            String text = doc.body().text();

            // 4. 截断保护 (SOP 1 挑战3)
            if (text.length() > MAX_CHARS) {
                log.warn("网页内容过长 ({} 字符)，执行截断。", text.length());
                return text.substring(0, MAX_CHARS) + "\n...(content truncated because it's too long)";
            }

            return text;

        } catch (Exception e) {
            log.error("网页抓取失败: {} - {}", url, e.getMessage());
            // 优雅降级: 返回错误信息给 AI，而不是抛出异常打断对话
            return "Error: Unable to access the webpage. Reason: " + e.getMessage();
        }
    }
}