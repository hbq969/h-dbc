package com.github.hbq969.middleware.dbc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "login")
@Component
@Data
public class Config {
    /**
     * 是否启用登录功能
     */
    private boolean enabled;

    /**
     * cookie最大存活时间（秒）
     */
    private long cookieMaxAgeSec = 1800;

    /**
     * 无需校验会话的url
     */
    private List<String> excludeUrls;

    /**
     * 数据库dialect，默认mysql，可支持oracle
     */
    private String dialect = "mysql";
}
