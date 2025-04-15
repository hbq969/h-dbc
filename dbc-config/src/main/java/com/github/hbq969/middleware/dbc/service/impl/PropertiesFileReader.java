package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.lang.PatternPool;
import com.github.hbq969.code.common.decorde.OptionalFacade;
import com.github.hbq969.code.common.decorde.OptionalFacadeAware;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.spring.yaml.TypePair;
import com.github.hbq969.code.common.utils.I18nUtils;
import com.github.hbq969.middleware.dbc.service.FileReader;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service("dbc-PropertiesFileReader")
@Slf4j
public class PropertiesFileReader implements OptionalFacadeAware<String, FileReader>, FileReader {

    @Autowired
    private FileReaderFacade facade;

    @Autowired
    private SpringContext context;

    private final static Set<String> BOOLEAN_SET = Sets.newHashSet("true", "false");

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
        try (InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            p.load(isr);
        } catch (IOException e) {
            log.error("读取properties文件异常", e);
            throw new RuntimeException(I18nUtils.getMessage(context, "PropertiesFileReader.read.msg1"));
        }
        if (log.isDebugEnabled())
            log.debug("读取properties配置数量: {} 个", p.size());
        List<TypePair> pairs = new ArrayList<>();
        String key;
        Object value;
        Class<?> clz;
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            key = String.valueOf(entry.getKey());
            value = entry.getValue();
            clz = getValueClass(value);
            TypePair pair = new TypePair(key, value, clz.getName());
            pairs.add(pair);
        }
        return pairs;
    }

    private Class<?> getValueClass(Object value) {
        Class<?> clz;
        String valueStr;
        if (value == null)
            clz = String.class;
        else {
            valueStr = value.toString();
            if (BOOLEAN_SET.contains(valueStr.toLowerCase())) {
                clz = Boolean.class;
            } else if (valueStr.startsWith("0")) {
                clz = String.class;
            } else if (PatternPool.NUMBERS.matcher(valueStr).matches()) {
                clz = Long.class;
            } else
                clz = String.class;
        }
        return clz;
    }
}
