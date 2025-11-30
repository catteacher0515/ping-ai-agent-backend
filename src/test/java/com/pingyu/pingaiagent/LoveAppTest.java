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
    void testChat() {
        // 生成随机 chatId，模拟一个全新用户
        String chatId = UUID.randomUUID().toString();

        // --- 第一轮: 建立连接 ---
        String message1 = "你好";
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
}