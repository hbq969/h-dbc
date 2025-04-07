package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.middleware.dbc.driver.api.model.TypePair;
import com.github.hbq969.middleware.dbc.driver.config.DbcConfig;
import com.github.hbq969.middleware.dbc.driver.initial.RefreshListener;
import com.google.gson.Gson;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class APIPropertySource extends MapPropertySource implements RefreshListener {

    public final static String ENC_PREFIX = "ENC(";
    public final static String ENC_SUFFIX = ")";
    private Gson gson = new Gson();
    private ConfigurableEnvironment environment;
    private Map<String, Object> configMap;
    private static volatile PooledPBEStringEncryptor encryptor;
    private static DbcConfig conf = new DbcConfig();
    private volatile boolean loadFailed = false;
    private final boolean mix;

    public APIPropertySource(String name, ConfigurableEnvironment environment, boolean mix) {
        super(name, new HashMap<>());
        this.mix = mix;
        this.environment = environment;
        loadConfig();
    }

    @Override
    public void refreshConfig() {
        loadConfig();

    }

    private void loadConfig() {
        this.configMap = new HashMap<>();
        try {
            ConfigService api = conf.configSet(environment).getApiProxy();
            ConfigInfo model = new ConfigInfo();
            model.setServiceName(conf.getServiceName());
            model.setProfileName(conf.getProfileName());
            List<TypePair> pairs = Collections.EMPTY_LIST;
            try {
                pairs = (List<TypePair>) api.getConfigList(model);
            } catch (Exception e) {
                if (e instanceof RetryableException) {
                    log.error("api方式连接异常，{}", e.getMessage());
                } else {
                    log.error("api方式连接异常", e);
                }
                this.loadFailed = true;
            }
            if (isLoadFailed()) log.warn(mix ? "api拉取配置失败" : "api拉取配置失败，切换为读取应用本地配置");
            else if (pairs == null || pairs.isEmpty()) log.warn("未拉取到相关配置，请检查是否在配置中心添加服务配置");
            else if (!pairs.isEmpty()) {
                if (log.isDebugEnabled()) log.debug("api拉取到配置数: {} 个", pairs.size());
            }
            for (TypePair pair : pairs) {
                String value = String.valueOf(pair.getValue());
                if (isEncValue(value)) {
                    value = decode(environment, String.valueOf(pair.getKey()), value);
                    configMap.put(pair.getKey(), value);
                } else {
                    configMap.put(pair.getKey(), pair.getValue());
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("加载到服务配置: {}", gson.toJson(configMap));
            }
        } catch (Exception e) {
            log.error("通过api方式读取配置异常", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> getSource() {
        return this.configMap;
    }

    @Override
    public Object getProperty(String name) {
        return this.configMap.get(name);
    }

    @Override
    public boolean containsProperty(String name) {
        return null != getProperty(name);
    }

    @Override
    public String[] getPropertyNames() {
        synchronized (this.configMap) {
            return StringUtils.toStringArray(this.configMap.keySet());
        }
    }

    public boolean isLoadFailed() {
        return loadFailed;
    }

    public synchronized static void initialStringEncryptor(ConfigurableEnvironment environment) {
        if (encryptor != null) {
            return;
        }
        encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        String pwd = environment.getProperty("jasypt.encryptor.password", "U(^3ia)*v2$");
        String algorithm = environment.getProperty("jasypt.encryptor.algorithm", "PBEWITHHMACSHA512ANDAES_256");
        String koi = environment.getProperty("jasypt.encryptor.key-obtention-iterations", "1000");
        String poolSize = environment.getProperty("jasypt.encryptor.pool-size", "1");
        String providerName = environment.getProperty("jasypt.encryptor.provider-name", "SunJCE");
        String sgcn = environment.getProperty("jasypt.encryptor.salt-generator-class-name", "org.jasypt.salt.RandomSaltGenerator");
        String sot = environment.getProperty("jasypt.encryptor.string-output-type", "base64");
        if (log.isDebugEnabled()) {
            log.debug("jasypt配置, algorithm: {}, koi: {}, poolSize: {}, providerName: {}, sgcn: {}, sot: {}", algorithm, koi, poolSize, providerName, sgcn, sot);
        }
        config.setPassword(pwd);
        config.setAlgorithm(algorithm);
        config.setKeyObtentionIterations(koi);
        config.setPoolSize(poolSize);
        config.setProviderName(providerName);
        config.setSaltGeneratorClassName(sgcn);
        config.setStringOutputType(sot);
        try {
            config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        } catch (Error e) {
            log.error("SimpleStringPBEConfig方法setIvGeneratorClassName不兼容，向下兼容处理");
        }
        encryptor.setConfig(config);
    }

    public static boolean isEncValue(String value) {
        if (value == null) return false;
        return value.startsWith(ENC_PREFIX) && value.endsWith(ENC_SUFFIX);
    }

    public static String decode(ConfigurableEnvironment environment, String key, String encode) {
        initialStringEncryptor(environment);
        if (!isEncValue(encode)) {
            return encode;
        }
        String value = encryptor.decrypt(encode.substring(4, encode.length() - 1));
        if (log.isDebugEnabled()) {
            log.debug("springboot初始化阶段属性解密处理, 属性名: {}, 原始值: {}, 解密后: {}", key, encode, value);
        }
        return value;
    }
}
