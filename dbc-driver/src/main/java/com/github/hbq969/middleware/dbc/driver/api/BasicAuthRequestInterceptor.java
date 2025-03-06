package com.github.hbq969.middleware.dbc.driver.api;

import cn.hutool.core.codec.Base64;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

import static feign.Util.checkNotNull;

@Slf4j
public class BasicAuthRequestInterceptor implements RequestInterceptor {
    private ApiInfo api;
    private final String headerValue;

    private Charset c;


    public BasicAuthRequestInterceptor(ApiInfo api) {
        this.api = api;
        this.c = Charset.forName(api.getCharset());
        String username = api.getAuth().getBasic().getUsername();
        String password = api.getAuth().getBasic().getPassword();
        checkNotNull(username, "username");
        checkNotNull(password, "password");
        String auth = base64Encode((username + ":" + password).getBytes(c));
        this.headerValue = "Basic " + auth;
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.encode(bytes);
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(api.getAuth().getBasic().getKey(), headerValue);
    }
}
