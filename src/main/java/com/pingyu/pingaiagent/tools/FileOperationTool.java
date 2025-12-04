package com.pingyu.pingaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.pingyu.pingaiagent.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 案件 Tool-002: 文件操作工具
 * <p>
 * 核心能力：
 * 1. writeFile: 将文本写入本地文件
 * 2. readFile: 读取本地文件内容
 * <p>
 * 安全约束：所有操作限制在 FileConstant.FILE_SAVE_DIR 目录下
 */
@Slf4j
public class FileOperationTool {

    /**
     * 将内容写入文件
     *
     * @param fileName 文件名（不包含路径）
     * @param content  要写入的文本内容
     * @return 操作结果描述
     */
    @Tool(description = "Write content to a file. Useful when you need to save text, code, or reports locally.")
    public String writeFile(
            @ToolParam(description = "The name of the file (e.g., 'love_report.txt')") String fileName,
            @ToolParam(description = "The text content to write into the file") String content) {

        // 安全防御：强制拼接根目录，防止路径穿越
        String filePath = FileConstant.FILE_SAVE_DIR + File.separator + fileName;

        try {
            // 确保父目录存在
            FileUtil.touch(filePath);
            // 写入内容 (覆盖模式，使用 UTF-8)
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);

            log.info("AI 成功写入文件: {}", filePath);
            return "Success: File saved to " + fileName;
        } catch (Exception e) {
            log.error("AI 写入文件失败: {}", filePath, e);
            // 异常兜底：返回友好提示，不抛出异常打断对话
            return "Error writing file: " + e.getMessage();
        }
    }

    /**
     * 读取文件内容
     *
     * @param fileName 文件名
     * @return 文件内容或错误信息
     */
    @Tool(description = "Read content from a file. Useful when you need to retrieve past records or analyze local files.")
    public String readFile(
            @ToolParam(description = "The name of the file to read") String fileName) {

        // 安全防御：强制拼接根目录
        String filePath = FileConstant.FILE_SAVE_DIR + File.separator + fileName;

        try {
            if (!FileUtil.exist(filePath)) {
                return "Error: File '" + fileName + "' does not exist.";
            }

            // 读取内容
            String content = FileUtil.readString(filePath, StandardCharsets.UTF_8);
            log.info("AI 成功读取文件: {}", filePath);
            return content;
        } catch (Exception e) {
            log.error("AI 读取文件失败: {}", filePath, e);
            return "Error reading file: " + e.getMessage();
        }
    }
}