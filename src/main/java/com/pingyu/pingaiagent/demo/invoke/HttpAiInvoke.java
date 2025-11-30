package com.pingyu.pingaiagent.demo.invoke;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class HttpAiInvoke{
    public static void main(String[] args) {
        // 1. 准备案发现场：目标 URL
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 2. 提取证物：API Key (从环境变量读取，安全第一)
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null) {
            System.err.println("❌ 警告：未找到环境变量 DASHSCOPE_API_KEY");
            return;
        }

        // 3. 组装弹药：构造那个复杂的 JSON Body
        // 这里的逻辑就是一层层把洋葱包起来

        // 3.1 构造 messages 数组里的对象
        JSONObject systemMsg = JSONUtil.createObj()
                .set("role", "system")
                .set("content", "You are a helpful assistant.");

        JSONObject userMsg = JSONUtil.createObj()
                .set("role", "user")
                .set("content", "你好,我是程序员花萍雨"); // <--- 以后这里可以换成变量

        // 3.2 构造 input 对象
        JSONObject input = JSONUtil.createObj()
                .set("messages", JSONUtil.createArray().put(systemMsg).put(userMsg));

        // 3.3 构造 parameters 对象
        JSONObject parameters = JSONUtil.createObj()
                .set("result_format", "message");

        // 3.4 最终合体：最外层的 JSON
        JSONObject requestBody = JSONUtil.createObj()
                .set("model", "qwen-plus")
                .set("input", input)
                .set("parameters", parameters);

        // 4. 发起突袭：发送 POST 请求
        System.out.println("🚀 正在发送请求给通义千问...");

        String result = HttpRequest.post(url)
                // 对应 curl 的 --header "Authorization: Bearer $KEY"
                // 注意：Bearer 后面必须有一个空格！
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                // 对应 curl 的 --header "Content-Type: application/json"
                .header(Header.CONTENT_TYPE, "application/json")
                // 对应 curl 的 --data
                .body(requestBody.toString())
                .timeout(20000) // 建议设置超时时间，防止网络卡死
                .execute()
                .body();

        // 5. 审讯结果
        System.out.println("✅ 收到回复：");
        System.out.println(result);
    }
}