package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.middleware.dbc.driver.api.feign.FeignFactoryBean;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;

import java.util.LinkedList;

public class PublicKeyServiceImpl extends FeignFactoryBean<PublicKeyService> {
    private ApiInfo api;

    public PublicKeyServiceImpl(ApiInfo api) {
        this.api = api;
    }

    @Override
    public Class<PublicKeyService> getObjectType() {
        return PublicKeyService.class;
    }

    @Override
    public String getUrl() {
        return this.api.getUrl();
    }

    @Override
    protected LinkedList<RequestInterceptor> interceptors() {
        LinkedList<RequestInterceptor> ins = super.interceptors();
        if (!api.isApiLog()) {
            ins.clear();
        }
        ins.addFirst(new BasicAuthRequestInterceptor(api));
        return ins;
    }
}
