package com.github.hbq969.middleware.dbc.driver.config;

import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import com.github.hbq969.middleware.dbc.driver.api.ConfigService;
import com.github.hbq969.middleware.dbc.driver.api.ConfigServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;

@ConfigurationProperties(prefix = "spring.cloud.config.h-dbc.api")
@Data
@Slf4j
public class ApiInfo {

    /**
     * api接口地址
     */
    private String url = "http://localhost:30170";

    /**
     * api调用失败重试次数，每秒重试一次
     */
    private int retry = 3;

    /**
     * api接口私钥
     */
    private String secret;

    /**
     * api接口编码
     */
    private String charset = "utf-8";

    private Auth auth = new Auth();

    private volatile transient ConfigService api;

    public ApiInfo configSet(ConfigurableEnvironment env) {
        this.url = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.api.url",
                env.getProperty("spring.cloud.config.h-dbc.api.url", this.url));
        this.secret = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.api.secret",
                env.getProperty("spring.cloud.config.h-dbc.api.secret"));
        this.charset = env.getProperty("spring.cloud.config.h-dbc.api.charset", this.charset);
        this.auth.setEnabled(env.getProperty("spring.cloud.config.h-dbc.api.auth.enabled",Boolean.class,true));
        this.auth.getBasic().setKey(APIPropertySource.decode(env,"spring.cloud.config.h-dbc.api.auth.basic.key",env.getProperty("spring.cloud.config.h-dbc.api.auth.basic.key",this.auth.getBasic().getKey())));
        this.auth.getBasic().setUsername(APIPropertySource.decode(env,"spring.cloud.config.h-dbc.api.auth.basic.username",env.getProperty("spring.cloud.config.h-dbc.api.auth.basic.username")));
        this.auth.getBasic().setPassword(APIPropertySource.decode(env,"spring.cloud.config.h-dbc.api.auth.basic.password",env.getProperty("spring.cloud.config.h-dbc.api.auth.basic.password")));
        return this;
    }

    public ConfigService getApiProxy(String dbcKey) throws Exception {
        if (this.api != null) {
            return this.api;
        }
        ConfigServiceImpl csi = new ConfigServiceImpl(this);
        csi.setUrl(String.join("/", this.url, dbcKey, "api"));
        csi.setInter(ConfigService.class);
        csi.afterPropertiesSet();
        this.api = csi.getObject();
        log.debug("初始化配置中心api加载器");
        return this.api;
    }
}
