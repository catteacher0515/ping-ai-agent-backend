package com.pingyu.pingaiagent.constant;

import java.io.File;

/**
 * 案件 Tool-002: 文件操作常量定义
 * <p>
 * 作用：定义文件的安全存储根目录，防止 AI 随意读写系统文件。
 */

public interface FileConstant {

    /**
     * 文件保存根目录
     * 路径：项目根目录/tmp
     * 警告：所有 AI 生成的文件必须限制在此目录下
     */
    String FILE_SAVE_DIR = System.getProperty("user.dir") + File.separator + "tmp";
}