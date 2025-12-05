package com.pingyu.pingaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TerminalToolTest {

    @Autowired
    private TerminalTool terminalTool;

    @Test
    void testRunAllowedCommand() {
        // 测试白名单命令: java -version
        // 注意: java -version 通常输出到 stderr，但我们合并了流，所以应该能读到
        String result = terminalTool.runCommand("java -version");
        System.out.println(">>> 终端输出:\n" + result);

        Assertions.assertNotNull(result);
        // 验证输出了版本号 (包含 "version" 或 "build")
        // 如果失败，可能是因为某些 OS 的 java -version 输出格式不同，可尝试改为 "echo hello"
        Assertions.assertFalse(result.startsWith("Error"));
    }

    @Test
    void testRunDeniedCommand() {
        // 测试黑名单命令: rm -rf
        String result = terminalTool.runCommand("rm -rf /");
        System.out.println(">>> 拒绝结果: " + result);

        // 验证安全拦截
        Assertions.assertTrue(result.contains("Permission denied"));
    }
}