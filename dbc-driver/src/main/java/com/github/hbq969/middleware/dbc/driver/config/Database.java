package com.github.hbq969.middleware.dbc.driver.config;

import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

@ConfigurationProperties(prefix = "spring.cloud.config.h-dbc.db")
@Data
@Slf4j
public class Database {

    /**
     * 数据库驱动类
     */
    private String driverClassName = "com.mysql.cj.jdbc.Driver";

    /**
     * 数据库url
     */
    private String jdbcUrl = "jdbc:mysql://docker.for.mac.host.internal:3306/dbc?useUnicode=true&allowPublicKeyRetrieval=true&characterEncoding=utf-8&useSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=15000";

    private String username = "foo";

    private String password = "bar";

    private int maximumPoolSize = 2;

    private int minimumIdle = 1;

    private long maxLifetime = 1800000L;

    private String connectionTestQuery = "SELECT 1";

    private volatile transient JdbcTemplate jt;

    private volatile transient boolean initialized = false;

    private transient StringEncryptor stringEncryptor;

    public Database configSet(ConfigurableEnvironment env) {
        if (this.initialized) {
            return this;
        }
        this.driverClassName = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.db.driver-class-name",
                env.getProperty("spring.cloud.config.h-dbc.db.driver-class-name", String.class, this.driverClassName));
        this.jdbcUrl = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.db.jdbc-url",
                env.getProperty("spring.cloud.config.h-dbc.db.jdbc-url", String.class, this.jdbcUrl));
        this.username = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.db.username",
                env.getProperty("spring.cloud.config.h-dbc.db.username", String.class, this.username));
        this.password = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.db.password",
                env.getProperty("spring.cloud.config.h-dbc.db.password", String.class, this.password));
        this.maximumPoolSize = env.getProperty("spring.cloud.config.h-dbc.db.maximum-pool-size", Integer.class, this.maximumPoolSize);
        this.minimumIdle = env.getProperty("spring.cloud.config.h-dbc.db.minimum-idle", Integer.class, this.minimumIdle);
        this.maxLifetime = env.getProperty("spring.cloud.config.h-dbc.db.max-lifetime", Long.class, this.maxLifetime);
        this.connectionTestQuery = env.getProperty("spring.cloud.config.h-dbc.db.connection-test-query", String.class, this.connectionTestQuery);
        jdbcInitial();
        this.initialized = true;
        return this;
    }

    private void jdbcInitial() {
        if (this.jt != null) {
            return;
        }
        HikariDataSource hds = new HikariDataSource();
        hds.setDriverClassName(this.driverClassName);
        hds.setJdbcUrl(this.jdbcUrl);
        hds.setUsername(this.username);
        hds.setPassword(this.password);
        hds.setMaximumPoolSize(this.maximumPoolSize);
        hds.setMinimumIdle(this.minimumIdle);
        hds.setMaxLifetime(this.maxLifetime);
        hds.setConnectionTestQuery(this.connectionTestQuery);
        this.jt = new JdbcTemplate(hds);
    }
}
