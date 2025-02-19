package com.github.hbq969.middleware.dbc.driver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.config.h-dbc.api.auth.basic")
@Data
public class Basic {
    private String key="Authorization";
    private String username;
    private String password;
}
