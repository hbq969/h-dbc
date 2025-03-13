package com.github.hbq969.middleware.dbc.service.impl;

import com.github.hbq969.code.common.decorde.OptionalFacade;
import com.github.hbq969.code.common.decorde.OptionalFacadeAware;
import com.github.hbq969.code.common.spring.context.SpringContext;
import com.github.hbq969.code.common.spring.yaml.TypePair;
import com.github.hbq969.code.common.utils.I18nUtils;
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

    @Autowired
    private SpringContext context;

    @Override
    public OptionalFacade<String, FileReader> getOptionalFacade() {
        return this.facade;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public Set<String> getKeys() {
        return Sets.newHashSet("yaml","yml");
    }

    @Override
    public List<TypePair> read(InputStream in) {
        String content=null;
        try {
            content = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取yaml文件异常",e);
            throw new RuntimeException(I18nUtils.getMessage(context,"YamlFileReader.read.msg1"));
        }
        if(content==null){
            return Collections.emptyList();
        }
        return YamlPropertiesFileConverter.yamlToProperties(content);
    }
}
