package com.github.hbq969.middleware.dbc.service;

import com.github.hbq969.code.common.spring.yaml.TypePair;
import com.github.hbq969.middleware.dbc.model.APIModel;
import com.github.hbq969.middleware.dbc.model.ConfigModel;

public interface APIService {
    Object getConfigList(APIModel model);

    TypePair getConfigValue(ConfigModel model);
}
