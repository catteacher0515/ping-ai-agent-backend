package com.pingyu.pingaiagent.config;

import cn.hutool.extra.mail.MailAccount;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 案件 Tool-009: 邮件配置读取器
 * 作用: 将 YAML 配置映射为 Hutool 的 MailAccount 对象
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "email")
public class EmailConfig {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private Boolean ssl = false;
    private Boolean starttls = true;

    @Bean
    public MailAccount mailAccount() {
        MailAccount account = new MailAccount();
        account.setHost(host);
        account.setPort(port);
        account.setAuth(true); // 必须开启验证
        account.setFrom(username);
        account.setUser(username);
        account.setPass(password);
        account.setSslEnable(ssl);
        account.setStarttlsEnable(starttls); // Gmail 587 端口通常需要这个
        return account;
    }
}