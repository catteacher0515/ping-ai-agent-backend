package com.pingyu.pingaiagent.tools;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class PdfToolTest {

    @Autowired
    private PdfTool pdfTool;

    private String generatedFilePath;

    @Test
    void testGeneratePdf() {
        // 测试内容 (包含中文)
        String content = "恋爱诊断报告\n\n用户表现出轻微的焦虑症状。\n建议多喝热水，保持心情愉快。\nSigned by AI.";
        String fileName = "test_report.pdf";

        String result = pdfTool.generatePdf(content, fileName);
        System.out.println(">>> 生成结果: " + result);

        // 验证
        Assertions.assertTrue(result.endsWith(".pdf"));
        Assertions.assertFalse(result.startsWith("Error"));

        // 物理验证
        File file = new File(result);
        Assertions.assertTrue(file.exists());
        Assertions.assertTrue(file.length() > 0); // 确保文件不是空的

        generatedFilePath = result;
    }

    @AfterEach
    void cleanup() {
        if (generatedFilePath != null) {
            // 在实际开发中，你可能想保留生成的 PDF 看看效果
            // 这里为了自动化测试的纯洁性，还是删了吧。
            // 如果你想看文件，可以把下面这行注释掉
            FileUtil.del(generatedFilePath);
            System.out.println(">>> 测试文件已清理");
        }
    }
}