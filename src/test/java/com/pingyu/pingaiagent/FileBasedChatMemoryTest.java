package com.pingyu.pingaiagent;

import com.pingyu.pingaiagent.chatmemory.FileBasedChatMemory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 案件 AI-009: 记忆持久化验证
 * 目标: 验证“断电”（实例化新对象）后，能否从磁盘读取旧记忆。
 */
class FileBasedChatMemoryTest {

    // 生成一个临时的测试 ID，避免污染真实数据
    private final String testConversationId = "test-" + UUID.randomUUID().toString();
    private final String expectedFilePath = "chat_memory/" + testConversationId + ".bin";

    @Test
    void testPersistenceAndReload() {
        // --- 第一阶段: 模拟“前世” (写入记忆) ---
        System.out.println(">>> 阶段 1: 写入记忆...");
        FileBasedChatMemory memoryInstance1 = new FileBasedChatMemory();
        List<Message> history1 = List.of(
                new UserMessage("我叫萍雨，记住我的名字。"),
                new AssistantMessage("好的，萍雨，我已经记在小本本上了。")
        );
        memoryInstance1.add(testConversationId, history1);

        // 验证文件是否物理存在
        File file = Paths.get(expectedFilePath).toFile();
        Assertions.assertTrue(file.exists(), "错误: 记忆文件未生成! path: " + expectedFilePath);
        System.out.println(">>> 验证成功: 物理文件已生成 -> " + file.getAbsolutePath());

        // --- 第二阶段: 模拟“今生” (重启/新建实例读取) ---
        System.out.println(">>> 阶段 2: 模拟重启，读取记忆...");
        // 关键点: 这里必须要 new 一个新的实例，模拟服务器重启后的状态
        FileBasedChatMemory memoryInstance2 = new FileBasedChatMemory();

        // 获取记忆
        List<Message> reloadedHistory = memoryInstance2.get(testConversationId, 10);

        // --- 第三阶段: 审计对比 ---
        Assertions.assertEquals(2, reloadedHistory.size(), "错误: 读取到的记忆条数不匹配!");
        Assertions.assertTrue(reloadedHistory.get(0) instanceof UserMessage);
        Assertions.assertEquals("我叫萍雨，记住我的名字。", reloadedHistory.get(0).getText());

        System.out.println(">>> 最终验证: 重启后成功读取到 " + reloadedHistory.size() + " 条记忆。");
        System.out.println(">>> 内容: " + reloadedHistory.get(0).getText());
    }

    @AfterEach
    void cleanup() {
        // 测试结束后，清理战场，删除生成的临时文件
        FileBasedChatMemory memory = new FileBasedChatMemory();
        memory.clear(testConversationId);
        System.out.println(">>> 战场清理完毕: 已删除测试文件 " + testConversationId);
    }
}