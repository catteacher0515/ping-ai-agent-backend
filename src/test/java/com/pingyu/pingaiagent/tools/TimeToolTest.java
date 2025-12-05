package com.pingyu.pingaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TimeToolTest {

    @Autowired
    private TimeTool timeTool;

    @Test
    void testGetCurrentTimeDefault() {
        String time = timeTool.getCurrentTime(null);
        System.out.println(">>> 默认时区时间: " + time);

        Assertions.assertNotNull(time);
        // 验证包含中文星期 (例如 "星期")
        Assertions.assertTrue(time.contains("星期"));
    }

    @Test
    void testGetCurrentTimeLondon() {
        String time = timeTool.getCurrentTime("Europe/London");
        System.out.println(">>> 伦敦时间: " + time);

        Assertions.assertNotNull(time);
        // 验证时区后缀 (GMT 或 BST)
        Assertions.assertTrue(time.contains("GMT") || time.contains("BST"));
    }

    @Test
    void testDateDiff() {
        // 测试过去的日期
        String pastResult = timeTool.dateDiff("2000-01-01");
        System.out.println(">>> 过去日期: " + pastResult);
        Assertions.assertTrue(pastResult.contains("已过去"));

        // 测试未来的日期 (假设 2099 年)
        String futureResult = timeTool.dateDiff("2099-01-01");
        System.out.println(">>> 未来日期: " + futureResult);
        Assertions.assertTrue(futureResult.contains("还有"));
    }
}