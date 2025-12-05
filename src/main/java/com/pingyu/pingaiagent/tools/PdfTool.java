package com.pingyu.pingaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.pingyu.pingaiagent.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 案件 Tool-007: PDF 生成工具
 * <p>
 * 目标: 将 AI 的文本回复固化为 PDF 文件。
 * 核心: OpenPDF + 本地字体 (解决乱码)。
 */
@Slf4j
@Component
public class PdfTool {

    // SOP 1 决策: 指定字体文件名 (必须与 resources/fonts/ 下的文件名一致)
    private static final String FONT_NAME = "simhei.ttf";

    /**
     * 生成 PDF 文件
     *
     * @param content  文本内容
     * @param fileName 保存文件名 (可选)
     * @return 绝对路径
     */
    @Tool(description = "Generate a PDF file from text content. Returns the local file path.")
    public String generatePdf(
            @ToolParam(description = "The text content to write into the PDF") String content,
            @ToolParam(description = "The filename to save as (optional)") String fileName) {

        try {
            log.info("AI 正在生成 PDF...");

            // 1. 处理文件名
            if (StrUtil.isBlank(fileName)) {
                fileName = "report_" + IdUtil.fastSimpleUUID() + ".pdf";
            }
            if (!fileName.endsWith(".pdf")) {
                fileName += ".pdf";
            }
            // 安全清洗
            fileName = FileUtil.getName(fileName);

            // 2. 构造路径
            File saveDir = new File(FileConstant.FILE_SAVE_DIR);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            File destFile = new File(saveDir, fileName);

            // 3. 加载字体 (核心!)
            // 使用 Spring 的 ClassPathResource 读取 jar 包内的字体
            ClassPathResource fontResource = new ClassPathResource("fonts/" + FONT_NAME);
            if (!fontResource.exists()) {
                return "Error: Font file '" + FONT_NAME + "' not found in resources.";
            }
            // BaseFont.IDENTITY_H 是横排中文的关键参数
            BaseFont baseFont = BaseFont.createFont(fontResource.getURL().toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            // 定义两种样式: 标题(大), 正文(小)
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font bodyFont = new Font(baseFont, 12, Font.NORMAL);

            // 4. 创建文档
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(destFile));
            document.open();

            // 5. 排版 (流式写入)
            String[] lines = content.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) {
                    continue; // 跳过空行
                }

                // 简单的排版策略: 第一行默认为标题
                Paragraph paragraph;
                if (i == 0) {
                    paragraph = new Paragraph(line, titleFont);
                    paragraph.setAlignment(Paragraph.ALIGN_CENTER); // 标题居中
                    paragraph.setSpacingAfter(20f); // 标题后留白
                } else {
                    paragraph = new Paragraph(line, bodyFont);
                    paragraph.setSpacingAfter(5f); // 行间距
                }
                document.add(paragraph);
            }

            document.close();
            log.info("PDF 生成成功: {}", destFile.getAbsolutePath());
            return destFile.getAbsolutePath();

        } catch (Exception e) {
            log.error("PDF 生成失败", e);
            return "Error generating PDF: " + e.getMessage();
        }
    }
}