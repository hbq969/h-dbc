package com.github.hbq969.middleware.dbc.driver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.config.h-dbc.api.basic-auth")
@Data
public class BasicAuth {
    private String key = "Authorization";
    private String username;
    private String password;

    public void check() {
        if (username == null || password == null) {
            throw new IllegalArgumentException("缺少basic-auth的账号、密码");
        }
    }
}
