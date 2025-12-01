package com.pingyu.pingaiagent.app;

import com.pingyu.pingaiagent.advisor.MyLoggerAdvisor;
import com.pingyu.pingaiagent.advisor.ReReadingAdvisor;
import com.pingyu.pingaiagent.chatmemory.FileBasedChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 案件 AI-005: LoveApp 多轮对话智能体
 * 核心策略:
 * 1. 使用 InMemoryChatMemory (SOP 2 决策: 快速验证)
 * 2. 记忆窗口限制为 3 (SOP 2 决策: 平衡效率)
 * 3. 集成结构化输出 (SOP 3: 案件 AI-008)
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

    // ... 原有代码 ...

    @Resource
    private org.springframework.ai.vectorstore.VectorStore loveAppVectorStore;


    /**
     * 注入云端 RAG 顾问
     * 注意：使用 @Resource 配合名称注入，避免与其他的 Advisor 冲突
     */
    @jakarta.annotation.Resource
    private org.springframework.ai.chat.client.advisor.api.Advisor loveAppRagCloudAdvisor;

    /**
     * 案件 #009: 云端知识库问答
     * <p>
     * 使用阿里云百炼提供的 DocumentRetriever 进行检索增强。
     *
     * @param message 用户问题
     * @param chatId  会话ID
     * @return 增强后的回答
     */
    public String doChatWithCloudRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(
                        // 1. 挂载云端 RAG 顾问 (核心升级)
                        loveAppRagCloudAdvisor,
                        // 2. 保持日志观察
                        new MyLoggerAdvisor()
                )
                .call()
                .chatResponse();

        String content = "AI 云端链路暂时繁忙...";
        if (chatResponse != null && chatResponse.getResult() != null) {
            content = chatResponse.getResult().getOutput().getText();
        }
        return content;
    }

    /**
     * 案件 #008: RAG 知识库问答
     * <p>
     * 核心逻辑：挂载 QuestionAnswerAdvisor，实现 "检索-增强-生成"。
     *
     * @param message 用户问题
     * @param chatId  会话ID
     * @return 增强后的回答
     */
    public String doChatWithRag(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec
                        // 1. 保持原有的记忆功能 (可选，视需求而定)
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(
                        // 2. (核心) 挂载 RAG 顾问：自动查库、改写 Prompt
                        new org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor(loveAppVectorStore),
                        // 3. 保持日志观察
                        new MyLoggerAdvisor()
                )
                .call()
                .chatResponse();

        String content = "AI 暂时掉线了...";
        if (response != null && response.getResult() != null) {
            content = response.getResult().getOutput().getText();
        }
        return content;
    }

    /**
     * [新增] 定义结构化数据载体 (Java 14 Record 特性)
     * 这是一个不可变的数据类，专门用来接收 AI 的结构化输出。
     * @param title 报告标题
     * @param suggestions 具体的恋爱建议列表
     */
    public record LoveReport(String title, List<String> suggestions) {}

    /**
     * 初始化 ChatClient 并挂载记忆 Advisor
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // SOP 3 执行: 切换为文件持久化记忆 (FileBasedChatMemory)
        ChatMemory chatMemory = new FileBasedChatMemory();

        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                // 核心:挂载记忆拦截器
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory), // 记忆
                        // 坑1(执行顺序): ReReadingAdvisor 必须先于 MyLoggerAdvisor 执行 (order -100 vs 0)
                        new ReReadingAdvisor(),   // <--- 1. 先执行重读 (篡改请求)
                        new MyLoggerAdvisor()     // <--- 2. 再执行日志 (记录篡改后的结果)
                )
                .build();
    }

    /**
     * 执行多轮对话 (返回纯文本)
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

    /**
     * [新增] 结构化输出专用方法
     * 目标: 返回一个强类型的 LoveReport 对象，而不是 String
     *
     * @param message 用户输入
     * @param chatId 会话ID
     * @return 结构化的恋爱报告对象
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        // A. 准备转换器 (指定目标类型为 LoveReport)
        BeanOutputConverter<LoveReport> converter = new BeanOutputConverter<>(LoveReport.class);

        // B. 获取格式指令 (Format Instructions)
        // 这是一段包含了 JSON Schema 的 Prompt，告诉 AI 必须按什么格式返回
        String formatInstructions = converter.getFormat();

        // C. 构造新的 System Prompt
        // 我们把"格式指令"追加到系统 Prompt 后面，确保 AI 能看见
        // 增加"不要包含 Markdown 代码块"的提示，防止 AI 输出 ```json
        String structuredSystemPrompt = SYSTEM_PROMPT + "\n\n" +
                "请务必按照以下格式输出纯 JSON 数据，不要包含任何 Markdown 代码块(```json)或其他解释：\n" + formatInstructions;

        // D. 发起调用
        String jsonResponse = chatClient.prompt()
                .system(structuredSystemPrompt) // 使用包含格式指令的新 System Prompt
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 3))
                .call()
                .content(); // 先拿到原始的 JSON 字符串

        // E. 自动反序列化
        try {
            // BeanOutputConverter 会自动处理 JSON 解析
            LoveReport report = converter.convert(jsonResponse);
            log.info("结构化输出成功: {}", report);
            return report;
        } catch (Exception e) {
            log.error("结构化转换失败，原始响应: {}", jsonResponse, e);
            // 兜底策略: 如果转换失败，返回一个包含原始文本的"空"报告，防止程序崩溃
            return new LoveReport("解析失败", List.of("原始回复: " + jsonResponse));
        }
    }
}