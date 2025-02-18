package com.github.hbq969.middleware.dbc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "dbc")
@Component
@Data
public class Config {
    /**
     * 是否开启自动备份
     */
    private boolean backup = true;
}
