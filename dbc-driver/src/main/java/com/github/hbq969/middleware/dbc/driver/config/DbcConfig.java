package com.github.hbq969.middleware.dbc.driver.config;

import cn.hutool.core.lang.Assert;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.middleware.dbc.driver.api.ConfigService;
import com.github.hbq969.middleware.dbc.driver.api.ConfigServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
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
     * 数据库驱动类
     */
    private String driverClassName = "com.mysql.cj.jdbc.Driver";

    /**
     * 数据库url
     */
    private String url = "jdbc:mysql://docker.for.mac.host.internal:3306/h-dbc?useUnicode=true&allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=15000";

    private String username = "foo";

    private String password = "bar";

    private int maximumPoolSize = 5;

    private int minimumIdle = 2;

    private long maxLifetime = 1800000L;

    private String connectionTestQuery = "SELECT 1";

    /**
     * api接口地址
     */
    private String apiUrl;

    /**
     * api接口私钥
     */
    private String apiSecret;

    /**
     * api接口编码
     */
    private String apiCharset = "utf-8";

    private volatile transient JdbcTemplate jt;

    private volatile transient boolean initialized = false;

    private volatile transient ConfigService api;

    public DbcConfig configSet(ConfigurableEnvironment env) {
        if (this.initialized) {
            return this;
        }
        this.enabled = env.getProperty("spring.cloud.config.h-dbc.enabled", Boolean.class, false);
        this.dbcKey = env.getProperty("spring.cloud.config.h-dbc.dbc-key", String.class, "h-dbc");
        this.serviceName = env.getProperty("spring.cloud.config.h-dbc.service-name", String.class, null);
        Assert.notNull(this.serviceName, "请配置服务名, spring.cloud.config.h-dbc.service-name");
        this.profileName = env.getProperty("spring.cloud.config.h-dbc.profile-name", String.class, "default");
        this.driverClassName = env.getProperty("spring.cloud.config.h-dbc.driver-class-name", String.class, "com.mysql.cj.jdbc.Driver");
        this.url = env.getProperty("spring.cloud.config.h-dbc.url", String.class, "jdbc:mysql://docker.for.mac.host.internal:3306/h-dbc?useUnicode=true&allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=15000");
        this.username = env.getProperty("spring.cloud.config.h-dbc.username", String.class, "foo");
        this.password = env.getProperty("spring.cloud.config.h-dbc.password", String.class, "bar");
        this.maximumPoolSize = env.getProperty("spring.cloud.config.h-dbc.url.maximum-pool-size", Integer.class, 5);
        this.minimumIdle = env.getProperty("spring.cloud.config.h-dbc.minimum-idle", Integer.class, 2);
        this.maxLifetime = env.getProperty("spring.cloud.config.h-dbc.max-lifetime", Long.class, 1800000L);
        this.connectionTestQuery = env.getProperty("spring.cloud.config.h-dbc.connection-test-query", String.class, "select 1");
        this.apiUrl = env.getProperty("spring.cloud.config.h-dbc.api-url");
        this.apiSecret = env.getProperty("spring.cloud.config.h-dbc.api-secret");
        this.apiCharset = env.getProperty("spring.cloud.config.h-dbc.api-charset");
        this.initialized = true;
        log.debug("配置中心对应配置: {}", GsonUtils.toJson(this));
        return this;
    }

    public JdbcTemplate jdbcInitial() {
        if (this.jt != null) {
            return this.jt;
        }
        HikariDataSource hds = new HikariDataSource();
        hds.setDriverClassName(getDriverClassName());
        hds.setJdbcUrl(getUrl());
        hds.setUsername(getUrl());
        hds.setUsername(getUsername());
        hds.setPassword(getPassword());
        hds.setMaximumPoolSize(getMaximumPoolSize());
        hds.setMinimumIdle(getMinimumIdle());
        hds.setMaxLifetime(getMaxLifetime());
        hds.setConnectionTestQuery(getConnectionTestQuery());
        this.jt = new JdbcTemplate(hds);
        log.debug("初始化配置中心db加载器");
        return this.jt;
    }

    public ConfigService apiServiceInitial() throws Exception {
        if (this.api != null) {
            return this.api;
        }
        ConfigServiceImpl csi = new ConfigServiceImpl(this.apiSecret, this.apiCharset);
        csi.setUrl(String.join("/", this.apiUrl, this.dbcKey, "api"));
        csi.setInter(ConfigService.class);
        csi.afterPropertiesSet();
        this.api = csi.getObject();
        log.debug("初始化配置中心api加载器");
        return this.api;
    }
}
