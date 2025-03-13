package com.github.hbq969.middleware.dbc.driver.mix;

import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import com.github.hbq969.middleware.dbc.driver.db.DatabasePropertySource;
import com.github.hbq969.middleware.dbc.driver.initial.RefreshListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MixPropertySource extends MapPropertySource implements RefreshListener {

    private MapPropertySource proxy;

    public MixPropertySource(String name, ConfigurableEnvironment environment) {
        super(name, new HashMap<>());
        this.proxy = new APIPropertySource(name, environment);
        if (APIPropertySource.class.cast(this.proxy).isLoadFailed()) {
            log.warn("通过api方式拉取配置失败，改为使用db方式拉取配置");
            this.proxy = new DatabasePropertySource(name, environment);
        }
    }

    @Override
    public void refreshConfig() {
        if (this.proxy instanceof RefreshListener) {
            RefreshListener.class.cast(this.proxy).refreshConfig();
        }
    }

    @Override
    public Map<String, Object> getSource() {
        return this.proxy.getSource();
    }

    @Override
    public Object getProperty(String name) {
        return this.proxy.getProperty(name);
    }

    @Override
    public boolean containsProperty(String name) {
        return this.proxy.containsProperty(name);
    }

    @Override
    public String[] getPropertyNames() {
        return this.proxy.getPropertyNames();
    }
}
