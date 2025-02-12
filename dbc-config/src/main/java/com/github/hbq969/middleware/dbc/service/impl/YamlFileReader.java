package com.github.hbq969.middleware.dbc.service.impl;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.decorde.OptionalFacade;
import com.github.hbq969.code.common.decorde.OptionalFacadeAware;
import com.github.hbq969.code.common.utils.YamlPropertiesFileConverter;
import com.github.hbq969.middleware.dbc.service.FileReader;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service("dbc-YamlFileReader")
@Slf4j
public class YamlFileReader implements OptionalFacadeAware<String, FileReader>,FileReader{

    @Autowired
    private FileReaderFacade facade;

    @Override
    public OptionalFacade<String, FileReader> getOptionalFacade() {
        return this.facade;
    }

    @Override
    public String getKey() {
        return "yml";
    }

    @Override
    public Set<String> getKeys() {
        return Sets.newHashSet("yaml","yml");
    }

    @Override
    public List<Pair<String, Object>> read(InputStream in) {
        String content=null;
        try {
            content = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取yaml文件异常",e);
            throw new RuntimeException("读取yaml文件异常");
        }
        if(content==null){
            return Collections.emptyList();
        }
        return YamlPropertiesFileConverter.yamlToProperties(content);
    }
}
