package com.github.hbq969.middleware.dbc.driver.initial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
public class CustomPropertySourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("初始化 CustomPropertySourceInitializer.");
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        boolean enabled = environment.getProperty("spring.cloud.config.h-dbc.enabled", Boolean.class, false);
        log.info("h-dbc配置中心启用标识: {}", enabled);
        if (enabled) {
            log.info("启用h-dbc配置中心配置");
            environment.getPropertySources().addFirst(new DatabasePropertySource("databasePropertySource", environment));
        }
    }
}
