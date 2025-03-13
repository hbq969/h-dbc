package com.github.hbq969.middleware.dbc.driver.api;

import cn.hutool.core.io.IoUtil;
import com.github.hbq969.middleware.dbc.driver.api.model.TypePair;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import com.github.hbq969.middleware.dbc.driver.utils.AESUtils;
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
        Reader reader = response.body().asReader(c);
        String content = IoUtil.read(reader);
        if (api.isApiLog()) {
            log.debug("解码前: {}", content);
        }
        String decryptBody = AESUtils.decrypt(content, this.api.getSecret(), this.api.getIv(), c);
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

    public static void main(String[] args) throws Exception {
        String content = "[{\"key\":\"advice.log.enabled\",\"value\":true,\"type\":\"java.lang.Boolean\"},{\"key\":\"server.port\",\"value\":30161,\"type\":\"java.lang.Integer\"},{\"key\":\"server.servlet.context-path\",\"value\":\"/h-example\",\"type\":\"java.lang.String\"},{\"key\":\"foo\",\"value\":12.34,\"type\":\"java.lang.Float\"},{\"key\":\"bar\",\"value\":1.67,\"type\":\"java.lang.Double\"},{\"key\":\"abc\",\"value\":123,\"type\":\"java.lang.Long\"},{\"key\":\"bbb\",\"value\":12,\"type\":\"java.lang.Short\"},{\"key\":\"ccc\",\"value\":10,\"type\":\"java.lang.Byte\"},{\"key\":\"ddd\",\"value\":\"\\n\",\"type\":\"java.lang.Character\"}]";
        Gson gson = new GsonBuilder().registerTypeAdapter(TypePair.class, new TypePairDeserializer()).create();
        List<TypePair> list = gson.fromJson(content, new TypeToken<List<TypePair>>() {
        });
        System.out.println(list);
    }
}
