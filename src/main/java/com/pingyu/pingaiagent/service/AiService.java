package com.pingyu.pingaiagent.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class AiService {

    // 1. 利用 @Value 注解，把 yaml 里的值注入进来
    @Value("${aliyun.dashscope.api-key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        // 2. 验证一下是否注入成功 (仅用于开发阶段调试)
        System.out.println("--- Spring 容器侦查 ---");
        System.out.println("Spring 已成功从 YAML 读取到 Key: " + (apiKey != null && !apiKey.isEmpty()));
    }

    public void callAi() {
        // 3. 以后调用 SDK 时，就把这个 this.apiKey 传进去
        // 例如: Generation.call(params, this.apiKey);
    }
}