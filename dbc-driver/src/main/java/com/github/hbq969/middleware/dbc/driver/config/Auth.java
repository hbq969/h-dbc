package com.github.hbq969.middleware.dbc.driver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.config.h-dbc.api.auth")
@Data
public class Auth {
    private boolean enabled = true;
    private Basic basic=new Basic();
}
