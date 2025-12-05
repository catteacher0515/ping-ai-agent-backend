package com.pingyu.pingaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 案件 Tool-005: 终端操作工具 (Terminal Tool)
 * <p>
 * 目标: 允许 AI 执行特定的系统命令 (如 git status, java -version)。
 * 安全核心: 严格白名单机制 (Strict Allowlist)，防止 rm -rf 等危险操作。
 */
@Slf4j
@Component
public class TerminalTool {

    // SOP 1 决策: 严格白名单，严禁 rm, format, shutdown
    private static final List<String> ALLOWED_COMMANDS = List.of(
            "git status",
            "git log",
            "java -version",
            "mvn -v",
            "ls",   // Linux/Mac
            "dir",  // Windows
            "echo"  // 测试用
    );

    /**
     * 在终端执行命令
     *
     * @param command 要执行的命令
     * @return 执行结果 (stdout + stderr)
     */
    @Tool(description = "Execute a terminal command to check system status or project info. Only specific safe commands are allowed.")
    public String runCommand(@ToolParam(description = "The command to execute (e.g. 'git status')") String command) {
        // 1. 安全安检 (SOP 1 挑战2)
        boolean isAllowed = ALLOWED_COMMANDS.stream().anyMatch(command::startsWith);
        if (!isAllowed) {
            log.warn("AI 试图执行非法命令: {}", command);
            return "Error: Permission denied. Command '" + command + "' is not in the allowlist.";
        }

        try {
            log.info("AI 执行终端命令: {}", command);

            // 2. OS 适配 (SOP 1 挑战1)
            ProcessBuilder processBuilder = new ProcessBuilder();
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                processBuilder.command("cmd.exe", "/c", command);
            } else {
                processBuilder.command("/bin/sh", "-c", command);
            }

            // 3. 锁定工作目录 (SOP 1 挑战3)
            // 锁定在项目根目录，以便执行 git/mvn 命令
            processBuilder.directory(new File(System.getProperty("user.dir")));

            // 4. 合并错误流 (SOP 1 决策)
            // 这样 stderr 也会被 redirect 到 inputStream 中，方便统一读取
            processBuilder.redirectErrorStream(true);

            // 5. 启动进程
            Process process = processBuilder.start();

            // 6. 读取输出流
            // 注意: ProcessBuilder 的输出流编码通常取决于 OS，但在 Java 18+ 默认 UTF-8
            // 这里的 Charset 可能需要根据实际环境调整 (Windows 中文版可能是 GBK)
            // 为了通用性，我们先尝试 UTF-8，如果乱码再调
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待执行结束 (设置 5秒超时防止死锁)
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return output.append("\n[Timeout] Command execution timed out.").toString();
            }

            return output.length() > 0 ? output.toString() : "[Success] (No output returned)";

        } catch (Exception e) {
            log.error("命令执行失败: {}", command, e);
            return "Error executing command: " + e.getMessage();
        }
    }
}