package com.pingyu.pingaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 案件 Tool-009: 邮件发送工具
 * <p>
 * 目标: 允许 AI 向指定邮箱发送文本或附件。
 * 架构: 基于 Hutool MailUtil + Jakarta Mail。
 */
@Slf4j
@Component
public class EmailTool implements AgentTool {

    private final MailAccount mailAccount;

    public EmailTool(MailAccount mailAccount) {
        this.mailAccount = mailAccount;
    }

    /**
     * 发送邮件
     *
     * @param to       收件人
     * @param subject  标题
     * @param body     正文
     * @param filePath 附件路径 (可选)
     * @return 发送结果
     */
    @Tool(description = "Send an email to a recipient. You can optionally attach a file by providing its local file path.")
    public String sendEmail(
            @ToolParam(description = "The recipient email address") String to,
            @ToolParam(description = "The email subject") String subject,
            @ToolParam(description = "The email body content") String body,
            @ToolParam(description = "The local file path of the attachment (optional)") String filePath) {

        try {
            log.info("AI 正在发送邮件给: {}", to);

            // 1. 检查附件
            if (StrUtil.isNotBlank(filePath)) {
                File file = new File(filePath);
                if (!file.exists()) {
                    return "Error: Attachment file not found at " + filePath;
                }
                // 发送带附件
                MailUtil.send(mailAccount, to, subject, body, false, file);
                log.info("邮件发送成功 (带附件): {}", filePath);
            } else {
                // 发送纯文本
                MailUtil.send(mailAccount, to, subject, body, false);
                log.info("邮件发送成功 (纯文本)");
            }

            return "Email sent successfully to " + to;

        } catch (Exception e) {
            log.error("邮件发送失败", e);
            return "Error sending email: " + e.getMessage();
        }
    }
}