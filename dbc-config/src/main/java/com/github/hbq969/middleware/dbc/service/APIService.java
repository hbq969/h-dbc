package com.github.hbq969.middleware.dbc.service;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.middleware.dbc.model.APIModel;

import java.util.List;

public interface APIService {
    List<Pair> getConfigList(APIModel model);
}
