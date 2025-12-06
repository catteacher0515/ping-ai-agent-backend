package com.pingyu.pingaiagent;

import com.pingyu.pingaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * 案件 AI-005: 靶场验证 (L2 Test)
 * 目标: 验证多轮对话记忆是否生效
 */
@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChatWithMcp() {
        String chatId = "test-mcp-001";
        // 这是一个只有连上高德 MCP 才能回答的问题
        String message = "我在北京天安门，请帮我找一家最近的必胜客，告诉我地址和距离。";

        System.out.println(">>> User: " + message);
        String answer = loveApp.doChatWithMcp(message, chatId);
        System.out.println(">>> AI (MCP): " + answer);

        // 断言：如果成功，回答中应该包含具体的地址或距离数字
        Assertions.assertNotNull(answer);
        // 简单验证是否包含数字 (距离) 或 "必胜客"
        Assertions.assertTrue(answer.contains("必胜客") || answer.contains("公里") || answer.contains("米"));
    }

    @Test
    void testChat() {
        // 生成随机 chatId，模拟一个全新用户
        String chatId = UUID.randomUUID().toString();

        // --- 第一轮: 建立连接 ---
        String message1 = "你好，现在几点了";
        String answer1 = loveApp.doChat(message1, chatId);
        System.out.println(">>> Round 1 AI: " + answer1);
        Assertions.assertNotNull(answer1);

        // --- 第二轮: 注入记忆 (关键测试点) ---
        String message2 = "我是侦探萍雨，正在测试你的记忆力";
        String answer2 = loveApp.doChat(message2, chatId);
        System.out.println(">>> Round 2 AI: " + answer2);
        Assertions.assertNotNull(answer2);

        // --- 第三轮: 验证记忆 ---
        // 核心断言: AI 必须能叫出我的名字
        String message3 = "我是谁？";
        String answer3 = loveApp.doChat(message3, chatId);
        System.out.println(">>> Round 3 AI: " + answer3);

        Assertions.assertNotNull(answer3);
        // 验证它是否记住了 Round 2 的信息
        boolean hasMemory = answer3.contains("萍雨");
        System.out.println(">>> 记忆验证结果: " + (hasMemory ? "通过 (Green)" : "失败 (Red)"));
        Assertions.assertTrue(hasMemory, "AI 竟然忘记了侦探的名字！(Memory Fail)");
    }

    @Test
    void testChatWithReport() {
        String chatId = "test-report-001";
        String message = "我暗恋隔壁班的女生，经常偷看她但不敢说话，我该怎么办？";

        // 调用结构化接口
        LoveApp.LoveReport report = loveApp.doChatWithReport(message, chatId);

        // 验证结果
        System.out.println(">>> 报告标题: " + report.title());
        System.out.println(">>> 建议列表: " + report.suggestions());

        Assertions.assertNotNull(report);
        Assertions.assertNotNull(report.title());
        Assertions.assertTrue(report.suggestions().size() > 0);
    }

    @Test
    void testChatWithRag() {
        // 准备：请确保 resources/document 下有关于 "单身" 或 "已婚" 的 md 文档
        String chatId = "test-rag-001";
        // 提问一个必须依赖知识库才能回答的具体问题
        String message = "我今年30岁还是单身，家里催得急，但我有点社恐，该怎么办？";

        System.out.println(">>> User: " + message);
        String answer = loveApp.doChatWithRag(message, chatId);

        System.out.println(">>> AI (RAG): " + answer);
        Assertions.assertNotNull(answer);
        // 如果文档里有推荐课程链接，可以断言 answer.contains("codefather.cn")
    }

    @Test
    void testChatWithCloudRag() {
        // 前置条件：请确保你已经在阿里云百炼控制台创建了名为 "恋爱大师" 的知识库并导入了数据
        String chatId = "test-cloud-rag-001";
        String message = "既然是恋爱大师，请告诉我如何处理异地恋的信任危机？";

        System.out.println(">>> User (Cloud): " + message);

        // 调用云端 RAG 接口
        String answer = loveApp.doChatWithCloudRag(message, chatId);

        System.out.println(">>> AI (Cloud RAG): " + answer);
        Assertions.assertNotNull(answer);
    }
}