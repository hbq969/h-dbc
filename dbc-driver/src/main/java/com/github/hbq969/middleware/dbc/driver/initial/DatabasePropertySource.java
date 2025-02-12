package com.github.hbq969.middleware.dbc.driver.initial;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.middleware.dbc.driver.config.DbcConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DatabasePropertySource extends MapPropertySource {
    private ConfigurableEnvironment environment;
    private Map<String, Object> configMap;

    public DatabasePropertySource(String name, ConfigurableEnvironment environment) {
        super(name, new HashMap<>());
        this.environment = environment;
        loadConfig();
    }

    private void loadConfig() {
        DbcConfig conf = new DbcConfig();
        try {
            JdbcTemplate jt = conf.configSet(environment).jdbcInitial();
            String sql = "select config_key,config_value from h_dbc_service a,h_dbc_config b " + " where a.service_id=b.service_id" + " and b.app=? and a.service_name=? " + " and b.profile_name=? ";
            String serviceName = conf.getServiceName();
            String profileName = conf.getProfileName();
            Object[] paras = new Object[]{conf.getDbcKey(), serviceName, profileName};
            log.info("加载服务配置, {}, {}", sql, GsonUtils.toJson(paras));
            List<Pair<String, Object>> pairs = jt.query(sql, paras, new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR}, (rs, rowNum) -> new Pair<>(rs.getString(1), rs.getString(2)));
            this.configMap = new HashMap<>();
            for (Pair<String, Object> pair : pairs) {
                configMap.put(pair.getKey(), pair.getValue());
            }
            if (log.isDebugEnabled()) {
                log.debug("加载到服务配置: {}", GsonUtils.toJson(configMap));
            }
        } finally {
            try {
                conf.destroy();
            } catch (Exception e) {
            }
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
}
