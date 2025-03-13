package com.github.hbq969.middleware.dbc.service.impl;

import com.github.hbq969.code.common.decorde.OptionalFacade;
import com.github.hbq969.code.common.decorde.OptionalFacadeAware;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.spring.yaml.TypePair;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.middleware.dbc.service.FileReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service("dbc-PropertiesFileReader")
@Slf4j
public class PropertiesFileReader implements OptionalFacadeAware<String, FileReader>, FileReader {

    @Autowired
    private FileReaderFacade facade;

    @Autowired
    private SpringContext context;

    @Override
    public OptionalFacade<String, FileReader> getOptionalFacade() {
        return this.facade;
    }

    @Override
    public String getKey() {
        return "properties";
    }

    @Override
    public List<TypePair> read(InputStream in) {
        Properties p = new Properties();
        try {
            p.load(in);
        } catch (IOException e) {
            log.error("读取properties文件异常", e);
            throw new RuntimeException(I18nUtils.getMessage(context, "PropertiesFileReader.read.msg1"));
        }
        List<TypePair> pairs = new ArrayList<>();
        String key;
        Object value;
        Class<?> clz;
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            key = String.valueOf(entry.getKey());
            value = entry.getValue();
            clz = null == value ? String.class : value.getClass();
            TypePair pair = new TypePair(key, value, clz.getName());
            pairs.add(pair);
        }
        return pairs;
    }
}
