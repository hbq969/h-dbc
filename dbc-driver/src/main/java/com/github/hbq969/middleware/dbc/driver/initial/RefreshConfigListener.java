package com.github.hbq969.middleware.dbc.driver.initial;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

@Component("dbc-RefreshConfigListener")
@ConditionalOnProperty(prefix = "spring.cloud.config.h-dbc", name = "enabled", havingValue = "true")
@Slf4j
public class RefreshConfigListener implements ApplicationListener<RefreshScopeRefreshedEvent>, EnvironmentAware {

    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("监听到配置变化事件: {}", event);
        }
        PropertySource<?> target = fetchDelegate(this.environment.getPropertySources().get("dbcPropertySource"));
        if (target != null) {
            RefreshListener.class.cast(target).refreshConfig();
        }
    }

    public PropertySource<?> fetchDelegate(PropertySource<?> source) {
        if (source == null) {
            log.warn("未发现注册的自定义的配置加载器 dbcPropertySource");
            return null;
        }
        if (source instanceof EncryptablePropertySource) {
            EncryptablePropertySource delegate = (EncryptablePropertySource) source;
            delegate.refresh();
            return fetchDelegate(delegate.getDelegate());
        } else if (source instanceof RefreshListener) {
            return source;
        } else {
            return null;
        }
    }
}
