package com.pingyu.pingaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 案件 AI-006: 自定义日志 Advisor
 * 目标: 打印 INFO 级别的精简日志 (用户输入 + AI 完整回复)
 * 策略: 使用 MessageAggregator 处理流式响应的聚合
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 优先级控制: 数值越小，优先级越高 (越先执行)
     */
    @Override
    public int getOrder() {
        return 0;
    }

    // --- 核心逻辑: 前置处理 (记录请求) ---
    private AdvisedRequest before(AdvisedRequest request) {
        // 打印 INFO 级别的用户输入
        log.info(">>> User Request: {}", request.userText());
        return request;
    }

    // --- 核心逻辑: 后置处理 (记录响应) ---
    private void observeAfter(AdvisedResponse advisedResponse) {
        // 获取 AI 回复的完整文本
        String aiResponse = advisedResponse.response().getResult().getOutput().getText();
        // 打印 INFO 级别的 AI 回复
        log.info("<<< AI Response: {}", aiResponse);
    }

    // --- 场景 1: 同步对话 (Call) ---
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 1. 前置日志
        advisedRequest = this.before(advisedRequest);

        // 2. 执行调用 (放行)
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

        // 3. 后置日志
        this.observeAfter(advisedResponse);

        return advisedResponse;
    }

    // --- 场景 2: 流式对话 (Stream) ---
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 1. 前置日志
        advisedRequest = this.before(advisedRequest);

        // 2. 执行调用 (获取流)
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);

        // 3. 后置日志 (使用聚合器)
        // 策略关键点: MessageAggregator 会等待流结束，拼凑出完整响应，然后调用 observeAfter
        return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
}