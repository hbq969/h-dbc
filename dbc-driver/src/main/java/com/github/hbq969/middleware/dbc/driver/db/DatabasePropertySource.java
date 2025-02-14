package com.github.hbq969.middleware.dbc.driver.db;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
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

    private final static String SQL = "select config_key,config_value from h_dbc_service a,h_dbc_config b where a.service_id=b.service_id and b.app=? and a.service_name=?  and b.profile_name=?";
    private final static int[] TYPES = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
    private ConfigurableEnvironment environment;
    private Map<String, Object> configMap;

    private static DbcConfig conf = new DbcConfig();

    public DatabasePropertySource(String name, ConfigurableEnvironment environment) {
        super(name, new HashMap<>());
        this.environment = environment;
        loadConfig();
    }

    private void loadConfig() {
        log.info("通过db方式从h-dbc配置中心读取配置。");
        try {
            JdbcTemplate jt = conf.configSet(environment).getJt();
            String serviceName = conf.getServiceName();
            String profileName = conf.getProfileName();
            Object[] paras = new Object[]{conf.getDbcKey(), serviceName, profileName};
            log.info("加载服务配置, {}, {}", SQL, GsonUtils.toJson(paras));
            List<Pair<String, Object>> pairs = jt.query(SQL, paras, TYPES,
                    (rs, rowNum) -> new Pair<>(rs.getString(1), rs.getString(2)));
            this.configMap = new HashMap<>();
            for (Pair<String, Object> pair : pairs) {
                String value = String.valueOf(pair.getValue());
                value = APIPropertySource.decode(environment, pair.getKey(), value);
                configMap.put(pair.getKey(), value);
            }
            if (log.isTraceEnabled()) {
                log.trace("加载到服务配置: {}", GsonUtils.toJson(configMap));
            }
        } catch (Exception e) {
            log.error("通过db方式读取配置异常", e);
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
}
