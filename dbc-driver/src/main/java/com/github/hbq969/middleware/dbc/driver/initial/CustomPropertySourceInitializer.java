package com.github.hbq969.middleware.dbc.driver.initial;

import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import com.github.hbq969.middleware.dbc.driver.db.DatabasePropertySource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * <p></p><code>ApplicationContextInitializer</code>`的 initialize 方法通常会在 Spring 应用上下文的启动过程中被调用。
 * 这个方法会在 <code>ConfigurableApplicationContext</code> 初始化时执行，而配置需要给每个上下文都要添加</p>
 */
@Slf4j
public class CustomPropertySourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private MapPropertySource customPropertySource;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        boolean enabled = environment.getProperty("spring.cloud.config.h-dbc.enabled", Boolean.class, false);
        if (enabled) {
            String strategy = environment.getProperty("spring.cloud.config.h-dbc.strategy");
            switch (strategy){
                case "api":
                    this.customPropertySource = new APIPropertySource("apiPropertySource", environment);
                    break;
                case "db":
                    this.customPropertySource = new DatabasePropertySource("dbPropertySource", environment);
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("不支持的策略[%s]", strategy));
            }
            environment.getPropertySources().addFirst(this.customPropertySource);
        }
    }
}
