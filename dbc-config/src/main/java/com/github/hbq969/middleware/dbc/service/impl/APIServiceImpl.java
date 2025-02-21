package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.middleware.dbc.dao.APIDao;
import com.github.hbq969.middleware.dbc.dao.entity.ConfigEntity;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.model.ConfigModel;
import com.github.hbq969.middleware.dbc.service.APIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("dbc-APIServiceImpl")
@Slf4j
public class APIServiceImpl implements APIService {

    @Autowired
    private SpringContext context;

    @Autowired
    private APIDao apiDao;

    @Override
    public List<Pair> getConfigList(APIModel model) {
        model.setApp(context.getProperty("spring.application.name"));
        List<ConfigEntity> ces = apiDao.queryConfigList(model);
        return ces.stream()
                .map(e -> new Pair(e.getConfigKey(), e.getConfigValue()))
                .collect(Collectors.toList());
    }

    @Override
    public Pair getConfigValue(ConfigModel model) {
        model.setApp(context.getProperty("spring.application.name"));
        ConfigEntity entity = apiDao.queryConfig(model);
        return entity == null ? new Pair(model.getConfigKey(), null) :
                new Pair(model.getConfigKey(), entity.getConfigValue());
    }
}
