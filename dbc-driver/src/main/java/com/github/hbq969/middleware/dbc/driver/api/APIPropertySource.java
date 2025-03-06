package com.github.hbq969.middleware.dbc.driver.api;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.middleware.dbc.driver.config.DbcConfig;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class APIPropertySource extends MapPropertySource {
    private ConfigurableEnvironment environment;
    private Map<String, Object> configMap;
    private static PooledPBEStringEncryptor encryptor;
    private static DbcConfig conf = new DbcConfig();

    public APIPropertySource(String name, ConfigurableEnvironment environment) {
        super(name, new HashMap<>());
        this.environment = environment;
        loadConfig();
    }

    private void loadConfig() {
        try {
            ConfigService api = conf.configSet(environment).getApiProxy();
            ConfigInfo model = new ConfigInfo();
            model.setServiceName(conf.getServiceName());
            model.setProfileName(conf.getProfileName());
            List<Pair> pairs = api.getConfigList(model);
            this.configMap = new HashMap<>();
            for (Pair pair : pairs) {
                String value = String.valueOf(pair.getValue());
                value = decode(environment, String.valueOf(pair.getKey()), value);
                configMap.put((String) pair.getKey(), value);
            }
            if (log.isTraceEnabled()) {
                log.trace("加载到服务配置: {}", GsonUtils.toJson(configMap));
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
        return StringUtils.toStringArray(this.configMap.keySet());
    }

    public static void initialStringEncryptor(ConfigurableEnvironment environment) {
        if (encryptor != null) {
            return;
        }
        encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        String pwd = environment.getProperty("jasypt.encryptor.password", "U(^3ia)*v2$");
        config.setPassword(pwd);
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
    }

    public static String decode(ConfigurableEnvironment environment, String key, String encode) {
        initialStringEncryptor(environment);
        if (encode.startsWith("ENC(") && encode.endsWith(")")) {
            String value = encryptor.decrypt(encode.substring(4, encode.length() - 1));
            if (log.isTraceEnabled()) {
                log.trace("springboot初始化阶段属性解密处理, 属性名: {}, 原始值: {}, 解密后: {}", key, encode, value);
            }
            return value;
        } else {
            return encode;
        }
    }
}
