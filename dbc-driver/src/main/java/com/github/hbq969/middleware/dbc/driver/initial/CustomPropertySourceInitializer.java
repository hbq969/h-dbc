package com.github.hbq969.middleware.dbc.driver.initial;

import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p></p><code>ApplicationContextInitializer</code>的 initialize 方法通常会在 Spring 应用上下文的启动过程中被调用。
 * 这个方法会在 <code>ConfigurableApplicationContext</code> 初始化时执行，而配置需要给每个上下文都要添加</p>
 */
@Slf4j
public class CustomPropertySourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final AtomicReference<MapPropertySource> CUSTOM_PROPERTY_SOURCE = new AtomicReference<>();
    private static final Object LOCK = new Object();

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        // 缓存常用属性
        boolean enabled = environment.getProperty("spring.cloud.config.h-dbc.enabled", Boolean.class, false);

        if (!enabled) {
            if (log.isDebugEnabled())
                log.debug("配置中心拉取配置未启用，改为从本地加载配置");
            return;
        }

        MapPropertySource propertySource = CUSTOM_PROPERTY_SOURCE.get();
        if (propertySource == null) {
            synchronized (LOCK) {
                propertySource = CUSTOM_PROPERTY_SOURCE.get();
                if (propertySource == null) {
                    try {
                        propertySource = new APIPropertySource("dbcPropertySource", environment, false);
                        CUSTOM_PROPERTY_SOURCE.set(propertySource);
                    } catch (Exception e) {
                        log.error("配置中心加载配置异常，改为从本地加载配置", e);
                        return;
                    }
                }
            }
        }

        if (propertySource != null) {
            environment.getPropertySources().addFirst(propertySource);
        }
    }
}
