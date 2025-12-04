package com.pingyu.pingaiagent;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 核心修改：排除掉 PGVector 的自动配置类
// 理由：我们已经手动编写了 LoveAppVectorStoreConfig 来处理多模型冲突，不需要系统自动捣乱
@SpringBootApplication(exclude = {PgVectorStoreAutoConfiguration.class})
public class PingAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PingAiAgentApplication.class, args);
    }

}