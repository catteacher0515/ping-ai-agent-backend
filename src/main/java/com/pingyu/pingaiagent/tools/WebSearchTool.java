package com.pingyu.pingaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 案件 Tool-003: 联网搜索工具
 * <p>
 * 目标: 让 AI 具备“联网”能力,查询实时信息。
 * 实现: 对接 SearchAPI (如 searchapi.io 或 Google Custom Search)
 */
@Slf4j
@Component
public class WebSearchTool {

    private final String apiKey;

    // SearchAPI 的通用端点 (以 searchapi.io 为例，也可以适配 SerpApi)
    private static final String SEARCH_URL = "https://www.searchapi.io/api/v1/search";

    public WebSearchTool(@Value("${search-api.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 执行联网搜索
     *
     * @param query 用户想要搜索的关键词
     * @return 搜索结果摘要 (Top 5)
     */
    @Tool(description = "Search the web for real-time information. Use this when the user asks about current events, news, or specific data not in your memory.")
    public String searchWeb(@ToolParam(description = "The search query (e.g. 'current weather in Tokyo')") String query) {
        try {
            log.info("AI 正在执行联网搜索: {}", query);

            // 1. 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("engine", "google");
            params.put("q", query);
            params.put("api_key", apiKey);

            // 2. 发起 GET 请求 (使用 Hutool)
            String responseStr = HttpUtil.get(SEARCH_URL, params);

            // 3. 解析结果 (提取 organic_results)
            JSONObject json = JSONUtil.parseObj(responseStr);
            if (!json.containsKey("organic_results")) {
                return "No results found or API error.";
            }

            JSONArray results = json.getJSONArray("organic_results");
            StringBuilder report = new StringBuilder("Search Results for '" + query + "':\n");

            // 4. 清洗数据 (Top 5)
            int limit = Math.min(results.size(), 5);
            for (int i = 0; i < limit; i++) {
                JSONObject item = results.getJSONObject(i);
                String title = item.getStr("title");
                String link = item.getStr("link");
                String snippet = item.getStr("snippet");

                report.append(String.format("%d. [%s](%s)\n   Snippet: %s\n\n", i + 1, title, link, snippet));
            }

            return report.toString();

        } catch (Exception e) {
            log.error("联网搜索失败: {}", query, e);
            return "Error searching web: " + e.getMessage();
        }
    }
}