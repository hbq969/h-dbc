package com.github.hbq969.middleware.dbc.driver.api;

import com.github.hbq969.code.common.spring.cloud.feign.FeignFactoryBean;
import com.github.hbq969.middleware.dbc.driver.config.ApiInfo;
import feign.RequestInterceptor;
import feign.Retryer;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class ConfigServiceImpl extends FeignFactoryBean<ConfigService> {

    private ApiInfo api;

    public ConfigServiceImpl(ApiInfo api) {
        this.api = api;
    }

    @Override
    protected feign.codec.Encoder encoder() {
        return new Encoder(this.api.getSecret(), this.api.getCharset());
    }

    @Override
    protected feign.codec.Decoder decoder() {
        return new Decoder(this.api.getSecret(), this.api.getCharset());
    }

    @Override
    protected Retryer feignRetry() {
        return new Retryer.Default(1000L, TimeUnit.SECONDS.toMillis(5L), this.api.getRetry());
    }

    @Override
    protected LinkedList<RequestInterceptor> interceptors() {
        LinkedList<RequestInterceptor> ins = super.interceptors();
        if(api.getAuth().isEnabled()){
            ins.addLast(new BasicAuthRequestInterceptor(api));
        }
        return ins;
    }
}
