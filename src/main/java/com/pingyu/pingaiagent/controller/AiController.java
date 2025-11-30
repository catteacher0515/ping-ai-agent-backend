package com.pingyu.pingaiagent.controller;

import com.pingyu.pingaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 案件 AI-005: 前端连接器
 * 作用: 将 LoveApp 暴露为 HTTP 接口
 */
@RestController
@RequestMapping("/ai")
// 核心: 允许跨域，否则前端 HTML 无法访问
@CrossOrigin(origins = "*")
public class AiController {

    @Resource
    private LoveApp loveApp;

    /**
     * 对话接口
     * POST /api/ai/chat
     * Body: { "message": "你好", "chatId": "uuid..." }
     */
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String chatId = request.get("chatId");

        // 调用我们之前写的核心业务
        String answer = loveApp.doChat(message, chatId);

        return Map.of("answer", answer);
    }
}