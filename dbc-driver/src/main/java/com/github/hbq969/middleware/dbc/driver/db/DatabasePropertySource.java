package com.github.hbq969.middleware.dbc.driver.db;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import com.github.hbq969.middleware.dbc.driver.api.APIPropertySource;
import com.github.hbq969.middleware.dbc.driver.config.DbcConfig;
import com.github.hbq969.middleware.dbc.driver.initial.RefreshListener;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DatabasePropertySource extends MapPropertySource implements RefreshListener {

    private final static String SQL = "select config_key,config_value,data_type from h_dbc_service a,h_dbc_config b where a.service_id=b.service_id and b.app=? and a.service_name=?  and b.profile_name=?";
    private final static int[] TYPES = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
    private Gson gson = new Gson();
    private ConfigurableEnvironment environment;
    private Map<String, Object> configMap;
    private static DbcConfig conf = new DbcConfig();

    public DatabasePropertySource(String name, ConfigurableEnvironment environment) {
        super(name, new HashMap<>());
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
            JdbcTemplate jt = conf.configSet(environment).getJt();
            String serviceName = conf.getServiceName();
            String profileName = conf.getProfileName();
            Object[] paras = new Object[]{conf.getDbcKey(), serviceName, profileName};
            Map<String, String> tmp = new HashMap<>(256);
            List<Pair<String, Object>> pairs = jt.query(SQL, paras, TYPES,
                    (rs, rowNum) -> rowMapperToPair(rs, tmp)
            );
            tmp.clear();
            if (null == pairs || pairs.isEmpty()) {
                log.warn("db方式拉取到配置为空，切换为读取应用本地配置");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("db方式拉取到配置数: {} 个", pairs.size());
            }
            for (Pair<String, Object> pair : pairs) {
                String value = String.valueOf(pair.getValue());
                if (APIPropertySource.isEncValue(value)) {
                    value = APIPropertySource.decode(environment, pair.getKey(), value);
                    configMap.put(pair.getKey(), value);
                } else {
                    configMap.put(pair.getKey(), pair.getValue());
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("加载到服务配置: {}", gson.toJson(configMap));
            }
        } catch (Exception e) {
            log.error("通过db方式读取配置异常", e);
            throw new RuntimeException(e);
        }
    }

    private static Pair rowMapperToPair(ResultSet rs, Map<String, String> tmp) throws SQLException {
        String dt = rs.getString(3);
        Class<?> clz;
        String key, value;
        try {
            clz = Class.forName(dt);
        } catch (ClassNotFoundException e) {
            clz = String.class;
        }
        key = rs.getString(1);
        value = rs.getString(2);
        tmp.put(key, value);
        Object result = MapUtil.get(tmp, key, clz);
        Pair pair = new Pair<>(key, result);
        return pair;
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
}
