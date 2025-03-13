package com.github.hbq969.middleware.dbc.driver.initial;

import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import com.github.hbq969.middleware.dbc.driver.db.DatabasePropertySource;
import com.github.hbq969.middleware.dbc.driver.mix.MixPropertySource;
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
        String strategy = environment.getProperty("spring.cloud.config.h-dbc.strategy");

        if (!enabled) {
            if (log.isTraceEnabled()) {
                log.trace("H-DBC 特性未启用，跳过自定义属性源初始化");
            }
            return;
        }

        if (null == strategy || strategy.trim().length() == 0) {
            log.warn("未配置有效的 H-DBC 策略，跳过自定义属性源初始化");
            return;
        }

        MapPropertySource propertySource = CUSTOM_PROPERTY_SOURCE.get();
        if (propertySource == null) { // 第一次检查（无锁）
            synchronized (LOCK) {
                propertySource = CUSTOM_PROPERTY_SOURCE.get(); // 第二次检查（加锁后）
                if (propertySource == null) {
                    try {
                        propertySource = createPropertySource(strategy, environment);
                        CUSTOM_PROPERTY_SOURCE.set(propertySource);
                        if (log.isDebugEnabled()) {
                            log.debug("成功加载 H-DBC 策略: {}", strategy);
                        }
                    } catch (Exception e) {
                        log.error("加载 H-DBC 策略失败，策略: {}, 原因: {}", strategy, e.getMessage(), e);
                        return; // 加载失败时直接退出
                    }
                }
            }
        }

        if (propertySource != null) {
            environment.getPropertySources().addFirst(propertySource);
            if (log.isTraceEnabled()) {
                log.trace("已将自定义属性源添加到环境配置中");
            }
        }
    }

    // 提取策略选择逻辑为独立方法
    private MapPropertySource createPropertySource(String strategy, ConfigurableEnvironment env) {
        switch (strategy.toLowerCase()) {
            case "api":
                return new APIPropertySource("dbcPropertySource", env);
            case "db":
                return new DatabasePropertySource("dbcPropertySource", env);
            case "mix":
                return new MixPropertySource("dbcPropertySource", env);
            default:
                throw new UnsupportedOperationException(String.format("不支持的策略[%s]", strategy));
        }
    }
}
