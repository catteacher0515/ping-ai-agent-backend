package com.pingyu.pingaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailToolTest {

    @Autowired
    private EmailTool emailTool;

    // ✅ 修改: 收件人填你自己，方便查收验证
    private static final String TEST_RECIPIENT = "2726784816@qq.com";

    @Test
    void testSendTextEmail() {
        System.out.println(">>> 开始发送 QQ 邮件...");

        String result = emailTool.sendEmail(
                TEST_RECIPIENT,
                "来自 AI 侦探的测试信 (QQ SMTP)",
                "你好！\n\n当你收到这就说明 Tool-009 (邮件工具) 彻底跑通了！\n\nBy PingAiAgent",
                null
        );

        System.out.println(">>> 发送结果: " + result);

        // 断言
        Assertions.assertTrue(result.contains("successfully"), "发送失败: " + result);
    }
}