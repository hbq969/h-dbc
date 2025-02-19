package com.github.hbq969.middleware.dbc.config;

import com.google.common.collect.Lists;
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

    /**
     * 认证信息
     */
    private Auth auth=new Auth();

    @ConfigurationProperties(prefix = "dbc.auth")
    @Data
    public static class Auth{

        /**
         * 开启认证
         */
        private boolean enabled=true;

        private Basic basic=new Basic();

    }

    @ConfigurationProperties(prefix = "dbc.auth.basic")
    @Data
    public static class Basic{
        /**
         * basic认证请求头
         */
        private String key = "Authorization";

        /**
         * 需要basic认证的url列表
         */
        private List<String> includeUrls= Lists.newArrayList("/api/**");
    }
}
