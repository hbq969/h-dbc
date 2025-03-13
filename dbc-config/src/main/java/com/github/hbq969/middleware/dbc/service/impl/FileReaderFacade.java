package com.github.hbq969.middleware.dbc.service.impl;

import com.github.hbq969.code.common.decorde.DefaultOptionalFacade;
import com.github.hbq969.code.common.spring.yaml.TypePair;
import com.github.hbq969.middleware.dbc.service.FileReader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service("dbc-FileReaderFacade")
public class FileReaderFacade extends DefaultOptionalFacade<String,FileReader> implements FileReader {
    @Override
    public List<TypePair> read(InputStream in) {
        return getService().read(in);
    }
}
