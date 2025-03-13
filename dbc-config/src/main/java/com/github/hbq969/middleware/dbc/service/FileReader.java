package com.github.hbq969.middleware.dbc.service;

import com.github.hbq969.code.common.spring.yaml.TypePair;

import java.io.InputStream;
import java.util.List;

public interface FileReader {
    List<TypePair> read(InputStream in);
}
