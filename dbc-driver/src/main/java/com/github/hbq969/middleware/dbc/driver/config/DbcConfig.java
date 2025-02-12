package com.github.hbq969.middleware.dbc.driver.config;

import cn.hutool.core.lang.Assert;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

@Data
public class DbcConfig implements DisposableBean {
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

    private transient HikariDataSource hds =new HikariDataSource();

    public DbcConfig configSet(ConfigurableEnvironment env) {
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
        return this;
    }

    public JdbcTemplate jdbcInitial() {
        hds.setDriverClassName(getDriverClassName());
        hds.setJdbcUrl(getUrl());
        hds.setUsername(getUrl());
        hds.setUsername(getUsername());
        hds.setPassword(getPassword());
        hds.setMaximumPoolSize(getMaximumPoolSize());
        hds.setMinimumIdle(getMinimumIdle());
        hds.setMaxLifetime(getMaxLifetime());
        hds.setConnectionTestQuery(getConnectionTestQuery());
        return new JdbcTemplate(hds);
    }

    @Override
    public void destroy() throws Exception {
        hds.close();
    }
}
