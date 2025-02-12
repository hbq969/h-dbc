package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.decorde.OptionalFacade;
import com.github.hbq969.code.common.decorde.OptionalFacadeAware;
import com.github.hbq969.middleware.dbc.service.FileReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service("dbc-PropertiesFileReader")
@Slf4j
public class PropertiesFileReader implements OptionalFacadeAware<String, FileReader>,FileReader {

    @Autowired
    private FileReaderFacade facade;

    @Override
    public OptionalFacade<String, FileReader> getOptionalFacade() {
        return this.facade;
    }

    @Override
    public String getKey() {
        return "properties";
    }

    @Override
    public List<Pair<String, Object>> read(InputStream in) {
        Properties p = new Properties();
        try {
            p.load(in);
        } catch (IOException e) {
            log.error("读取properties文件异常",e);
            throw new RuntimeException("读取");
        }
        List<Pair<String,Object>> pairs = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            Pair<String,Object> pair=new Pair<>(String.valueOf(entry.getKey()),entry.getValue());
            pairs.add(pair);
        }
        return pairs;
    }
}
