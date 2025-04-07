package com.github.hbq969.middleware.dbc.driver.api;

import cn.hutool.core.io.IoUtil;
import com.github.hbq969.middleware.dbc.driver.api.model.TypePair;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import com.github.hbq969.middleware.dbc.driver.utils.AESUtils;
import com.github.hbq969.middleware.dbc.driver.utils.MDCUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

@Slf4j
public class Decoder implements feign.codec.Decoder {
    private final static Gson gson = new GsonBuilder().registerTypeAdapter(TypePair.class, new TypePairDeserializer()).create();
    private ApiInfo api;

    private Charset c;

    public Decoder(ApiInfo api) {
        this.api = api;
        this.c = Charset.forName(api.getCharset());
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (response.body() == null) {
            return null;
        }
        String key = MDCUtils.rmAndGet("dbc,api,aes,key");
        String iv = MDCUtils.rmAndGet("dbc,api,aes,iv");
        Reader reader = response.body().asReader(c);
        String content = IoUtil.read(reader);
        if (api.isApiLog()) {
            log.debug("解码前, key: {}, iv: {}, 相应报文: {}", key, iv, content);
        }
        String decryptBody = AESUtils.decrypt(content, key, iv, c);
        if (api.isApiLog()) {
            log.debug("解码后: {}", decryptBody);
        }
        if (decryptBody.startsWith("[")) {
            return gson.fromJson(decryptBody, new TypeToken<List<TypePair>>() {
            });
        } else {
            return gson.fromJson(decryptBody, type);
        }
    }
}
