package com.github.hbq969.middleware.dbc.driver.api;

import cn.hutool.core.lang.Pair;
import com.github.hbq969.code.common.encrypt.ext.utils.AESUtil;
import com.github.hbq969.code.common.utils.GsonUtils;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

@Slf4j
public class Decoder implements feign.codec.Decoder {
    private final static Gson gson = new Gson();
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
        String content = IOUtils.toString(reader);
        if (log.isTraceEnabled()) {
            log.trace("解码前: {}", content);
        }
        String decryptBody = AESUtil.decrypt(content, this.api.getSecret(), this.api.getIv(), c);
        if (log.isTraceEnabled()) {
            log.trace("解码后: {}", decryptBody);
        }
        if (decryptBody.startsWith("[")) {
            return GsonUtils.parseArray(decryptBody, new TypeToken<List<Pair>>() {
            });
        } else {
            return gson.fromJson(decryptBody, type);
        }
    }
}
