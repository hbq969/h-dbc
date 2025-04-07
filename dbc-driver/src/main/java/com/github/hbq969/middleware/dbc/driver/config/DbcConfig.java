package com.github.hbq969.middleware.dbc.driver.config;

import cn.hutool.core.lang.Assert;
import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import com.github.hbq969.middleware.dbc.driver.api.ConfigService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;

@ConfigurationProperties(prefix = "spring.cloud.config.h-dbc")
@Data
@Slf4j
public class DbcConfig {
    /**
     * 是否启用登录功能
     */
    private boolean enabled;

    /**
     * 服务名，参考 <code>spring.application.name</code>
     */
    private String serviceName;

    /**
     * 环境profile，参考<code>spring.profiles.active</code>
     */
    private String profileName;

    private ApiInfo api = new ApiInfo();

    public DbcConfig configSet(ConfigurableEnvironment env) {
        this.enabled = env.getProperty("spring.cloud.config.h-dbc.enabled", Boolean.class, false);
        this.serviceName = env.getProperty("spring.cloud.config.h-dbc.service-name", String.class, null);
        Assert.notNull(this.serviceName, "请配置服务名, spring.cloud.config.h-dbc.service-name");
        this.profileName = env.getProperty("spring.cloud.config.h-dbc.profile-name", String.class, "default");
        this.api.configSet(env);
        if (log.isTraceEnabled()) {
            log.trace("配置中心对应配置: {}", this);
        }
        return this;
    }

    public ConfigService getApiProxy() throws Exception {
        return this.api.getApiProxy();
    }
}
