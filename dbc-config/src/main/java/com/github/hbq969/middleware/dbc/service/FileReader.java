package com.github.hbq969.middleware.dbc.service;

import cn.hutool.core.lang.Pair;

import java.io.InputStream;
import java.util.List;

public interface FileReader {
    List<Pair<String, Object>> read(InputStream in);
}
