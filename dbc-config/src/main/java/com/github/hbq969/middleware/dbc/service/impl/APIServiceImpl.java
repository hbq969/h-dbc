package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.map.MapUtil;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.spring.yaml.TypePair;
import com.github.hbq969.code.common.utils.YamlPropertiesFileConverter;
import com.github.hbq969.code.sm.login.session.UserContext;
import com.github.hbq969.middleware.dbc.dao.APIDao;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.model.ConfigModel;
import com.github.hbq969.middleware.dbc.service.APIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("dbc-APIServiceImpl")
@Slf4j
public class APIServiceImpl implements APIService {

    @Autowired
    private SpringContext context;

    @Autowired
    private APIDao apiDao;

    @Override
    public Object getConfigList(APIModel model) {
        model.setApp(context.getProperty("spring.application.name"));
        model.setUsername(UserContext.get().getUserName());

        List<ConfigEntity> profileConfigs = apiDao.queryConfigList(model);
        Map<String, ConfigEntity> profileConfigMap = profileConfigs.stream().collect(Collectors.toMap(ConfigEntity::getConfigKey, e -> e, (k1, k2) -> k2));

        model.setProfileName("default");
        List<ConfigEntity> defaultConfigs = apiDao.queryConfigList(model);

        List<TypePair> pairs;
        if (MapUtil.isNotEmpty(profileConfigMap)) {
            // 将当前profile的配置覆盖default的
            pairs = defaultConfigs.stream().map(e -> {
                ConfigEntity profileConfig = profileConfigMap.remove(e.getConfigKey());
                TypePair pair;
                if (profileConfig != null) {
                    pair = new TypePair(profileConfig.getConfigKey(), profileConfig.getConfigValue(), profileConfig.getDataType());
                } else {
                    pair = new TypePair(e.getConfigKey(), e.getConfigValue(), e.getDataType());
                }
                return pair;
            }).collect(Collectors.toList());
            // 增加追加profile多出来的
            if(MapUtil.isNotEmpty(profileConfigMap)){
                for (ConfigEntity conf : profileConfigMap.values()) {
                    pairs.add(new TypePair(conf.getConfigKey(), conf.getConfigValue(), conf.getDataType()));
                }
            }
        } else {
            pairs = profileConfigs.stream().map(e -> new TypePair(e.getConfigKey(), e.getConfigValue(), e.getDataType())).
                    collect(Collectors.toList());
        }

        if (model.isProp()) {
            return pairs;
        } else {
            return YamlPropertiesFileConverter.propertiesToYaml(pairs);
        }
    }

    @Override
    public TypePair getConfigValue(ConfigModel model) {
        model.setApp(context.getProperty("spring.application.name"));
        model.setUsername(UserContext.get().getUserName());
        ConfigEntity entity = apiDao.queryConfig(model);
        if (entity == null) {
            model.setProfileName("default");
            entity = apiDao.queryConfig(model);
        }
        return entity == null ? new TypePair(model.getConfigKey(), null, "java.lang.String")
                : new TypePair(model.getConfigKey(), entity.getConfigValue(), entity.getDataType());
    }
}
