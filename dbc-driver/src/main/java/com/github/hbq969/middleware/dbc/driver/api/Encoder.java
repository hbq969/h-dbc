package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import com.github.hbq969.middleware.dbc.driver.utils.AESUtils;
import com.google.gson.Gson;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

@Slf4j
public class Encoder implements feign.codec.Encoder {

    private Gson gson = new Gson();
    private ApiInfo api;

    public Encoder(ApiInfo api) {
        this.api = api;
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        String content = gson.toJson(object);
        if (api.isApiLog()) {
            log.debug("加密前: {}", content);
        }
        String encryptBody = AESUtils.encrypt(content, this.api.getSecret(), this.api.getIv(), Charset.forName(this.api.getCharset()));
        if (api.isApiLog()) {
            log.debug("加密后: {}", encryptBody);
        }
        template.body(encryptBody);
    }
}
