package com.github.hbq969.middleware.dbc.driver.api;

import cn.hutool.core.codec.Base64;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.nio.charset.Charset;

import static feign.Util.checkNotNull;

public class BasicAuthRequestInterceptor implements RequestInterceptor {
    private ApiInfo api;
    private final String headerValue;


    public BasicAuthRequestInterceptor(ApiInfo api) {
        this.api = api;
        String username = api.getAuth().getBasic().getUsername();
        String password = api.getAuth().getBasic().getPassword();
        checkNotNull(username, "username");
        checkNotNull(password, "password");
        this.headerValue = "Basic " + base64Encode((username + ":" + password).getBytes(Charset.forName(api.getCharset())));
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.encode(bytes);
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(api.getAuth().getBasic().getKey(), headerValue);
    }
}
