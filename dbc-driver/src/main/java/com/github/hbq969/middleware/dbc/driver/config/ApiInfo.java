package com.github.hbq969.middleware.dbc.driver.config;

import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import com.github.hbq969.middleware.dbc.driver.api.ConfigService;
import com.github.hbq969.middleware.dbc.driver.api.ConfigServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;

@ConfigurationProperties(prefix = "spring.cloud.config.h-dbc.api")
@Slf4j
public class ApiInfo {

    /**
     * api连接超时时间
     */
    @Getter
    @Setter
    private long connectTimeoutMills = 5000L;

    /**
     * api接口地址
     */
    @Getter
    @Setter
    private String url = "http://localhost:30170";

    /**
     * api调用失败重试次数，每秒重试一次
     */
    @Getter
    @Setter
    private int retry = 3;

    /**
     * api重试间隔时间
     */
    @Getter
    @Setter
    private long retryPeriodMills = 1000L;

    /**
     * api重试超时时间
     */
    @Getter
    @Setter
    private long retryTimeoutMills = 5000L;

    /**
     * api接口编码
     */
    @Getter
    @Setter
    private String charset = "utf-8";

    /**
     * 是否开启api请求日志，默认：false
     */
    @Getter
    @Setter
    private boolean apiLog = false;

    /**
     * AES秘钥、iv长度，最大不能超过32位
     */
    @Getter
    @Setter
    private int lengthAesKey = 16;

    @Getter
    @Setter
    private BasicAuth basicAuth = new BasicAuth();

    /**
     * 是否开启证书验证
     */
    @Getter
    @Setter
    private boolean https = false;

    /**
     * hdbc_client.p12的路径，默认：/certs/hdbc_client.p12
     */
    @Getter
    @Setter
    private String truststorePath = "/certs/hdbc_client.p12";

    /**
     * hdbc_client.p12的密码
     */
    @Getter
    @Setter
    private String truststorePassword;

    /**
     * hdbc_client.p12的类型
     */
    @Getter
    @Setter
    private String truststoreType = "PKCS12";

    @Getter
    private volatile transient ConfigService api;

    public ApiInfo configSet(ConfigurableEnvironment env) {
        this.url = env.getProperty("spring.cloud.config.h-dbc.api.url", this.url);
        this.charset = env.getProperty("spring.cloud.config.h-dbc.api.charset", this.charset);
        this.apiLog = env.getProperty("spring.cloud.config.h-dbc.api.api-log", Boolean.class, this.apiLog);
        this.lengthAesKey = env.getProperty("spring.cloud.config.h-dbc.api.length-aes-key", Integer.class, this.lengthAesKey);
        this.basicAuth.setKey(env.getProperty("spring.cloud.config.h-dbc.api.basic-auth.key", this.basicAuth.getKey()));
        this.basicAuth.setUsername(APIPropertySource.decode(env, "spring.cloud.config.h-dbc.api.basic-auth.username", env.getProperty("spring.cloud.config.h-dbc.api.basic-auth.username")));
        this.basicAuth.setPassword(APIPropertySource.decode(env, "spring.cloud.config.h-dbc.api.basic-auth.password", env.getProperty("spring.cloud.config.h-dbc.api.basic-auth.password")));
        this.basicAuth.check();
        this.https = env.getProperty("spring.cloud.config.h-dbc.api.https", Boolean.class, this.https);
        String _truststorePassword = env.getProperty("spring.cloud.config.h-dbc.api.truststore-password");
        if (this.https && (_truststorePassword == null || _truststorePassword.length() == 0))
            throw new UnsupportedOperationException("开启https后，spring.cloud.config.h-dbc.api.truststore-password不能为空");
        this.truststorePath = env.getProperty("spring.cloud.config.h-dbc.api.truststore-path", this.truststorePath);
        this.truststoreType = env.getProperty("spring.cloud.config.h-dbc.api.truststore-type", this.truststoreType);
        this.truststorePassword = APIPropertySource.decode(env, "spring.cloud.config.h-dbc.api.truststore-password", _truststorePassword);
        return this;
    }

    public ConfigService getApiProxy() throws Exception {
        if (this.api != null) {
            return this.api;
        }
        ConfigServiceImpl csi = new ConfigServiceImpl(this);
        csi.setUrl(this.url);
        csi.setInter(ConfigService.class);
        csi.afterPropertiesSet();
        this.api = csi.getObject();
        return this.api;
    }
}
