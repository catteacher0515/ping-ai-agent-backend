package com.pingyu.pingaiagent.demo.invoke;

public class KeyTest {
    public static void main(String[] args) {
        // 核心代码：从环境变量中读取
        String apiKey = System.getenv("DASHSCOPE_API_KEY");

        System.out.println("--- 环境变量侦查报告 ---");
        if (apiKey != null && !apiKey.isEmpty()) {
            // 安全起见，我们只打印前几位和后几位，防止再次截图泄露
            String maskedKey = apiKey.substring(0, 5) + "******" + apiKey.substring(apiKey.length() - 4);
            System.out.println("✅ 成功捕获 API Key: " + maskedKey);
            System.out.println("侦查结论：环境变量配置无误，可以进行 API 调用。");
        } else {
            System.out.println("❌ 未找到 'DASHSCOPE_API_KEY'。");
            System.out.println("可能原因：");
            System.out.println("1. 忘记重启 IDE 或 终端。");
            System.out.println("2. 变量名拼写错误。");
        }
        System.out.println("-------------------------");
    }
}