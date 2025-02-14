package com.github.hbq969.middleware.dbc.driver.config;

import cn.hutool.core.lang.Assert;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import com.github.hbq969.middleware.dbc.driver.api.ConfigService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

@ConfigurationProperties(prefix = "spring.cloud.config.h-dbc")
@Data
@Slf4j
public class DbcConfig {
    /**
     * 是否启用登录功能
     */
    private boolean enabled;

    /**
     * 配置中心标识
     */
    private String dbcKey = "h-dbc";

    /**
     * 服务名，参考 <code>spring.application.name</code>
     */
    private String serviceName;

    /**
     * 环境profile，参考<code>spring.profiles.active</code>
     */
    private String profileName;

    /**
     * 支持的策略
     * <ul>
     *     <li>api</li>
     *     <li>db</li>
     * </ul>
     */
    private String strategy = "api";

    private Database db = new Database();

    private ApiInfo api = new ApiInfo();

    public DbcConfig configSet(ConfigurableEnvironment env) {
        this.enabled = env.getProperty("spring.cloud.config.h-dbc.enabled", Boolean.class, false);
        this.dbcKey = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.dbc-key",
                env.getProperty("spring.cloud.config.h-dbc.dbc-key", String.class, "h-dbc"));
        this.serviceName = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.service-name",
                env.getProperty("spring.cloud.config.h-dbc.service-name", String.class, null));
        Assert.notNull(this.serviceName, "请配置服务名, spring.cloud.config.h-dbc.service-name");
        this.profileName = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.profile-name",
                env.getProperty("spring.cloud.config.h-dbc.profile-name", String.class, "default"));
        this.db.configSet(env);
        this.api.configSet(env);
        log.debug("配置中心对应配置: {}", GsonUtils.toJson(this));
        return this;
    }

    public JdbcTemplate getJt() {
        return this.db.getJt();
    }

    public ConfigService getApiProxy() throws Exception {
        return this.api.getApiProxy(dbcKey);
    }
}
