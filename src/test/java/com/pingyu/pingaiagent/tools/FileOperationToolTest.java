package com.pingyu.pingaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.pingyu.pingaiagent.constant.FileConstant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

/**
 * 案件 Tool-002: 文件操作单元测试
 * <p>
 * 目标：验证读写逻辑是否闭环，路径隔离是否生效。
 */
@SpringBootTest
class FileOperationToolTest {

    private final FileOperationTool tool = new FileOperationTool();
    private final String testFileName = "test_evidence.txt";

    @Test
    void testWriteAndReadCycle() {
        // 1. 准备数据
        String content = "这是一份关键证据：侦探萍雨到此一游。";

        // 2. 执行写入
        String writeResult = tool.writeFile(testFileName, content);
        System.out.println("写入结果: " + writeResult);

        // 验证写入是否成功 (AI 返回的消息应包含 Success)
        Assertions.assertTrue(writeResult.startsWith("Success"));

        // 物理验证：文件是否真的存在于 tmp 目录
        File file = new File(FileConstant.FILE_SAVE_DIR + File.separator + testFileName);
        Assertions.assertTrue(file.exists(), "文件未物理生成！");

        // 3. 执行读取
        String readContent = tool.readFile(testFileName);
        System.out.println("读取内容: " + readContent);

        // 验证内容一致性
        Assertions.assertEquals(content, readContent);
    }

    @Test
    void testReadNonExistentFile() {
        // 测试读取不存在的文件，应该返回错误提示而不是抛出异常
        String result = tool.readFile("ghost_file.txt");
        System.out.println("读取不存在文件: " + result);
        Assertions.assertTrue(result.startsWith("Error"));
    }

    @AfterEach
    void cleanup() {
//         每次测试后清理战场
        String filePath = FileConstant.FILE_SAVE_DIR + File.separator + testFileName;
        FileUtil.del(filePath);
        System.out.println("战场清理完毕: " + filePath);
    }
}