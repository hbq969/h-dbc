package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.code.common.encrypt.ext.utils.AESUtil;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

@Slf4j
public class Encoder implements feign.codec.Encoder {

    private ApiInfo api;

    public Encoder(ApiInfo api) {
        this.api = api;
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        String content = GsonUtils.toJson(object);
        if (log.isTraceEnabled()) {
            log.trace("加密前: {}", content);
        }
        String encryptBody = AESUtil.encrypt(content, this.api.getSecret(), this.api.getIv(), Charset.forName(this.api.getCharset()));
        if (log.isTraceEnabled()) {
            log.trace("加密后: {}", encryptBody);
        }
        template.body(encryptBody);
    }
}
