package com.pingyu.pingaiagent.app;

import com.pingyu.pingaiagent.advisor.MyLoggerAdvisor;
import com.pingyu.pingaiagent.advisor.ReReadingAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 案件 AI-005: LoveApp 多轮对话智能体
 * 核心策略:
 * 1. 使用 InMemoryChatMemory (SOP 2 决策: 快速验证)
 * 2. 记忆窗口限制为 3 (SOP 2 决策: 平衡效率)
 */
@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    // SOP 1 蓝图定义: 系统 Prompt - 恋爱心理专家
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份,告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问:单身状态询问社交圈拓展及追求心仪对象的困扰;" +
            "恋爱状态询问沟通、习惯差异引发的矛盾;已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法,以便给出专属解决方案。";

    /**
     * 初始化 ChatClient 并挂载记忆 Advisor
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // SOP 2 决策: 暂时使用内存版记忆
        ChatMemory chatMemory = new InMemoryChatMemory();

        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                // 核心: 挂载记忆拦截器
                .defaultAdvisors(
                    new MessageChatMemoryAdvisor(chatMemory), // 记忆
                    new ReReadingAdvisor(), // <--- 1. 先执行重读 (篡改请求)
                    new MyLoggerAdvisor()   // <--- 2. 再执行日志 (记录篡改后的结果)
                )
                .build();
    }

    /**
     * 执行多轮对话
     *
     * @param message 用户输入
     * @param chatId  会话ID (用于隔离不同用户的记忆)
     * @return AI 回复
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec
                        // 传入会话ID，定位特定用户的"日记本"
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        // SOP 2 独特配置: 只检索最近 3 条记忆
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 3))
                .call()
                .chatResponse();

        String content = "AI 暂时掉线了..."; // 默认值
        if (response != null && response.getResult() != null) {
            content = response.getResult().getOutput().getText();
        }
        log.info("ChatId: {}, User: {}, AI: {}", chatId, message, content);
        return content;
    }
}