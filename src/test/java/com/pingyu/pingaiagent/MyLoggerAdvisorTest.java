package com.pingyu.pingaiagent;

import com.pingyu.pingaiagent.advisor.MyLoggerAdvisor;
import jakarta.annotation.Resource; // 注意这里换成了 Resource
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 案件 AI-006: Advisor 靶场验证 (修正版)
 * 目标: 验证 MyLoggerAdvisor 是否能正常拦截并打印日志
 */
@SpringBootTest
class MyLoggerAdvisorTest {

    // 修正点: 使用 @Resource 精确指定 Bean 名称为 "dashscopeChatModel"
    // 你的 pom.xml 同时引入了 ollama 和 alibaba，必须消除歧义
    @Resource(name = "dashscopeChatModel")
    private ChatModel chatModel;

    @Test
    void testLoggerAdvisor() {
        // 1. 现场组装: 临时构建一个挂载了 MyLoggerAdvisor 的 ChatClient
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();

        // 2. 发起突袭
        String message = "你好，测试一下日志拦截器";
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();

        // 3. 验证结果
        System.out.println(">>> 最终响应结果: " + response);
        Assertions.assertNotNull(response, "AI 响应不应为空");
    }
}