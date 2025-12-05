package com.pingyu.pingaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.pingyu.pingaiagent.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 案件 Tool-006: 资源下载工具
 * <p>
 * 目标: 安全地下载网络资源到本地。
 * 核心防御: 路径穿越清洗、大小熔断机制 (50MB)。
 */
@Slf4j
@Component
public class ResourceTool implements AgentTool{

    // SOP 1 决策: 硬性大小限制 50MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    /**
     * 下载文件
     *
     * @param url      资源地址
     * @param fileName 保存的文件名 (可选)
     * @return 本地路径或错误信息
     */
    @Tool(description = "Download a file from a URL to local storage. Returns the local file path.")
    public String downloadFile(
            @ToolParam(description = "The URL of the resource") String url,
            @ToolParam(description = "The filename to save as (optional)") String fileName) {

        File tempFile = null;
        try {
            log.info("AI 正在尝试下载: {}", url);

            // 1. 文件名处理 (SOP 1 逻辑1)
            if (StrUtil.isBlank(fileName)) {
                // 尝试从 URL 提取
                fileName = URLUtil.getURL(url).getPath();
                fileName = FileUtil.getName(fileName); // 再次清洗，防患未然
                if (StrUtil.isBlank(fileName)) {
                    fileName = IdUtil.fastSimpleUUID(); // 保底策略
                }
            } else {
                // 2. 路径穿越防御 (SOP 1 逻辑2)
                fileName = FileUtil.getName(fileName);
            }

            // 3. 构造保存路径
            File saveDir = new File(FileConstant.FILE_SAVE_DIR);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            File destFile = new File(saveDir, fileName);

            // 4. 重名检查 (SOP 1 逻辑3)
            if (destFile.exists()) {
                return "Error: File '" + fileName + "' already exists. Please choose a different name.";
            }

            tempFile = destFile; // 标记，以便出错时删除

            // 5. 限流下载 (SOP 1 逻辑4 - 关键!)
            // 我们不能用简单的 downloadFile，必须手动控制流
            HttpResponse response = HttpUtil.createGet(url).execute();
            if (!response.isOk()) {
                return "Error: Download failed. HTTP Status: " + response.getStatus();
            }

            long totalBytesRead = 0;
            try (InputStream in = response.bodyStream();
                 OutputStream out = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[8192]; // 8KB buffer
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // 熔断检查
                    if (totalBytesRead > MAX_FILE_SIZE) {
                        // 抛出异常触发 finally 清理
                        throw new RuntimeException("File size exceeds the limit of 50MB.");
                    }
                }
            }

            log.info("下载成功: {}", destFile.getAbsolutePath());
            return destFile.getAbsolutePath();

        } catch (Exception e) {
            log.error("下载失败: {}", url, e);
            // 清理碎片文件
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
            }
            return "Error: Download failed. Reason: " + e.getMessage();
        }
    }
}