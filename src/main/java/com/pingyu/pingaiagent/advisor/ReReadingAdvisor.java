package com.pingyu.pingaiagent.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 案件 AI-007: 自定义 Re-Reading Advisor (Re2) - 修正版
 * 修复: 手动执行字符串替换，解决占位符不生效的问题
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        // 保持高优先级，先于日志执行
        return -100;
    }

    // --- 核心逻辑: 篡改用户请求 ---
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        // 1. 获取原始用户输入
        String originalInput = advisedRequest.userText();

        // 2. 构造重读模板 (直接在 Java 层拼接，不依赖框架的自动替换)
        // 格式: {原文本} \n Read the question again: {原文本}
        String re2Prompt = originalInput + "\nRead the question again: " + originalInput;

        // 3. 构建新的请求
        // 注意: 我们不再需要把 userParams 塞进去了，因为我们已经手动替换完了
        return AdvisedRequest.from(advisedRequest)
                .userText(re2Prompt)
                .build();
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }
}